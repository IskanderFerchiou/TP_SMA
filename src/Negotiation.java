import java.util.List;

public class Negotiation implements Runnable {

    private final Ticket ticket;
    private final Provider provider;
    private final Buyer buyer;

    private NegotiationStatus status;

    private String issue;

    public Negotiation(Ticket ticket, Buyer buyer, Provider provider) {
        this.provider = provider;
        this.buyer = buyer;
        this.ticket = ticket;
        this.status = NegotiationStatus.RUNNING;
        this.issue = "";
    }

    public NegotiationStatus getStatus() {
        return status;
    }

    public Provider getProvider() {
        return provider;
    }

    public synchronized boolean isTicketNotAvailable() {
        synchronized (Ticket.class) {
            if (ticket.isNotAvailable()) {
                stopNegociation(NegotiationStatus.FAILURE,  "le ticket a été vendu à un autre acheteur.");
            }
            return ticket.isNotAvailable();
        }
    }

    public synchronized boolean isBuyerNotAvailable() {
        synchronized (Ticket.class) {
            if (!buyer.isAvailable()) {
                stopNegociation(NegotiationStatus.FAILURE,  "le client n'est plus disponible.");
            }
            return !buyer.isAvailable();
        }
    }

    public Ticket getTicket() {
        return ticket;
    }

    private void stopNegociation(NegotiationStatus status, String issue) {
        this.status = status;
        this.issue = issue;
    }

    private void displayDiscussion() {
        StringBuilder discussion = new StringBuilder();
        List<Offer> history = this.buyer.getOffers(this);

        discussion.append("\n------------------ Historique de la négociation ").append(Thread.currentThread().getId()).append(" ------------------ \n");

        discussion.append("- ").append(this.ticket).append("\n");
        discussion.append("- Client : ").append(this.buyer).append("\n");
        discussion.append("- Status : ").append(this.status).append("\n");
        discussion.append("- Raison : ").append(issue).append("\n\n");

        for (int i = 0; i < history.size(); i++) {

            if (i % 2 == 0) {
                discussion.append("Client ").append(this.buyer.getName()).append(" : ");
            } else {
                discussion.append("Vendeur ").append(this.provider.getId()).append(" : ");
            }

            discussion.append(history.get(i)).append("\n");

        }
        discussion.append("--------------------------------------------------------------------- \n");

        System.out.println(discussion);
    }

    @Override
    public void run() {
        Offer offer;

        // première proposition de l'acheteur
        int buyerPrice = buyer.calculatePrice(this);
        int numberOfOffers = 1; // compteur
        offer = new Offer(ticket, buyerPrice, Timer.getDate(), numberOfOffers);
        buyer.send(this, offer);

        while (Thread.currentThread().isAlive()) {
            // avant de réagir à l'offre de l'acheteur, on vérifie si le ticket est toujours disponible
            if (isTicketNotAvailable()) break;

            // réponse du fournisseur en fonction de ses contraintes
            Response providerResponse = provider.checkConstraint(offer);
            offer.setResponse(providerResponse);

            // si le prix est trop bas, on continue les offres
            if (providerResponse == Response.LOW_PROPOSAL) {

                // calcul du nouveau prix selon le ticket et la dernière offre du provider
                int providerPrice = provider.calculatePrice(this);

                offer = new Offer(ticket, providerPrice, Timer.getDate(), numberOfOffers);
                provider.send(this, offer);

                // sinon on arrête la négociation car la vente a été effectué
            } else if (providerResponse == Response.VALID_CONSTRAINTS) {

                // un seul thread pour la phrase de paiement (vente du ticket)
                synchronized (Ticket.class) {
                    if (isTicketNotAvailable() || isBuyerNotAvailable()) break;
                    provider.sellTicket(ticket);
                    buyer.setAvailable(false);
                    stopNegociation(NegotiationStatus.SUCCESS, "le fournisseur a vendu le ticket.");
                    break;
                }
                // ou une contrainte majeur n'a pas été respecté
            } else {
                stopNegociation(NegotiationStatus.FAILURE, "Dernier jour de vente (" + ticket.getLatestProvidingDate() + ") terminé.");
                break;
            }

            // avant de réagir à l'offre du vendeur, on vérifie si l'acheteur est toujours à la recherche d'un ticket
            if (isBuyerNotAvailable()) break;


            // L'acheteur étudie l'offre selon ses contraintes et répond au fournisseur
            Response buyerResponse = buyer.checkConstraint(offer);
            offer.setResponse(buyerResponse);
            // on passe au lendemain (le fournisseur fait 1 offre par jour)
            synchronized (Utils.lock) {
                try {
                    Utils.lock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            // si le prix est trop bas, on continue les offres
            if (buyerResponse == Response.KEEP_NEGOCIATING) {
                // on vérifie d'abord si l'acheteur a dépasse le nombre maximum d'offres
                if (numberOfOffers == buyer.getMaximumNumberOfOffers()) {
                    stopNegociation(NegotiationStatus.FAILURE, "le nombre maximum d'offres a été dépassé.");
                    break;
                } else {
                    buyerPrice = buyer.calculatePrice(this);
                    numberOfOffers++;
                    offer = new Offer(ticket, buyerPrice, Timer.getDate(), numberOfOffers);
                    buyer.send(this, offer);
                }
                // sinon on arrête la négociation car l'achat a été effectué
            } else if (buyerResponse == Response.VALID_CONSTRAINTS) {

                // un seul thread pour la phrase de paiement (vente du ticket)
                synchronized (Ticket.class) {
                    if (isTicketNotAvailable() || isBuyerNotAvailable()) break;
                    provider.sellTicket(ticket);
                    buyer.setAvailable(false);
                    stopNegociation(NegotiationStatus.SUCCESS, "le client a acheté le ticket.");
                    break;

                }

                // ou une contrainte majeur n'a pas été respecté
            } else {
                stopNegociation(NegotiationStatus.FAILURE, "Date d'achat maximum écoulée (" + Utils.formatDate(buyer.getLatestBuyingDate()) + ").");
                break;
            }


        }
        displayDiscussion();
    }
}

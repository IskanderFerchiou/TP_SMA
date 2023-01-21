import java.util.Date;
import java.util.List;
import java.util.concurrent.Phaser;

public class Negotiation implements Runnable {

    private final Ticket ticket;
    private final Provider provider;
    private final Buyer buyer;

    private Date currentDate;

    private NegotiationStatus status;

    private final Phaser phaser;

    public Negotiation(Ticket ticket, Buyer buyer, Provider provider, Phaser phaser) {
        this.provider = provider;
        this.buyer = buyer;
        this.ticket = ticket;
        this.status = NegotiationStatus.RUNNING;
        this.currentDate = ticket.getPreferedProvidingDate();
        this.phaser = phaser;
        phaser.register();
    }

    public NegotiationStatus getStatus() {
        return status;
    }

    public Date getCurrentDate() {
        return currentDate;
    }

    public Provider getProvider() {
        return provider;
    }


    public boolean isTicketNotAvailable() {
        synchronized (Ticket.class) {
            if (ticket.isNotAvailable()) {
                this.status = NegotiationStatus.FAILURE;
                System.out.println("Négociation " + Thread.currentThread().getId() + " : " + ticket + " a été vendu à un autre acheteur.");
            }
            return ticket.isNotAvailable();
        }
    }

    public boolean isBuyerNotAvailable() {
        synchronized (Ticket.class) {
            if (!buyer.isAvailable()) {
                this.status = NegotiationStatus.FAILURE;
                System.out.println("Négociation " + Thread.currentThread().getId() + " : " + buyer.getName() + " n'est plus disponible.");
            }
            return !buyer.isAvailable();
        }
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void displayDiscussion() {
        StringBuilder discussion = new StringBuilder();
        List<Offer> history = this.buyer.getOffers(this);

        discussion.append("\n------------------ Historique de la négociation ").append(Thread.currentThread().getId()).append(" ------------------ \n");

        discussion.append("- ").append(this.ticket).append("\n");
        discussion.append("- Client : ").append(this.buyer).append("\n\n");

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
        phaser.arriveAndAwaitAdvance();

        Offer offer;

        // première proposition de l'acheteur
        int buyerPrice = buyer.calculatePrice(this);
        int numberOfOffers = 1; // compteur
        offer = new Offer(provider, buyer, ticket, buyerPrice, currentDate, numberOfOffers);
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

                offer = new Offer(provider, buyer, ticket, providerPrice, currentDate, numberOfOffers);
                provider.send(this, offer);

                // on passe au lendemain (le fournisseur fait 1 offre par jour)
                currentDate = Utils.nextDay(currentDate);

                // sinon on arrête la négociation car la vente a été effectué
            } else if (providerResponse == Response.VALID_CONSTRAINTS) {
                // on procéde à la vente si le ticket est toujours en vente et que l'acheteur est toujours à la recherche d'un ticket
                if (isTicketNotAvailable() || isBuyerNotAvailable()) break;

                // un seul thread pour la phrase de paiement (vente du ticket)
                synchronized (Ticket.class) {
                    provider.sellTicket(ticket);
                    buyer.setAvailable(false);
                    this.status = NegotiationStatus.SUCCESS;
                    System.out.println("Négociation " + Thread.currentThread().getId() + " : le fournisseur a vendu le " + ticket + " à " + buyer.getName() + ".");
                    break;
                }
                // ou une contrainte majeur n'a pas été respecté
            } else {
                this.status = NegotiationStatus.FAILURE;
                System.out.println("Négociation " + Thread.currentThread().getId() + " : Dernier jour de vente (" + ticket.getLatestProvidingDate() + ") terminé.");
                break;
            }

            // avant de réagir à l'offre du vendeur, on vérifie si l'acheteur est toujours à la recherche d'un ticket
            if (isBuyerNotAvailable()) break;

            // L'acheteur étudie l'offre selon ses contraintes et répond au fournisseur
            Response buyerResponse = buyer.checkConstraint(offer);
            offer.setResponse(buyerResponse);

            // si le prix est trop bas, on continue les offres
            if (buyerResponse == Response.KEEP_NEGOCIATING) {
                // on vérifie d'abord si l'acheteur a dépasse le nombre maximum d'offres
                if (numberOfOffers == buyer.getMaximumNumberOfOffers()) {
                    this.status = NegotiationStatus.FAILURE;
                    System.out.println("Négociation " + Thread.currentThread().getId() + " : le nombre maximum d'offres a été dépassé.");
                    break;
                } else {
                    buyerPrice = buyer.calculatePrice(this);
                    offer = new Offer(provider, buyer, ticket, buyerPrice, currentDate, numberOfOffers);
                    buyer.send(this, offer);
                    numberOfOffers++;
                }
                // sinon on arrête la négociation car l'achat a été effectué
            } else if (buyerResponse == Response.VALID_CONSTRAINTS) {
                // on procéde à l'achat si le ticket est toujours en vente et que l'acheteur est toujours à la recherche d'un ticket
                if (isTicketNotAvailable() || isBuyerNotAvailable()) break;

                // un seul thread entre dans cette partie du code (achat du ticket)
                synchronized (Ticket.class) {
                    provider.sellTicket(ticket);
                    buyer.setAvailable(false);
                    this.status = NegotiationStatus.SUCCESS;
                    System.out.println("Négociation " + Thread.currentThread().getId() + " : le client " + buyer.getName() + " a acheté le " + ticket + ".");
                    break;
                }
                // ou une contrainte majeur n'a pas été respecté
            } else {
                this.status = NegotiationStatus.FAILURE;
                System.out.println("Négociation " + Thread.currentThread().getId() + " : Date d'achat maximum écoulée pour " + buyer.getName() + " (" + Utils.formatDate(buyer.getLatestBuyingDate()) + ").");
                break;
            }
        }
        displayDiscussion();
    }
}

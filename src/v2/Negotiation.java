package v2;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Negotiation implements Runnable {

    private final Ticket ticket;
    private final Provider provider;
    private final Buyer buyer;

    private Date finalDate;

    private NegotiationStatus status;

    private CountDownLatch latch;

    public Negotiation(Ticket ticket, Buyer buyer, Provider provider, CountDownLatch latch) {
        this.provider = provider;
        this.buyer = buyer;
        this.ticket = ticket;
        this.status = NegotiationStatus.RUNNING;
        this.finalDate = new Date();
        this.latch = latch;
    }

    public NegotiationStatus getStatus() {
        return status;
    }

    public Date getFinalDate() {
        return finalDate;
    }

    public Provider getProvider() {
        return provider;
    }


    public boolean isTicketNotAvailable() {
        if (ticket.isNotAvailable()) {
            System.out.println("Négociation " + Thread.currentThread().getId() + " : " + ticket + " a été vendu à un autre acheteur.");
            this.status = NegotiationStatus.FAILURE;
        }
        return ticket.isNotAvailable();
    }

    public boolean isBuyerNotAvailable() {
        if (!buyer.isAvailable()) {
            System.out.println("Négociation " + Thread.currentThread().getId() + " : " + buyer.getName() + " n'est plus disponible.");
            this.status = NegotiationStatus.FAILURE;
        }
        return !buyer.isAvailable();
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void displayDiscussion() {
        String discussion = "";

        List<Offer> providerReceivedOffers = this.provider.getOffers();
        List<Offer> buyerReceivedOffers = this.buyer.getOffers();

        providerReceivedOffers = providerReceivedOffers.stream().filter(offer -> offer.getBuyer().equals(this.buyer)).toList();
        buyerReceivedOffers = buyerReceivedOffers.stream().filter(offer -> offer.getProvider().equals(this.provider)).toList();

        discussion += "------ Historique de la négociation " + Thread.currentThread().getId() + " ------ \n";

        for (int i = 0; i < Math.max(providerReceivedOffers.size(), buyerReceivedOffers.size()); i++) {

            if (providerReceivedOffers.size() > i) {
                discussion += providerReceivedOffers.get(0) + "\n";
            }

            if (buyerReceivedOffers.size() > i) {
                discussion += buyerReceivedOffers.get(0) + "\n";
            }
        }
        discussion += "------------------------------------------";

        System.out.println(discussion);
    }

    @Override
    public void run() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        int maximumNumberOfOffers = 6; // limite

        // date de la première offre
        Date offerDate = ticket.getPreferedProvidingDate();
        Offer offer;

        // première proposition de l'acheteur
        int buyerPrice = buyer.calculatePrice(ticket);
        offer = new Offer(provider, buyer, ticket, buyerPrice, offerDate);
        buyer.send(provider, offer);
        int numberOfOffers = 1; // compteur

        while (Thread.currentThread().isAlive()) {
            // avant de réagir à l'offre de l'acheteur, on vérifie si le ticket est toujours disponible
            if (isTicketNotAvailable()) break;

            // réponse du fournisseur en fonction de ses contraintes
            Response providerResponse = provider.checkConstraint(offer);
            offer.setResponse(providerResponse);

            // si le prix est trop bas, on continue les offres
            if (providerResponse == Response.PRICE_TOO_LOW) {
                // on vérifie d'abord si l'acheteur a dépasse le nombre maximum d'offres
                if (numberOfOffers == maximumNumberOfOffers) {
                    this.status = NegotiationStatus.FAILURE;
                    System.out.println("Négociation " + Thread.currentThread().getId() + " : le nombre maximum d'offres a été dépassé.");
                    break;
                } else {
                    // calcul du nouveau prix selon le ticket et la dernière offre du provider
                    int providerPrice = provider.calculatePrice(ticket, offerDate);
                    offer = new Offer(provider, buyer, ticket, providerPrice, offerDate);
                    provider.send(buyer, offer);

                    // on passe au lendemain (le fournisseur fait 1 offre par jour)
                    offerDate = Utils.nextDay(offerDate);
                }
                // sinon on arrête la négociation car la vente a été effectué
            } else if (providerResponse == Response.VALID_CONSTRAINTS) {
                // un seul thread pour la phrase de paiement (vente du ticket)
                synchronized (Ticket.class) {
                    // on procéde à la vente si le ticket est toujours en vente et que l'acheteur est toujours à la recherche d'un ticket
                    if (isTicketNotAvailable() || isBuyerNotAvailable()) break;

                    this.status = NegotiationStatus.SUCCESS;
                    System.out.println("Négociation " + Thread.currentThread().getId() + " : le fournisseur a vendu le " + ticket + " à " + buyer.getName() + ".");
                    provider.sellTicket(ticket);
                    buyer.setAvailable(false);
                    break;
                }
                // ou une contrainte majeur n'a pas été respecté
            } else {
                this.status = NegotiationStatus.FAILURE;
                System.out.println("Négociation " + Thread.currentThread().getId() + " : contrainte du vendeur non respecté.");
                break;
            }

            // avant de réagir à l'offre du vendeur, on vérifie si l'acheteur est toujours à la recherche d'un ticket
            if (!buyer.isAvailable()) break;


            // L'acheteur étudie l'offre selon ses contraintes et répond au fournisseur
            Response buyerResponse = buyer.checkConstraint(offer);
            offer.setResponse(buyerResponse);

            // si le prix est trop bas, on continue les offres.
            if (buyerResponse == Response.BUDGET_NOT_ENOUGH) {
                buyerPrice = buyer.calculatePrice(ticket);
                offer = new Offer(provider, buyer, ticket, buyerPrice, offerDate);
                buyer.send(provider, offer);
                numberOfOffers++;
                // sinon on arrête la négociation car l'achat a été effectué
            } else if (buyerResponse == Response.VALID_CONSTRAINTS) {
                // un seul thread entre dans cette partie du code (achat du ticket)
                synchronized (Ticket.class) {
                    // on procéde à l'achat si le ticket est toujours en vente et que l'acheteur est toujours à la recherche d'un ticket
                    if (isTicketNotAvailable() || isBuyerNotAvailable()) break;

                    this.status = NegotiationStatus.SUCCESS;
                    System.out.println("Négociation " + Thread.currentThread().getId() + " : le client " + buyer.getName() + " a acheté le " + ticket + ".");
                    provider.sellTicket(ticket);
                    buyer.setAvailable(false);
                    break;
                }
                // ou une contrainte majeur n'a pas été respecté
            } else {
                this.status = NegotiationStatus.FAILURE;
                System.out.println("Négociation " + Thread.currentThread().getId() + " : contrainte de l'acheteur non respecté.");
                break;
            }
        }
        // date de fin des négociations pour mettre à jour la date actuelle du point de vue du buyer
        this.finalDate = offerDate;
        displayDiscussion();
    }
}

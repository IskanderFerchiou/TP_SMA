package v2;

import java.util.Date;

public class Negotiation implements Runnable {

    private final Ticket ticket;
    private final Provider provider;
    private final Buyer buyer;

    private Date finalDate;

    private NegotiationStatus status;

    public Negotiation(Ticket ticket, Buyer buyer, Provider provider) {
        this.provider = provider;
        this.buyer = buyer;
        this.ticket = ticket;
        this.status = NegotiationStatus.RUNNING;
        this.finalDate = new Date();
    }

    public NegotiationStatus getStatus() {
        return status;
    }

    public Date getFinalDate() {
        return finalDate;
    }

    @Override
    public void run() {

        try {
            Thread.sleep(Thread.currentThread().getId() * 100);
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

        while (true) {
            // avant de réagir à l'offre de l'acheteur, on vérifie si le ticket est toujours disponible
            if (ticket.isNotAvailable()) {
                System.out.println("Negotiation " + Thread.currentThread().getId() + " : le ticket a été vendu à un autre acheteur.");
                this.status = NegotiationStatus.FAILURE;
                break;
            }

            // réponse du fournisseur en fonction de ses contraintes
            Response providerResponse = provider.checkConstraint(offer);
            offer.setResponse(providerResponse);

            // si le prix est trop bas, on continue les offres
            if (providerResponse == Response.PRICE_TOO_LOW) {
                // on vérifie d'abord si l'acheteur a dépasse le nombre maximum d'offres
                if (numberOfOffers == maximumNumberOfOffers) {
                    this.status = NegotiationStatus.FAILURE;
                    System.out.println("Negotiation " + Thread.currentThread().getId() + " : le nombre maximum d'offres a été dépassé.");
                    break;
                } else {
                    // calcul du nouveau prix selon le ticket et la dernière offre du provider
                    int providerPrice = provider.calculatePrice(ticket);
                    offer = new Offer(provider, buyer, ticket, providerPrice, offerDate);
                    provider.send(buyer, offer);

                    // on passe au lendemain (le fournisseur fait 1 offre par jour)
                    offerDate = Utils.nextDay(offerDate);
                }
            // sinon on arrête la négociation car la vente a été effectué
            } else if (providerResponse == Response.VALID_CONSTRAINTS) {
                this.status = NegotiationStatus.SUCCESS;
                provider.sellTicket(ticket);
                System.out.println("Negotiation " + Thread.currentThread().getId() + " : le fournisseur a vendu le ticket.");
                break;
            // ou une contrainte majeur n'a pas été respecté
            } else {
                this.status = NegotiationStatus.FAILURE;
                System.out.println("Negotiation " + Thread.currentThread().getId() + " : contrainte du vendeur non respecté.");
                break;
            }

            // avant de réagir à l'offre du vendeur, on vérifie si le ticket est toujours disponible
            if (ticket.isNotAvailable()) {
                System.out.println("Negotiation " + Thread.currentThread().getId() + " : le ticket a été vendu à un autre acheteur.");
                this.status = NegotiationStatus.FAILURE;
                break;
            }

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
                this.status = NegotiationStatus.SUCCESS;
                System.out.println("Negotiation " + Thread.currentThread().getId() + " : le client a acheté le ticket.");
                provider.sellTicket(ticket);
                break;
            // ou une contrainte majeur n'a pas été respecté
            } else {
                this.status = NegotiationStatus.FAILURE;
                System.out.println("Negotiation " + Thread.currentThread().getId() + " : contrainte de l'acheteur non respecté.");
                break;
            }
        }
        // date de fin des négociations pour mettre à jour la date actuelle du point de vue du buyer
        this.finalDate = offerDate;
    }
}

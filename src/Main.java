import java.util.Date;

public class Main {
    public static void main(String[] args) {
        // Initialisation des fournisseurs
        Provider provider1 = new Provider();

        // Initialisation des billets
        Ticket ticket1 = new Ticket(
                provider1,
                new Date(2022, 12, 9),
                new Date(2022, 12, 10),
                "Paris",
                "Tokyo",
                1000,
                800,
                new Date(2022, 10, 10),
                new Date(2022, 12, 3)
        );
        provider1.addTicket(ticket1);

        // Initialisation de l'acheteur
        Buyer buyer1 = new Buyer(
                "Tokyo",
                800,
                new Date(2022, 12, 8),
                new Date(2022, 12, 6)
        );
        buyer1.addProvider(provider1);

        double negociation = 0.2;

        boolean firstOffer = true;
        int providerPrice = 0;

        while(true) {
            // la communication
            Offer offer;
            if (firstOffer) {
                providerPrice = ticket1.getPreferedProvidingPrice();
                firstOffer = false;

                offer = new Offer(provider1, buyer1, ticket1, providerPrice);
                System.out.println("Offre Provider : " + offer.getPrice());
            } else {
                Offer lastBuyerOffer = buyer1.getOffers().get(buyer1.getOffers().size()-1);
                Offer lastProviderOffer = provider1.getOffers().get(provider1.getOffers().size()-1);

                providerPrice = lastProviderOffer.getPrice() - (int)(lastProviderOffer.getPrice() * 0.1);
                if (providerPrice < ticket1.getMinimumProvidingPrice()) {
                    providerPrice = ticket1.getMinimumProvidingPrice();
                }

                offer = new Offer(provider1, buyer1, ticket1, providerPrice);
                System.out.println("Offre Provider : " + offer.getPrice());
            }

            provider1.addOffer(offer);

            Response buyerResponse = buyer1.checkConstraint(offer);
            offer.setResponse(buyerResponse);

            if(buyerResponse == Response.VALID_CONSTRAINTS) {
                break; // achat du ticket (toutes les contraintes sont validées)
            } else {
                if (buyerResponse == Response.BUDGET_NOT_ENOUGH) {
                    int budget = buyer1.getMaximumBudget();
                    int newPrice = budget - (int)(negociation * budget);

                    // à chaque offre de l'acheteur, on augmente sa proposition
                    if (negociation > 0) {
                        negociation = negociation - negociation/2;
                    }

                    offer = new Offer(provider1, buyer1, ticket1, newPrice);
                    System.out.println("Offre Buyer : " + offer.getPrice());

                    buyer1.addOffer(offer);

                    Response providerResponse = provider1.checkConstraint(offer);
                    offer.setResponse(providerResponse);

                    if (providerResponse == Response.VALID_CONSTRAINTS) {
                        break; // vente du ticket (toutes les contraintes sont validées)
                    }
                }
            }
        }
    }
}

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Main {
    public static List<Provider> instantiateProviders(int count) {
        List providers = new ArrayList();
        for (int i = 0; i < count; i++) {
            providers.add(new Provider());
        }
        return providers;
    }

    public static void main(String[] args) {
        // Initialisation des fournisseurs
        Provider provider1 = new Provider();
        List providers = instantiateProviders(5);

        // Initialisation des billets
        Ticket ticket1 = new Ticket(
                provider1,
                "Paris",
                "Tokyo",
                1000,
                800,
                new Date(2022, 11, 7),
                new Date(2022, 11, 10)
        );
        provider1.addTicket(ticket1);

        // Initialisation de l'acheteur
        Buyer buyer1 = new Buyer(
                "Tokyo",
                800,
                new Date(2022, 11, 9),
                new Date(2022, 11, 6)
        );
        buyer1.addProvider(provider1);

        // Date de la première offre
        Date offerDate = ticket1.getPreferedProvidingDate();

        // les négociations entre fournisseur et acheteur
        while(true) {
            System.out.println("-------------------");
            System.out.println("Jour de négociation : " + offerDate);

            // Calcul du nouveau prix selon le ticket et la dernière offre du provider
            int providerPrice = provider1.calculatePrice(ticket1);

            Offer offer = new Offer(provider1, buyer1, ticket1, providerPrice, offerDate);
            System.out.println("Offre Provider : " + offer.getPrice());

            provider1.addOffer(offer);

            // L'acheteur étudie l'offre selon ses contraintes et répond au fournisseur
            Response buyerResponse = buyer1.checkConstraint(offer);
            offer.setResponse(buyerResponse);

            // si les contraintes sont validés, l'acheteur achète le ticket
            if(buyerResponse == Response.VALID_CONSTRAINTS) {
                System.out.println("Le client a acheté le ticket.");
                break;
            } else if (buyerResponse == Response.DATE_TOO_LATE) {
                System.out.println("Dernière date d'achat maximum pour l'acheteur écoulée (" + buyer1.getLatestBuyingDate() + ")");
                break;
            // sinon, l'acheteur négocie le prix en fonction de son budget maximum
            } else {
                if (buyerResponse == Response.BUDGET_NOT_ENOUGH) {
                    int buyerPrice = buyer1.calculatePrice();

                    offer = new Offer(provider1, buyer1, ticket1, buyerPrice, offerDate);
                    System.out.println("Offre Buyer : " + offer.getPrice());

                    buyer1.addOffer(offer);

                    // Réponse du fournisseur en fonction de ses contraintes
                    Response providerResponse = provider1.checkConstraint(offer);
                    offer.setResponse(providerResponse);

                    // si toutes les contraintes sont validées, le fournisseur vend le ticket à l'acheteur, sinon on boucle
                    if (providerResponse == Response.VALID_CONSTRAINTS) {
                        System.out.println("Le vendeur a vendu le ticket.");
                        break;
                    } else if (providerResponse == Response.DATE_TOO_LATE) {
                        System.out.println("Dernier jour de vente (" + ticket1.getLatestProvidingDate() + ") terminé.");
                        break;
                    // on passe au lendemain (le fournisseur fait 1 offre par jour)
                    } else {
                        offerDate = Utils.nextDay(offerDate);
                    }
                }
            }
        }
    }
}

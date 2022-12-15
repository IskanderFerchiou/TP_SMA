import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Main {
    public static List<Provider> instantiateProviders(int count) {
        List<Provider> providers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            providers.add(new Provider());
        }
        return providers;
    }

    public static void main(String[] args) {
        // Initialisation des fournisseurs
        Provider provider1 = new Provider();
        List<Provider> providers = instantiateProviders(5);
        List<Ticket> tickets = Utils.instantiateTickets(providers);
        System.out.println("test");
        tickets.forEach(ticket -> System.out.println(ticket.getDeparturePlace()));
        System.out.println("test");

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
        while (true) {
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

            // si le prix est trop bas, on continue les offres sinon on arrête la négociation car l'achat a été effectué ou une contrainte majeur n'a pas été respecté
            if (buyerResponse == Response.BUDGET_NOT_ENOUGH) {
                int buyerPrice = buyer1.calculatePrice();

                offer = new Offer(provider1, buyer1, ticket1, buyerPrice, offerDate);
                System.out.println("Offre Buyer : " + offer.getPrice());

                buyer1.addOffer(offer);

                // Réponse du fournisseur en fonction de ses contraintes
                Response providerResponse = provider1.checkConstraint(offer);
                offer.setResponse(providerResponse);

                // si le prix est trop bas, on continue les offres sinon on arrête la négociation car la vente a été effectué ou une contrainte majeur n'a pas été respecté
                if (providerResponse == Response.PRICE_TOO_LOW) {
                    // on passe au lendemain (le fournisseur fait 1 offre par jour)
                    offerDate = Utils.nextDay(offerDate);
                } else {
                    break;
                }
            } else {
                break;
            }
        }
    }
}

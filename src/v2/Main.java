package v2;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    public static List<Provider> instantiateProviders(int count) {
        List<Provider> providers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            providers.add(new Provider());
        }
        return providers;
    }

    // N-N communication
    public static void main(String[] args) {
        // Initialisation du catalogue
        BlockingQueue<Ticket> catalogue = new LinkedBlockingQueue<>();

        // Date actuelle
        Date actualDate = new Date(2022, 11, 1);

        // Initialisation des fournisseurs
        List<Provider> providers = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            providers.add(new Provider(catalogue));
        }

        // Initialisation des acheteurs
        List<Buyer> buyers = new ArrayList<>();
        Buyer buyer1 = new Buyer(
                "Tokyo",
                900,
                new Date(2022, 11, 9),
                new Date(2022, 11, 6),
                catalogue,
                actualDate
        );
        Buyer buyer2 = new Buyer(
                "Tokyo",
                800,
                new Date(2022, 11, 15),
                new Date(2022, 11, 6),
                catalogue,
                actualDate
        );
        buyers.add(buyer1);
        buyers.add(buyer2);

        for (Provider p: providers) {
            Thread providerThread = new Thread(p);
            providerThread.start();
        }

        for (Buyer b: buyers) {
            Thread buyerThread = new Thread(b);
            buyerThread.start();
        }

    }
}

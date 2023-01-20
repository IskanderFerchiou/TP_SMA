package v2;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {

    // N-N communication
    public static void main(String[] args) throws IOException, ParseException {
        // Initialisation du catalogue
        BlockingQueue<Ticket> catalogue = new LinkedBlockingQueue<>();

        // Signal de départ
        CountDownLatch latch = new CountDownLatch(1);

        // Date actuelle
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date actualDate = df.parse("01/12/2022");

//        // Initialisation des fournisseurs
//        List<Provider> providers = new ArrayList<>();
//        for (int i = 0; i < 1; i++) {
//            providers.add(new Provider(catalogue));
//        }
//
//        // Initialisation des tickets
//
//
//        // Initialisation des acheteurs
//        List<Buyer> buyers = new ArrayList<>();
//        Buyer buyer1 = new Buyer(
//                "Phillipe",
//                "Tokyo",
//                900,
//                new Date(2022, 11, 9),
//                new Date(2022, 11, 6),
//                catalogue,
//                actualDate
//        );
//        Buyer buyer2 = new Buyer(
//                "James",
//                "Tokyo",
//                800,
//                new Date(2022, 11, 15),
//                new Date(2022, 11, 6),
//                catalogue,
//                actualDate
//        );
//        buyers.add(buyer1);
//        buyers.add(buyer2);

        String scenario = "3";
        NegotiationStrat strat = NegotiationStrat.TICKETS_SIMILARITY;
        List<Buyer> buyers = Utils.instantiateBuyers("Buyers" + scenario + ".csv", catalogue, actualDate, latch);
        List<Provider> providers = Utils.instantiateProviders("Providers" + scenario + ".csv", catalogue, strat);
        Utils.instantiateTickets("Tickets" + scenario + ".csv", providers);

        for (Provider p: providers) {
            Thread providerThread = new Thread(p);
            providerThread.start();
        }

        for (Buyer b: buyers) {
            Thread buyerThread = new Thread(b);
            buyerThread.start();
        }

        latch.countDown();
    }
}

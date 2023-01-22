import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {

    // N-N communication
    public static void main(String[] args) throws IOException, ParseException {
        // Initialisation du catalogue
        BlockingQueue<Ticket> catalogue = new LinkedBlockingQueue<>();

        String scenario = Scenario.RejectedProvidersN_N.getFileName();
        NegotiationStrat strat = NegotiationStrat.REMAINING_TIME;
        List<Buyer> buyers = Utils.instantiateBuyers("Buyers - " + scenario + ".csv", catalogue, strat);
        List<Provider> providers = Utils.instantiateProviders("Providers - " + scenario + ".csv", catalogue, strat);
        Utils.instantiateTickets("Tickets - " + scenario + ".csv", providers);

        for (Provider p: providers) {
            Thread providerThread = new Thread(p);
            providerThread.start();
        }

        List<Thread> buyersThread = new ArrayList<>();
        for (Buyer b: buyers) {
            Thread buyerThread = new Thread(b);
            buyerThread.start();
            buyersThread.add(buyerThread);
        }

        Timer timer = new Timer(buyersThread);
        Thread timerThread = new Thread(timer);
        timerThread.start();
    }
}
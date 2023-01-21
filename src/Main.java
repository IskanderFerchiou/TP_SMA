import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Phaser;

public class Main {

    // N-N communication
    public static void main(String[] args) throws IOException, ParseException {
        // Initialisation du catalogue
        BlockingQueue<Ticket> catalogue = new LinkedBlockingQueue<>();

        // Signal de départ
        Phaser phaser = new Phaser(1);

        // Date actuelle
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date actualDate = df.parse("01/12/2022");

        String scenario = "2";
        NegotiationStrat strat = NegotiationStrat.REMAINING_TIME;
        List<Buyer> buyers = Utils.instantiateBuyers("Buyers" + scenario + ".csv", catalogue, actualDate, latch, strat);
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

        phaser.arriveAndAwaitAdvance();
    }
}

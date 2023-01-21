import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class Utils {

    public static String formatDate(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        return df.format(date);
    }

    public static Date nextDay(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    public static List<Provider> instantiateProviders(String filename, BlockingQueue<Ticket> catalogue, NegotiationStrat strat) throws FileNotFoundException {
        File file = new File("datasets/" + filename);
        List<Provider> providers = new ArrayList<>();
        if (file.exists()) {
            FileReader reader = new FileReader(file);
            BufferedReader br = new BufferedReader(reader);
            int totalProviders = (int) br.lines().count() - 1;

            System.out.println("Liste des fournisseurs : ");

            for (int i = 0; i < totalProviders; i++) {
                providers.add(new Provider(i, catalogue, strat));
                System.out.println("- Fournisseur " + i);
            }
            System.out.println();
        }

        return providers;
    }

    public static List<Buyer> instantiateBuyers(String filename, BlockingQueue<Ticket> catalogue, Date actualDate, CountDownLatch latch, NegotiationStrat strat) throws IOException, ParseException {
        File file = new File("datasets/" + filename);
        List<Buyer> buyers = new ArrayList<>();
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        if (file.exists()) {
            FileReader reader = new FileReader(file);
            BufferedReader br = new BufferedReader(reader);
            String line;
            String [] spllitedLine;
            br.readLine(); // avoid first line

            System.out.println("Liste des acheteurs : ");

            while ((line = br.readLine()) != null)
            {
                spllitedLine = line.split(";");
                Buyer buyer = new Buyer(spllitedLine[0],
                        spllitedLine[1],
                        Integer.parseInt(spllitedLine[2]),
                        df.parse(spllitedLine[3]),
                        df.parse(spllitedLine[4]),
                        catalogue,
                        actualDate,
                        latch,
                        6, strat);

                if (spllitedLine.length > 5 && spllitedLine[5] != null && !spllitedLine[5].equals("")) {
                    buyer.addPreferredProviderID(Integer.valueOf(spllitedLine[5]));
                }

                if (spllitedLine.length > 5 && spllitedLine[6] != null && !spllitedLine[6].equals("")) {
                    buyer.addRejectedProviderID(Integer.valueOf(spllitedLine[6]));
                }

                buyers.add(buyer);

                System.out.println("- " + buyer);
            }
            System.out.println();
        }
        return buyers;
    }

    public static void instantiateTickets(String filename, List<Provider> providers) throws IOException, ParseException {
        File file = new File("datasets/" + filename);
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        if (file.exists()) {
            FileReader reader = new FileReader(file);
            BufferedReader br = new BufferedReader(reader);
            String line;
            String [] spllitedLine;
            br.readLine(); // avoid first line

            System.out.println("Liste des tickets : ");

            while ((line = br.readLine()) != null)
            {
                spllitedLine = line.split(";");
                Provider provider = providers.get(Integer.parseInt(spllitedLine[6]));

                Ticket ticket = new Ticket(
                        provider, // provider
                        spllitedLine[0], // departurePlace
                        spllitedLine[1], // arrivalPlace
                        Integer.parseInt(spllitedLine[3]), // preferedProvidingPrice
                        Integer.parseInt(spllitedLine[2]), // minimumProvidingPrice
                        df.parse(spllitedLine[5]), // preferedProvidingDate
                        df.parse(spllitedLine[4]) // latestProvidingDate
                );

                provider.addTicket(ticket);

                System.out.println("- " + ticket);
            }
            System.out.println();
        }

    }
}

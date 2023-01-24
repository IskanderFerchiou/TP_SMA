import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.BlockingQueue;

public class Utils {

    static final Object lock = new Object(); // shared object to synchronize on

    public static String formatDate(LocalDate date) {
        DateTimeFormatter  df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return df.format(date);
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

    public static List<Buyer> instantiateBuyers(String filename, BlockingQueue<Ticket> catalogue, NegotiationStrat strat) throws IOException {
        File file = new File("datasets/" + filename);
        List<Buyer> buyers = new ArrayList<>();
        DateTimeFormatter  df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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
                        LocalDate.parse(spllitedLine[3], df),
                        catalogue,
                        6, strat);

                if (spllitedLine.length > 4 && spllitedLine[4] != null && !spllitedLine[4].equals("")) {
                    buyer.addPreferredProviderID(Integer.valueOf(spllitedLine[4]));
                }

                if (spllitedLine.length > 5 && spllitedLine[5] != null && !spllitedLine[5].equals("")) {
                    buyer.addRejectedProviderID(Integer.valueOf(spllitedLine[5]));
                }

                buyers.add(buyer);

                System.out.println("- " + buyer);
            }
            System.out.println();
        }
        return buyers;
    }

    public static void instantiateTickets(String filename, List<Provider> providers) throws IOException {
        File file = new File("datasets/" + filename);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
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
                        LocalDate.parse(spllitedLine[5], df), // preferedProvidingDate
                        LocalDate.parse(spllitedLine[4], df) // latestProvidingDate
                );

                provider.addTicket(ticket);

                System.out.println("- " + ticket);
            }
            System.out.println();
        }

    }
}

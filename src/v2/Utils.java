package v2;

import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Utils {
    public static Date nextDay(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, 1);
        return c.getTime();
    }

    public static List<Ticket> instantiateTickets(List<Provider> providers) {
        List<Ticket> tickets = new ArrayList<>();
        Path pathToFile = Paths.get("datasets/firstDataset.csv");

        // create an instance of BufferedReader
        // using try with resource, Java 7 feature to close resources
        try (BufferedReader br = Files.newBufferedReader(pathToFile,
                StandardCharsets.US_ASCII)) {

            // read the first line from the text file
            String line = br.readLine();

            line = br.readLine();

            // loop until all lines are read
            while (line != null) {

                // use string.split to load a string array with the values from
                // each line of
                // the file, using a comma as the delimiter
                String[] attributes = line.split(";");

                Random rand = new Random();
                Ticket ticket = new Ticket(
                        providers.get(rand.nextInt(providers.size())), //provider
                        attributes[0], //departurePlace
                        attributes[1], //arrivalPlace
                        Integer.parseInt(attributes[3]), //preferedProvidingPrice
                        Integer.parseInt(attributes[2]), //minimumProvidingPrice
                        new Date(attributes[5]), //preferedProvidingDate
                        new Date(attributes[4]) //latestProvidingDate
                );

                // adding ticket into ArrayList
                tickets.add(ticket);

                // read next line before looping
                // if end of file reached, line would be null
                line = br.readLine();
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return tickets;
    }
}

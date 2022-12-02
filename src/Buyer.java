import java.util.Date;
import java.util.List;
import java.util.Map;

public class Buyer extends Agent {
    private int id;
    private String destination;

    // contraintes
    private Date lastDate;
    private int maximumBudget;

    // preferences
    private List<Provider> preferredProviders;
    private List<Provider> rejectedProviders;
    private Date deadline; // la date au plus tard d'achat du billet

    //
    public static Map<Provider, Ticket> msg;

}

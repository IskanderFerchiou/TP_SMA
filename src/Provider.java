import java.util.Date;
import java.util.List;
import java.util.Map;

public class Provider extends Agent {
    private List<Ticket> tickets;
    // preferences
    private Date saleOfferingDate;

    // contraintes
    private float minimumFee;
    private Date lateSellDate;

    public static Map<Buyer, Ticket> msg;
}

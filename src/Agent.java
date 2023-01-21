import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Agent implements Runnable  {
    public abstract Response checkConstraint(Offer offer);
    private final Inbox chat;
    protected BlockingQueue<Ticket> catalogue;

    protected NegotiationStrat strat;


    public Agent() {
        this.chat = new Inbox();
    }

    public CopyOnWriteArrayList<Offer> getOffers(Negotiation negotiation) {
        return chat.getOffers(negotiation);
    }

    public void send(Negotiation negotiation, Offer offer) {
        chat.send(negotiation, offer);
    }

    public List<Ticket> findSimilarTickets(Ticket ticket) {
        List<Ticket> similarTickets = new ArrayList<>();
        for (Ticket t : catalogue) {
            if(ticket.equals(t))
                similarTickets.add(t);
        }
        return similarTickets;
    }
}

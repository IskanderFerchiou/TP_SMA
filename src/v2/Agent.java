package v2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Agent implements Runnable  {
    public abstract Response checkConstraint(Offer offer);
    private final Inbox chat;
    protected BlockingQueue<Ticket> catalogue;


    public Agent() {
        this.chat = new Inbox();
    }

    public CopyOnWriteArrayList<Offer> getOffers() {
        return chat.getOffers(this);
    }

    public void send(Agent agent, Offer offer) {
        chat.send(agent, offer);
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

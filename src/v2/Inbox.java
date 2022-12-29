package v2;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Inbox {
    private static final ConcurrentHashMap<Agent, CopyOnWriteArrayList<Offer>> offers = new ConcurrentHashMap<>();

    public CopyOnWriteArrayList<Offer> getOffers(Agent agent) {
        CopyOnWriteArrayList<Offer> receivedOffers = offers.get(agent);
        if (receivedOffers == null) {
            receivedOffers = new CopyOnWriteArrayList<>();
            offers.put(agent, receivedOffers);
        }
        return receivedOffers;
    }

    public void send(Agent agent, Offer offer){
        CopyOnWriteArrayList<Offer> recipientOffers = offers.get(agent);
        if (recipientOffers == null) {
            recipientOffers = new CopyOnWriteArrayList<>();
        }
        recipientOffers.add(offer);
        offers.put(agent, recipientOffers);
    }
}

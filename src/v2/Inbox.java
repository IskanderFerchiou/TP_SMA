package v2;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Inbox {
    private static final ConcurrentHashMap<Negotiation, CopyOnWriteArrayList<Offer>> chat = new ConcurrentHashMap<>();

    public CopyOnWriteArrayList<Offer> getOffers(Negotiation negotiation) {
        CopyOnWriteArrayList<Offer> offers = chat.get(negotiation);
        if (offers == null) {
            offers = new CopyOnWriteArrayList<>();
            chat.put(negotiation, offers);
        }
        return offers;
    }

    public void send(Negotiation negotiation, Offer offer) {
        CopyOnWriteArrayList<Offer> offers = chat.get(negotiation);
        if (offers == null) {
            offers = new CopyOnWriteArrayList<>();
        }
        offers.add(offer);
        chat.put(negotiation, offers);
    }
}

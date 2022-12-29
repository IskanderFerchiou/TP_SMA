package v2;

import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Agent implements Runnable  {
    public abstract Response checkConstraint(Offer offer);
    private final Inbox chat;

    public Agent() {
        this.chat = new Inbox();
    }

    public CopyOnWriteArrayList<Offer> getOffers() {
        return chat.getOffers(this);
    }

    public void send(Agent agent, Offer offer) {
        chat.send(agent, offer);
    }
}

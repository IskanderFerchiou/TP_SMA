package v1;

public abstract class Agent extends Thread {
    private static Offer msg; // attribut qui sert à réaliser la communication entre les agents et les acheteurs
    public abstract Response checkConstraint(Offer offer);
}

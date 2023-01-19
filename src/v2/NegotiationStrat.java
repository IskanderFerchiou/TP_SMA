package v2;

public enum NegotiationStrat {
    // plus le ticket arrive à sa date maximale de vente plus le vendeur va essayer
    // de diminuer son prix pour qu’il soit vendu le plus rapidement possible et au
    // contraire s’il reste beaucoup de temps il va essayer de maximiser son gain.
    DEFAULT,
    REMAINING_TIME,
    TICKETS_SIMILARITY
}

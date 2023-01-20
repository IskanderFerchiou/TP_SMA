public enum NegotiationStrat {
    // plus la date limite de vente du ticket approche, plus le vendeur va essayer
    // de diminuer son prix pour qu’il soit vendu le plus rapidement possible. Inveserment,
    // s’il reste beaucoup de temps, le vendeur va essayer de maximiser son gain.
    DEFAULT,
    REMAINING_TIME,
    TICKETS_SIMILARITY
}

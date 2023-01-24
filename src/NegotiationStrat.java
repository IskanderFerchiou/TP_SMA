public enum NegotiationStrat {
    // plus la date limite de vente du ticket approche, plus le vendeur va essayer
    // de diminuer le prix pour que le ticket soit vendu le plus rapidement possible.
    // Inversement, s’il reste beaucoup de temps, le vendeur va essayer de maximiser son gain.
    DEFAULT,
    REMAINING_TIME,
    TICKETS_SIMILARITY
}

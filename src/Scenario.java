public enum Scenario {
    ChangesOnDatesN_1("ChangesOnDates_N-1"),
    ChangesOnPricesN_1("ChangesOnPrices_N-1"),
    ChangesOnPrices1_N("ChangesOnPrices_1-N"),
    NotAccordingPrices1_1("NotAccordingPrices_1-1"),
    NotEnoughTicketsN_1("NotEnoughTickets_N-1"),
    PreferedProvidersN_N("PreferedProviders_N-N"),
    RejectedProvidersN_N("RejectedProviders_N-N");

    private final String fileName;

    Scenario(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}

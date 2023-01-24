import java.time.LocalDate;

public class Offer {
    private final LocalDate offerDate;
    private final Ticket ticket;
    private Response response;
    private final int price;

    // numéro de l'offre envoyé pendant une négociation
    private final int offerNumber;

    public Offer(Ticket ticket, int price, LocalDate date, int offerNumber) {
        this.offerDate = date;
        this.ticket = ticket;
        this.price = price;
        this.offerNumber = offerNumber;
    }

    public LocalDate getOfferDate() {
        return offerDate;
    }

    public Ticket getTicket() {
        return ticket;
    }


    public int getPrice() {
        return price;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public int getOfferNumber() {
        return offerNumber;
    }

    @Override
    public String toString() {
        return "Offre (Prix : " + price +
                ", Date : " + Utils.formatDate(offerDate) + ") -> " +
                " Réponse : " + response ;
    }


}

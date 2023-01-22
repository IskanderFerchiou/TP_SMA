import java.time.LocalDate;
import java.util.Date;

public class Offer {
    private Provider provider;
    private Buyer buyer;
    private LocalDate offerDate;
    private Ticket ticket;

    private Response response;

    private int price;

    // numéro de l'offre envoyé pendant une négociation
    private int offerNumber;

    public Offer(Provider provider, Buyer buyer, Ticket ticket, int price, LocalDate date, int offerNumber) {
        this.provider = provider;
        this.buyer = buyer;
        this.offerDate = date;
        this.ticket = ticket;
        this.price = price;
        this.offerNumber = offerNumber;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Buyer getBuyer() {
        return buyer;
    }

    public void setBuyer(Buyer buyer) {
        this.buyer = buyer;
    }

    public LocalDate getOfferDate() {
        return offerDate;
    }

    public void setOfferDate(LocalDate offerDate) {
        this.offerDate = offerDate;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public int getOfferNumber() {
        return offerNumber;
    }

    public void setOfferNumber(int offerNumber) {
        this.offerNumber = offerNumber;
    }

    @Override
    public String toString() {
        return "Offre (Prix : " + price +
                ", Date : " + Utils.formatDate(offerDate) + ") -> " +
                " Réponse : " + response ;
    }


}

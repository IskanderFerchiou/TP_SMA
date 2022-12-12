import java.util.Date;

public class Offer {
    private Provider provider;
    private Buyer buyer;
    private Date offerDate;
    private Ticket ticket;

    private Response response;

    private int price;

    public Offer(Provider provider, Buyer buyer, Ticket ticket, int price) {
        this.provider = provider;
        this.buyer = buyer;
        this.offerDate = new Date();
        this.ticket = ticket;
        this.price = price;
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

    public Date getOfferDate() {
        return offerDate;
    }

    public void setOfferDate(Date offerDate) {
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
}

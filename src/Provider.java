import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Provider extends Agent {
    private List<Ticket> tickets; // les billets à vendre
    private List<Buyer> buyers;

    private List<Offer> offers; // historique des négociations


    public Provider() {
        this.tickets = new ArrayList<>();
        this.offers = new ArrayList<>();
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void addTicket (Ticket ticket) {
        this.tickets.add(ticket);
        //ticket.setProvider(this);
    }

    public void removeTicket (Ticket ticket) {
        this.tickets.remove(ticket);
    }

    public List<Offer> getOffers() {
        return this.offers;
    }

    public void addOffer (Offer offer) {
        this.offers.add(offer);
    }

    public void removeOffer (Offer offer) {
        this.offers.remove(offer);
    }

    public Response checkConstraint (Offer offer) {
        Ticket ticket = offer.getTicket();
        if(offer.getPrice() < ticket.getMinimumProvidingPrice())
            return Response.PRICE_TOO_LOW;
        if (offer.getOfferDate().after(ticket.getLatestProvidingDate()))
            return Response.DATE_TOO_LATE;
        return Response.VALID_CONSTRAINTS;
    }
}

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
        if (offer.getOfferDate().equals(ticket.getLatestProvidingDate()))
            return Response.DATE_TOO_LATE;
        if (offer.getPrice() < ticket.getMinimumProvidingPrice())
            return Response.PRICE_TOO_LOW;
        return Response.VALID_CONSTRAINTS;
    }

    public int calculatePrice(Ticket ticket) {
        int providerPrice;
        // Première offre : le fournisseur fait une offre avec son prix de vente souhaité
        if (this.getOffers().size() == 0) {
            providerPrice = ticket.getPreferedProvidingPrice();
            // A partir de la deuxième offre : on diminue le prix en fonction de la dernière offre du fournisseur
        } else {
            Offer lastProviderOffer = this.getOffers().get(this.getOffers().size()-1);

            // si le nouveau prix calculé est en dessous du prix de vente minimum, on prend le prix de vente minimum
            providerPrice = lastProviderOffer.getPrice() - (int)(lastProviderOffer.getPrice() * 0.1);
            if (providerPrice < ticket.getMinimumProvidingPrice()) {
                providerPrice = ticket.getMinimumProvidingPrice();
            }
        }
        return providerPrice;
    }
}

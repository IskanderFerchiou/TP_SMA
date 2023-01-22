import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class Provider extends Agent {
    private final List<Ticket> tickets; // les billets à vendre
    private NegotiationStrat strat;
    private final Integer id;

    public Provider(Integer id, BlockingQueue<Ticket> catalogue, NegotiationStrat strat) {
        super();
        this.id = id;
        this.tickets = new ArrayList<>();
        this.catalogue = catalogue;
        this.strat = strat;
    }

    public Integer getId() {
        return id;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void addTicket (Ticket ticket) {
        this.tickets.add(ticket);
    }

    public void removeTicket (Ticket ticket) {
        this.tickets.remove(ticket);
    }

    public void sellTicket(Ticket ticket) {
        ticket.setAvailable(false);
        catalogue.remove(ticket);
    }

    public Response checkConstraint(Offer offer) {
        Ticket ticket = offer.getTicket();
        if (offer.getOfferDate().equals(ticket.getLatestProvidingDate())) {
            return Response.DATE_TOO_LATE;
        }
        if (offer.getPrice() < ticket.getMinimumProvidingPrice() || offer.getOfferNumber() < 3){
            return Response.LOW_PROPOSAL;
        }
        return Response.VALID_CONSTRAINTS;
    }

    public NegotiationStrat getStrat() {
        return strat;
    }

    public void setStrat(NegotiationStrat strat){
        this.strat = strat;
    }

    public int calculatePrice(Negotiation negotiation) {
        int providerPrice;
        List<Offer> history = this.getOffers(negotiation);
        Ticket ticket = negotiation.getTicket();

        // marge de négociation divisé par 5 pour temporiser la négociation
        double coefNegotiation = ((double)(ticket.getPreferedProvidingPrice() - ticket.getMinimumProvidingPrice()) / ticket.getPreferedProvidingPrice()) / 5;

        // si le fournisseur n'a toujours pas fait de contre-offre
        if (history.size() <= 1) {
            providerPrice = ticket.getPreferedProvidingPrice() - (int)(ticket.getPreferedProvidingPrice() * coefNegotiation);
        // A partir de la deuxième offre : on diminue le prix en fonction de la dernière offre du fournisseur et de la stratégie
        } else {
            Offer lastSentOffer = history.get(history.size() - 2);

            if (this.strat == NegotiationStrat.REMAINING_TIME && ticket.getRemainingDays(Timer.getDate()) < 2) {
                providerPrice = lastSentOffer.getPrice() - (int)(lastSentOffer.getPrice() * (coefNegotiation+0.03));
            } else if (this.strat == NegotiationStrat.TICKETS_SIMILARITY && findSimilarTickets(ticket).size() > 3) {
                providerPrice = lastSentOffer.getPrice() - (int)(lastSentOffer.getPrice() * (coefNegotiation+0.03));
            } else {
                providerPrice = lastSentOffer.getPrice() - (int)(lastSentOffer.getPrice() * coefNegotiation);
            }
        }

        // si le nouveau prix calculé est en dessous du prix de vente minimum, on prend le prix de vente minimum
        if (providerPrice < ticket.getMinimumProvidingPrice()) {
            providerPrice = ticket.getMinimumProvidingPrice();
        }

        return providerPrice;
    }

    @Override
    public void run() {
        catalogue.addAll(tickets);
    }
}
package v2;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class Provider extends Agent {
    private final List<Ticket> tickets; // les billets à vendre
    private List<Buyer> buyers;
    private HashMap<Ticket, Offer> bestOffers;

    private NegotiationStrat strat;

    private Integer id;


    public Provider() {
        this.tickets = new ArrayList<>();
    }

    public Provider(Integer id, BlockingQueue<Ticket> catalogue, NegotiationStrat strat) {
        super();
        this.id = id;
        this.tickets = new ArrayList<>();
        this.catalogue = catalogue;
        this.bestOffers = new HashMap<>();
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
        //ticket.setProvider(this);
    }

    public void removeTicket (Ticket ticket) {
        this.tickets.remove(ticket);
    }

    public void sellTicket(Ticket ticket) {
        ticket.setAvailable(false);
        catalogue.remove(ticket);
    }

    public Response checkConstraint (Offer offer) {
        Ticket ticket = offer.getTicket();
        Offer bestOffer = this.bestOffers.get(ticket);
        if (bestOffer == null || bestOffer.getPrice() <= offer.getPrice()) {
            this.bestOffers.put(ticket, offer);
        }
        if (offer.getOfferDate().equals(ticket.getLatestProvidingDate())) {
            System.out.println("Dernier jour de vente (" + ticket.getLatestProvidingDate() + ") terminé.");
            return Response.DATE_TOO_LATE;
        }
        if (offer.getPrice() < ticket.getMinimumProvidingPrice()){
            return Response.PRICE_TOO_LOW;
        }
        return Response.VALID_CONSTRAINTS;
    }

    public NegotiationStrat getStrat() {
        return strat;
    }

    public void setStrat(NegotiationStrat strat){
        this.strat = strat;
    }

    public int calculatePrice(Ticket ticket, Date actualDate) {
        int providerPrice = 0;
        Offer bestOffer = this.bestOffers.get(ticket);
        // Première offre : le fournisseur fait une offre avec son prix de vente souhaité
        if (bestOffer == null) {
            providerPrice = ticket.getPreferedProvidingPrice();
        } else {
            if(this.strat == NegotiationStrat.DEFAULT){
                // si le nouveau prix calculé est en dessous du prix de vente minimum, on prend le prix de vente minimum
                providerPrice = bestOffer.getPrice() + (int)(bestOffer.getPrice() * 0.1);

            } else if (this.strat == NegotiationStrat.REMAINING_TIME){
                if(ticket.getRemainingDays(actualDate) < 5) {
                    providerPrice = bestOffer.getPrice()+ (int)(bestOffer.getPrice() * 0.07);
                } else {
                    providerPrice = bestOffer.getPrice()+ (int)(bestOffer.getPrice() * 0.1);
                }
            } else if (this.strat == NegotiationStrat.TICKETS_SIMILARITY){
                if(findSimilarTickets(ticket).size() > 3) {
                    providerPrice = bestOffer.getPrice()+ (int)(bestOffer.getPrice() * 0.07);
                } else {
                    providerPrice = bestOffer.getPrice()+ (int)(bestOffer.getPrice() * 0.1);
                }
            }
            if (providerPrice < ticket.getMinimumProvidingPrice()) {
                providerPrice = ticket.getMinimumProvidingPrice();
            }
        }
        // A partir de la deuxième offre : on diminue le prix en fonction de la meilleur offre d'un acheteur sur le ticket
        return providerPrice;
    }

    @Override
    public void run() {
        // Initialisation des billets
//        Ticket ticket1 = new Ticket(
//                this,
//                "Paris",
//                "Tokyo",
//                1000,
//                800,
//                new Date(2022, 11, 7),
//                new Date(2022, 11, 10)
//        );

//        try {
            // catalogue.put(ticket1);
            catalogue.addAll(tickets);
            // System.out.println("Ajout du ticket :" + ticket1);
//        } catch(InterruptedException e) {
//            e.printStackTrace();
//        }
    }
}

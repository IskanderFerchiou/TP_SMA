package v2;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class Provider extends Agent {
    private final List<Ticket> tickets; // les billets à vendre
    private List<Buyer> buyers;
    private BlockingQueue<Ticket> catalogue;


    public Provider() {
        this.tickets = new ArrayList<>();
    }

    public Provider(BlockingQueue<Ticket> catalogue) {
        super();
        this.tickets = new ArrayList<>();
        this.catalogue = catalogue;
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
    }

    public Response checkConstraint (Offer offer) {
        Ticket ticket = offer.getTicket();
        if (offer.getOfferDate().equals(ticket.getLatestProvidingDate())) {
            System.out.println("Dernier jour de vente (" + ticket.getLatestProvidingDate() + ") terminé.");
            return Response.DATE_TOO_LATE;
        }
        if (offer.getPrice() < ticket.getMinimumProvidingPrice()){
            return Response.PRICE_TOO_LOW;
        }
        System.out.println("Le vendeur a vendu le ticket.");
        return Response.VALID_CONSTRAINTS;
    }

    public int calculatePrice(Ticket ticket) {
        int providerPrice;
        // Première offre : le fournisseur fait une offre avec son prix de vente souhaité
        if (this.getOffers().size() == 0) {
            providerPrice = ticket.getPreferedProvidingPrice();
            // A partir de la deuxième offre : on diminue le prix en fonction de la dernière offre de l'acheteur
        } else {
            Offer lastBuyerOffer = this.getOffers().get(this.getOffers().size()-1);

            // si le nouveau prix calculé est en dessous du prix de vente minimum, on prend le prix de vente minimum
            providerPrice = lastBuyerOffer.getPrice() + (int)(lastBuyerOffer.getPrice() * 0.1);
            if (providerPrice < ticket.getMinimumProvidingPrice()) {
                providerPrice = ticket.getMinimumProvidingPrice();
            }
        }
        return providerPrice;
    }

    @Override
    public void run() {
        // Initialisation des billets
        Ticket ticket1 = new Ticket(
                this,
                "Paris",
                "Tokyo",
                1000,
                800,
                new Date(2022, 11, 7),
                new Date(2022, 11, 10)
        );

        try {
            catalogue.put(ticket1);
            System.out.println("Ajout du ticket :" + ticket1);

            // TODO: remplacer ticket1 par la liste des tickets => le provider existe tant que ses propres tickets sont en vente
            while(catalogue.contains(ticket1)) {
                catalogue.removeIf(t -> !t.isAvailable());
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
}

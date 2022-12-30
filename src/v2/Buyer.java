package v2;

import java.util.*;
import java.util.concurrent.BlockingQueue;

public class Buyer extends Agent {
    private List<Provider> providers; // les acheteurs connaissent tous les fournisseur
    private String destination;

    // contraintes
    private int maximumBudget;
    private Date latestBuyingDate;
    private List<Provider> rejectedProviders;

    // preferences
    private Date preferedBuyingDate;
    private List<Provider> preferredProviders;

    private BlockingQueue<Ticket> catalogue;

    private Date actualDate;

    private HashMap<Ticket, Offer> bestOffers;


    public Buyer(String destination, int maximumBudget, Date latestBuyingDate, Date preferedBuyingDate) {
        this.destination = destination;
        this.maximumBudget = maximumBudget;
        this.latestBuyingDate = latestBuyingDate;
        this.preferedBuyingDate = preferedBuyingDate;
        this.providers = new ArrayList<>();
        this.rejectedProviders = new ArrayList<>();
        this.preferredProviders = new ArrayList<>();
    }

    public Buyer(String destination, int maximumBudget, Date latestBuyingDate, Date preferedBuyingDate, BlockingQueue<Ticket> catalogue, Date actualDate) {
        super();
        this.destination = destination;
        this.maximumBudget = maximumBudget;
        this.latestBuyingDate = latestBuyingDate;
        this.preferedBuyingDate = preferedBuyingDate;
        this.providers = new ArrayList<>();
        this.rejectedProviders = new ArrayList<>();
        this.preferredProviders = new ArrayList<>();
        this.catalogue = catalogue;
        this.actualDate = actualDate;
        this.bestOffers = new HashMap<>();
    }


    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getMaximumBudget() {
        return maximumBudget;
    }

    public void setMaximumBudget(int maximumBudget) {
        this.maximumBudget = maximumBudget;
    }

    public Date getLatestBuyingDate() {
        return latestBuyingDate;
    }

    public void setLatestBuyingDate(Date latestBuyingDate) {
        this.latestBuyingDate = latestBuyingDate;
    }

    public Date getPreferedBuyingDate() {
        return preferedBuyingDate;
    }

    public void setPreferedBuyingDate(Date preferedBuyingDate) {
        this.preferedBuyingDate = preferedBuyingDate;
    }

    public List<Provider> getProviders() {
        return this.providers;
    }

    public void addProvider(Provider provider) {
        this.providers.add(provider);
    }

    public void removeProvider(Provider provider) {
        this.providers.remove(provider);
    }

    public List<Provider> getRejectedProviders() {
        return this.rejectedProviders;
    }

    public void addRejectedProvider(Provider provider) {
        this.rejectedProviders.add(provider);
    }

    public void removeRejectedProvider(Provider provider) {
        this.rejectedProviders.remove(provider);
    }

    public List<Provider> getPreferredProviders() {
        return this.preferredProviders;
    }

    public void addPreferredProvider(Provider provider) {
        this.preferredProviders.add(provider);
    }

    public void removePreferredProvider(Provider provider) {
        this.preferredProviders.remove(provider);
    }

    public Response checkConstraint(Offer offer) {
        Ticket ticket = offer.getTicket();
        Offer bestOffer = this.bestOffers.get(ticket);
        if (bestOffer == null || bestOffer.getPrice() >= offer.getPrice()) {
            this.bestOffers.put(ticket, offer);
        }
        if (rejectedProviders.contains(offer.getProvider()))
            return Response.PROVIDER_REJECTED;
        if (offer.getOfferDate().after(this.latestBuyingDate)) {
            System.out.println("Dernière date d'achat maximum pour l'acheteur écoulée (" + this.getLatestBuyingDate() + ")");
            return Response.DATE_TOO_LATE;
        }
        if (offer.getPrice() > this.maximumBudget)
            return Response.BUDGET_NOT_ENOUGH;

        return Response.VALID_CONSTRAINTS;
    }

    public Response checkConstraint(Ticket ticket) {
        if (rejectedProviders.contains(ticket.getProvider()))
            return Response.PROVIDER_REJECTED;
        if (ticket.getPreferedProvidingDate().after(this.latestBuyingDate)) {
            System.out.println("La date de mise en vente du ticket dépasse la dernière date d'achat possible (" + this.getLatestBuyingDate() + ")");
            return Response.DATE_TOO_LATE;
        }
        if (ticket.getPreferedProvidingPrice() > this.maximumBudget)
            return Response.BUDGET_NOT_ENOUGH;

        return Response.VALID_CONSTRAINTS;
    }
    
    public int calculatePrice(Ticket ticket) {
        int buyerPrice;
        // Première offre de l'acheteur : il propose en fonction de son budget maximum
        if (this.getOffers().size() == 0) {
            buyerPrice = this.maximumBudget - (int) (0.2 * this.maximumBudget);
            // A partir de la deuxième offre, il regarde la réponse du fournisseur et augmente le prix sans dépasser son budget
        } else {
            Offer bestOffer = this.bestOffers.get(ticket);

            // si le nouveau prix calculé est au dessus du budget maximum, l'acheteur négocie
            buyerPrice = bestOffer.getPrice() - (int) (bestOffer.getPrice() * 0.1);
            if (buyerPrice > this.maximumBudget) {
                buyerPrice = this.maximumBudget;
            }
        }
        return buyerPrice;
    }

    @Override
    public void run() {
        boolean available = true; // condition d'arrêt
        List<Negotiation> negotiations = new ArrayList<>();
        List<Ticket> seenTickets = new ArrayList<>();
        List<Ticket> addedTickets;

        while (available) {
            // Si la date actuelle dépasse la date limite, l'acheteur n'est plus disponible
            if (this.actualDate.after(this.latestBuyingDate)) {
                System.out.println("La date actuelle a dépassé la date limite.");
                available = false;
                break;
            }

            // on analyse uniquement les nouveaux tickets du catalogue
            addedTickets = new ArrayList<>(catalogue.stream().toList());
            addedTickets.removeAll(seenTickets);

            for (Ticket ticket : addedTickets) {
                Response buyerResponse = this.checkConstraint(ticket);

                seenTickets.add(ticket);
                boolean startNegotiation = buyerResponse == Response.BUDGET_NOT_ENOUGH;
                // on commence les négociations si la contrainte est uniquement lié au prix
                if (startNegotiation) {
                    Negotiation nego = new Negotiation(ticket, this, ticket.getProvider());
                    negotiations.add(nego);
                    Thread negotiationThread = new Thread(nego);
                    negotiationThread.start();
                }
            }

            // on vérifie l'état des négociations en cours
            for (Iterator<Negotiation> iterator = negotiations.iterator(); iterator.hasNext(); ) {
                Negotiation n = iterator.next();
                // si une négociation a échoué ou aboutit
                if (n.getStatus() != NegotiationStatus.RUNNING) {
                    // on prend uniquement la date de la négociation si elle est plus grande
                    if (n.getFinalDate().after(this.actualDate)) {
                        this.actualDate = n.getFinalDate();
                    }
                    if (n.getStatus() == NegotiationStatus.SUCCESS) {
                        // on arrête la recherche de ticket si un achat a été effectué
                        available = false;
                        break;
                    }
                    // on retire la negociation de la liste des négociations en cours.
                    iterator.remove();
                }
            }


            // Si aucune négociations n'est en cours et qu'aucun nouveau ticket n'a été ajouté, on passe au jour suivant
            if (negotiations.isEmpty() && addedTickets.isEmpty()) {
                this.actualDate = Utils.nextDay(this.actualDate);
                System.out.println(Thread.currentThread().getName() + " : pas de ticket sur le marché, J+1 => " + this.actualDate);
            }
        }
    }
}

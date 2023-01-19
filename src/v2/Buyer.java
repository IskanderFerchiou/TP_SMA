package v2;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class Buyer extends Agent {
    private final List<Provider> providers; // les acheteurs connaissent tous les fournisseur
    private String destination;

    private String name;

    // contraintes
    private int maximumBudget;
    private Date latestBuyingDate;
    private final List<Provider> rejectedProviders;

    // preferences
    private Date preferedBuyingDate;
    private final List<Provider> preferredProviders;

    private final BlockingQueue<Ticket> catalogue;

    private Date actualDate;

    private final HashMap<Ticket, Offer> bestOffers;

    private final AtomicBoolean available;

    private CountDownLatch latch;


    public Buyer(String name, String destination, int maximumBudget, Date latestBuyingDate, Date preferedBuyingDate, BlockingQueue<Ticket> catalogue, Date actualDate, CountDownLatch latch) {
        super();
        this.name = name;
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
        this.available = new AtomicBoolean(true);
        this.latch = latch;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAvailable() {
        return available.get();
    }

    public void setAvailable(boolean available) {
        this.available.set(available);
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
        if (!ticket.getArrivalPlace().equals(this.destination))
            return Response.WRONG_DESTINATION;
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
        Offer bestOffer = this.bestOffers.get(ticket);
        // Première offre de l'acheteur : il propose en fonction de son budget maximum
        if (bestOffer == null) {
            buyerPrice = this.maximumBudget - (int) (0.2 * this.maximumBudget);
            // A partir de la deuxième offre, il regarde la réponse du fournisseur et augmente le prix sans dépasser son budget
        } else {

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
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        List<Ticket> seenTickets = new ArrayList<>();
        List<Ticket> addedTickets;
        List<Negotiation> negotiations = new ArrayList<>();

        while (isAvailable()) {
            // Si la date actuelle dépasse la date limite, l'acheteur n'est plus disponible
            if (this.actualDate.after(this.latestBuyingDate)) {
                System.out.println("Recherche de ticket (" + this.getName() + ") : la date actuelle (" + Utils.formatDate(this.actualDate) + ") a dépassé la date limite.");
                setAvailable(false);
                break;
            }

            // on analyse uniquement les nouveaux tickets du catalogue
            addedTickets = new ArrayList<>(catalogue.stream().toList());
            addedTickets.removeAll(seenTickets);

            for (Ticket ticket : addedTickets) {
                Response buyerResponse = this.checkConstraint(ticket);

                seenTickets.add(ticket);
                boolean startNegotiation = buyerResponse == Response.BUDGET_NOT_ENOUGH || buyerResponse == Response.VALID_CONSTRAINTS;
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
                // si une négociation a échoué
                if (n.getStatus() != NegotiationStatus.RUNNING) {
                    // on prend uniquement la date de la négociation si elle est plus grande
                    if (n.getFinalDate().after(this.actualDate)) {
                        this.actualDate = n.getFinalDate();
                    }
                    // on retire la negociation de la liste des négociations en cours.
                    iterator.remove();
                }
            }

            // Si aucune négociations n'est en cours et qu'aucun nouveau ticket n'a été ajouté, on passe au jour suivant
            if (negotiations.isEmpty() && addedTickets.isEmpty()) {
                this.actualDate = Utils.nextDay(this.actualDate);
                // System.out.println(Thread.currentThread().getName() + " : pas de ticket sur le marché, J+1 => " + Utils.formatDate(this.actualDate));
            }
        }
    }
}
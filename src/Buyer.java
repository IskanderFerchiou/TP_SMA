import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Buyer extends Agent {
    private final List<Provider> providers; // les acheteurs connaissent tous les fournisseur
    private String destination;
    private String name;

    private Integer maximumNumberOfOffers;
    
    // contraintes
    private int maximumBudget;
    private LocalDate latestBuyingDate;
    private final List<Integer> rejectedProvidersID;

    // preferences
    private LocalDate preferedBuyingDate;
    private final List<Integer> preferredProvidersID;

    private final AtomicBoolean available;

    public Buyer(String name, String destination, int maximumBudget, LocalDate latestBuyingDate, LocalDate preferedBuyingDate,
                 BlockingQueue<Ticket> catalogue, Integer maximumNumberOfOffers, NegotiationStrat strat) {
        super();
        this.name = name;
        this.destination = destination;
        this.maximumBudget = maximumBudget;
        this.latestBuyingDate = latestBuyingDate;
        this.preferedBuyingDate = preferedBuyingDate;
        this.providers = new ArrayList<>();
        this.rejectedProvidersID = new ArrayList<>();
        this.preferredProvidersID = new ArrayList<>();
        this.catalogue = catalogue;
        this.available = new AtomicBoolean(true);
        this.maximumNumberOfOffers = maximumNumberOfOffers;
        this.strat = strat;
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

    public LocalDate getLatestBuyingDate() {
        return latestBuyingDate;
    }

    public void setLatestBuyingDate(LocalDate latestBuyingDate) {
        this.latestBuyingDate = latestBuyingDate;
    }

    public LocalDate getPreferedBuyingDate() {
        return preferedBuyingDate;
    }

    public void setPreferedBuyingDate(LocalDate preferedBuyingDate) {
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

    public List<Integer> getRejectedProvidersID() {
        return this.rejectedProvidersID;
    }

    public void addRejectedProviderID(Integer providerID) {
        this.rejectedProvidersID.add(providerID);
    }

    public void removeRejectedProviderID(Integer providerID) {
        this.rejectedProvidersID.remove(providerID);
    }

    public List<Integer> getPreferredProvidersID() {
        return this.preferredProvidersID;
    }

    public void addPreferredProviderID(Integer providerID) {
        this.preferredProvidersID.add(providerID);
    }

    public void removePreferredProviderID(Integer providerID) {
        this.preferredProvidersID.remove(providerID);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMaximumNumberOfOffers() {
        return maximumNumberOfOffers;
    }

    public void setMaximumNumberOfOffers(Integer maximumNumberOfOffers) {
        this.maximumNumberOfOffers = maximumNumberOfOffers;
    }

    @Override
    public String toString() {
        return name + " (Destination : " + destination +
                ", Budget maximum : " + maximumBudget + "" +
                ", Date limite d'achat : " + Utils.formatDate(latestBuyingDate) +
                ", Fournisseurs préférés : " + Arrays.toString(preferredProvidersID.toArray()) +
                ", Fournisseurs detestés : " + Arrays.toString(rejectedProvidersID.toArray()) + ")";
    }

    public synchronized boolean isAvailable() {
        return available.get();
    }

    public void setAvailable(boolean available) {
        this.available.set(available);
    }

    // vérification des contraintes DURANT la négociation
    public Response checkConstraint(Offer offer) {
        if (offer.getOfferDate().isAfter(this.latestBuyingDate)) {
            return Response.DATE_TOO_LATE;
        }
        if (offer.getPrice() > this.maximumBudget || (double) offer.getOfferNumber() / this.maximumNumberOfOffers < 0.33)
            return Response.KEEP_NEGOCIATING;
        return Response.VALID_CONSTRAINTS;
    }

    // vérrification des contraintes AVANT la négociation
    public Response checkConstraint(Ticket ticket) {
        if (!ticket.getArrivalPlace().equals(this.destination))
            return Response.WRONG_DESTINATION;
        if (rejectedProvidersID.contains(ticket.getProvider().getId()))
            return Response.PROVIDER_REJECTED;
        if (ticket.getPreferedProvidingDate().isAfter(this.latestBuyingDate)) {
            System.out.println("La date de mise en vente du ticket dépasse la dernière date d'achat possible (" + Utils.formatDate(this.getLatestBuyingDate()) + ")");
            return Response.DATE_TOO_LATE;
        }
        if (ticket.getPreferedProvidingPrice() > this.maximumBudget)
            return Response.KEEP_NEGOCIATING;

        return Response.VALID_CONSTRAINTS;
    }

    public int calculatePrice(Negotiation negotiation) {
        int buyerPrice;
        List<Offer> history = this.getOffers(negotiation);
        Ticket ticket = negotiation.getTicket();

        int min = Math.min(this.maximumBudget, ticket.getPreferedProvidingPrice());

        // si l'acheteur n'a toujours pas fait de contre-offre, il propose en fonction du minimum entre le budget et le prix du ticket
        if (history.size() <= 1) {
            buyerPrice = min - (int)(0.2 * min);
            // A partir de la deuxième offre : on augmente le prix en fonction de la dernière offre de l'acheteur
        } else {
            Offer lastSentOffer = history.get(history.size() - 2);

            // marge de négociation divisé par 5 pour temporiser la négociation
            double coefNegotiation = ((double)(min - lastSentOffer.getPrice()) / min) / 5;
            if(this.strat == NegotiationStrat.REMAINING_TIME && ticket.getRemainingDays(Timer.getDate()) < 2)
                buyerPrice = lastSentOffer.getPrice() + (int)(lastSentOffer.getPrice()*(coefNegotiation+0.03));
            else if(this.strat == NegotiationStrat.TICKETS_SIMILARITY && findSimilarTickets(ticket).size() > 3)
                buyerPrice = lastSentOffer.getPrice() + (int)(lastSentOffer.getPrice()*(coefNegotiation+0.03));
            else
                buyerPrice = lastSentOffer.getPrice() + (int)(lastSentOffer.getPrice() * coefNegotiation);

            // si la négociation va se terminer, le client tente le tout pour le tout et met son budget maximum
            if (lastSentOffer.getOfferNumber() == this.maximumNumberOfOffers - 1) {
                buyerPrice = min;
            }
        }


        // si le nouveau prix calculé est au dessus du budget maximum, on prend le budget maximum
        if (buyerPrice > this.maximumBudget) {
            buyerPrice = this.maximumBudget;
        }

        return buyerPrice;
    }

    @Override
    public void run() {
        List<Ticket> seenTickets = new ArrayList<>();
        List<Ticket> addedTickets;
        List<Negotiation> negotiations = new ArrayList<>();

        List<Ticket> preferredTickets;

        boolean preferredNegotiations = false;

        while (isAvailable()) {

            // Si la date actuelle dépasse la date limite, l'acheteur n'est plus disponible
            if (Timer.getDate().isAfter(this.latestBuyingDate)) {
                System.out.println("Recherche de ticket (" + this.getName() + ") : la date actuelle (" + Utils.formatDate(Timer.getDate()) + ") a dépassé la date limite d'achat (" + Utils.formatDate(this.latestBuyingDate) + ").");
                setAvailable(false);
                break;
            }

            // on analyse uniquement les nouveaux tickets du catalogue
            addedTickets = new ArrayList<>(catalogue.stream().toList());
            addedTickets.removeAll(seenTickets);

            // on vérifie si des providers préférées ont ajoutés des tickets au catalogue (on négocie d'abord avec eux)
            preferredTickets = addedTickets.stream().filter(ticket -> this.preferredProvidersID.contains(ticket.getProvider().getId())).toList();

            if(!preferredTickets.isEmpty()) {
                addedTickets = preferredTickets;
            }

            // avant de commencer de nouvelles négotiations, on vérifie qu'une négotiation avec un fournisseur préféré n'est pas en cours
            if (!preferredNegotiations) {
                for (Ticket ticket : addedTickets) {
                    Response buyerResponse = this.checkConstraint(ticket);

                    seenTickets.add(ticket);
                    boolean startNegotiation = buyerResponse == Response.KEEP_NEGOCIATING || buyerResponse == Response.VALID_CONSTRAINTS;
                    // on commence les négociations si la contrainte est uniquement lié au prix
                    if (startNegotiation) {
                        Negotiation nego = new Negotiation(ticket, this, ticket.getProvider());
                        negotiations.add(nego);

                        Thread negotiationThread = new Thread(nego);
                        negotiationThread.start();
                    }
                }
            }

            // on réinitialise cette variable avant de vérifier à nouveau si une négotiation avec un fournisseur préféré est en cours
            preferredNegotiations = false;

            // on vérifie l'état des négociations en cours
            for (Iterator<Negotiation> iterator = negotiations.iterator(); iterator.hasNext(); ) {
                Negotiation n = iterator.next();
                // si une négociation a échoué ou réussi
                if (n.getStatus() != NegotiationStatus.RUNNING) {
                    // on retire la negociation de la liste des négociations en cours.
                    iterator.remove();
                }

                // on vérifie si une négotiation avec un fournisseur préféré est en cours
                if (n.getStatus() == NegotiationStatus.RUNNING && this.preferredProvidersID.contains(n.getProvider().getId())) {
                    preferredNegotiations = true;
                }
            }
        }
    }
}

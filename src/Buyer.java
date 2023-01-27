import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Buyer extends Agent {
    private final String destination;
    private final String name;
    private final Integer maximumNumberOfOffers;
    private final List<Ticket> seenTickets;
    
    // contraintes
    private final int maximumBudget;
    private final LocalDate latestBuyingDate;
    private final List<Integer> rejectedProvidersID;

    // preferences
    private final List<Integer> preferredProvidersID;

    private final AtomicBoolean available;

    public Buyer(String name, String destination, int maximumBudget, LocalDate latestBuyingDate,
                 BlockingQueue<Ticket> catalogue, Integer maximumNumberOfOffers, NegotiationStrat strat) {
        super();
        this.name = name;
        this.destination = destination;
        this.maximumBudget = maximumBudget;
        this.latestBuyingDate = latestBuyingDate;
        this.rejectedProvidersID = new ArrayList<>();
        this.preferredProvidersID = new ArrayList<>();
        this.seenTickets = new ArrayList<>();
        this.catalogue = catalogue;
        this.available = new AtomicBoolean(true);
        this.maximumNumberOfOffers = maximumNumberOfOffers;
        this.strat = strat;
    }

    public LocalDate getLatestBuyingDate() {
        return latestBuyingDate;
    }

    public void addRejectedProviderID(Integer providerID) {
        this.rejectedProvidersID.add(providerID);
    }

    public void addPreferredProviderID(Integer providerID) {
        this.preferredProvidersID.add(providerID);
    }

    public String getName() {
        return name;
    }


    public Integer getMaximumNumberOfOffers() {
        return maximumNumberOfOffers;
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
    @Override
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
        if (ticket.getPreferredProvidingDate().isAfter(this.latestBuyingDate)) {
            System.out.println("La date de mise en vente du ticket dépasse la dernière date d'achat possible (" + Utils.formatDate(this.getLatestBuyingDate()) + ")");
            return Response.DATE_TOO_LATE;
        }
        if (ticket.getPreferredProvidingPrice() > this.maximumBudget)
            return Response.KEEP_NEGOCIATING;

        return Response.VALID_CONSTRAINTS;
    }

    @Override
    public int calculatePrice(Negotiation negotiation) {
        int buyerPrice;
        List<Offer> history = this.getOffers(negotiation);
        Ticket ticket = negotiation.getTicket();

        int min = Math.min(this.maximumBudget, ticket.getPreferredProvidingPrice());

        // si l'acheteur n'a toujours pas fait de contre-offre, il propose en fonction du minimum entre le budget et le prix du ticket
        if (history.size() <= 1) {
            buyerPrice = min - (int)(0.2 * min);
            // A partir de la deuxième offre : on augmente le prix en fonction de la dernière offre de l'acheteur
        } else {
            Offer lastSentOffer = history.get(history.size() - 2);

            // marge de négociation divisé par 4 pour temporiser la négociation
            double coefNegotiation = ((double)(min - history.get(0).getPrice()) / (4 * min));
            // l'acheteur propose plus pour acheter le ticket si la date de vente limite approche
            if(this.strat == NegotiationStrat.REMAINING_TIME && ticket.getRemainingDays(Timer.getDate()) < 2)
                buyerPrice = lastSentOffer.getPrice() + (int)(lastSentOffer.getPrice()*(coefNegotiation+0.03));
            // l'acheteur propose moins si des tickets similaires se trouvent sur le marché
            else if(this.strat == NegotiationStrat.TICKETS_SIMILARITY && findSimilarTickets(ticket).size() >= 1)
                buyerPrice = lastSentOffer.getPrice() + (int)(lastSentOffer.getPrice()*(coefNegotiation-0.03 == 0 ? coefNegotiation : coefNegotiation-0.03));
            else
                buyerPrice = lastSentOffer.getPrice() + (int)(lastSentOffer.getPrice() * coefNegotiation);
        }


        // si le nouveau prix calculé est au dessus du budget maximum, on prend le budget maximum
        if (buyerPrice > this.maximumBudget) {
            buyerPrice = this.maximumBudget;
        }

        return buyerPrice;
    }

    private List<Ticket> filterCatalogue() {
        // on analyse uniquement les nouveaux tickets du catalogue
        List<Ticket> addedTickets = new ArrayList<>(catalogue.stream().toList());
        addedTickets.removeAll(this.seenTickets);

        // on vérifie si des providers préférées ont ajoutés des tickets au catalogue (on négocie d'abord avec eux)
        List<Ticket> preferredTickets = addedTickets.stream().filter(ticket -> this.preferredProvidersID.contains(ticket.getProvider().getId())).toList();

        if(!preferredTickets.isEmpty()) {
            addedTickets = preferredTickets;
        }

        return addedTickets;
    }

    private boolean checkIfPreferredNegotiationRunning(Negotiation n) {
        return n.getStatus() == NegotiationStatus.RUNNING && this.preferredProvidersID.contains(n.getProvider().getId());
    }

    @Override
    public void run() {
        List<Negotiation> negotiations = new ArrayList<>();
        boolean preferredNegotiationsRunning = false;

        while (isAvailable()) {

            LocalDate currentDate = Timer.getDate();

            // Si la date actuelle dépasse la date limite, l'acheteur n'est plus disponible
            if (currentDate.isAfter(this.latestBuyingDate)) {
                System.out.println("Recherche de ticket (" + this.getName() + ") : la date actuelle (" + Utils.formatDate(Timer.getDate()) + ") a dépassé la date limite d'achat (" + Utils.formatDate(this.latestBuyingDate) + ").");
                setAvailable(false);
                break;
            }

            // on analyse et on filtre les nouveaux tickets du catalogue
            List<Ticket> filteredTickets = filterCatalogue();

            // avant de commencer de nouvelles négotiations, on vérifie qu'une négotiation avec un fournisseur préféré n'est pas en cours
            if (!preferredNegotiationsRunning) {
                for (Ticket ticket : filteredTickets) {
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

            // on vérifie si une négotiation avec un fournisseur préféré est en cours
            for (Negotiation n : negotiations) {
                preferredNegotiationsRunning = checkIfPreferredNegotiationRunning(n);
                if (preferredNegotiationsRunning) {
                    break;
                }
            }
        }
    }
}

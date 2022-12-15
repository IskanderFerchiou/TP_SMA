import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Buyer extends Agent {
    private List<Provider> providers; // les acheteurs connaissent tous les fournisseurs
    private List<Offer> offers; // historique des négociations
    private String destination;

    // contraintes
    private int maximumBudget;
    private Date latestBuyingDate;
    private List<Provider> rejectedProviders;

    // preferences
    private Date preferedBuyingDate;
    private List<Provider> preferredProviders;


    public Buyer (String destination, int maximumBudget, Date latestBuyingDate, Date preferedBuyingDate) {
        this.destination = destination;
        this.maximumBudget = maximumBudget;
        this.latestBuyingDate = latestBuyingDate;
        this.preferedBuyingDate = preferedBuyingDate;
        this.providers = new ArrayList<>();
        this.offers = new ArrayList<>();
        this.rejectedProviders = new ArrayList<>();
        this.preferredProviders = new ArrayList<>();
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

    public List<Offer> getOffers() {
        return this.offers;
    }

    public void addOffer(Offer offer) {
        this.offers.add(offer);
    }

    public void removeOffer(Offer offer) {
        this.offers.remove(offer);
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
        if(rejectedProviders.contains(offer.getProvider()))
            return Response.PROVIDER_REJECTED;
        if (offer.getOfferDate().after(this.latestBuyingDate))
            return Response.DATE_TOO_LATE;
        if(offer.getPrice() > this.maximumBudget)
            return Response.BUDGET_NOT_ENOUGH;

        return Response.VALID_CONSTRAINTS;
    }

    public int calculatePrice() {
        int buyerPrice;
        // Première offre de l'acheteur : il propose en fonction de son budget maximum
        if (this.getOffers().size() == 0) {
            buyerPrice = this.maximumBudget - (int)(0.2 * this.maximumBudget);
            // A partir de la deuxième offre, il regarde sa dernière proposition et augmente le prix sans dépasser son budget
        } else {
            Offer lastBuyerOffer = this.getOffers().get(this.getOffers().size()-1);

            // si le nouveau prix calculé est au dessus du budget maximum, l'acheteur négocie
            buyerPrice = lastBuyerOffer.getPrice() + (int)(lastBuyerOffer.getPrice() * 0.1);
            if (buyerPrice > this.maximumBudget) {
                buyerPrice = this.maximumBudget;
            }
        }
        return buyerPrice;
    }
}

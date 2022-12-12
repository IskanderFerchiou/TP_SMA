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
        if(offer.getPrice() > this.maximumBudget)
            return Response.BUDGET_NOT_ENOUGH;
        if (offer.getOfferDate().after(this.latestBuyingDate))
            return Response.DATE_TOO_LATE;
        if(rejectedProviders.contains(offer.getProvider()))
            return Response.PROVIDER_REJECTED;

        return Response.VALID_CONSTRAINTS;
    }
}

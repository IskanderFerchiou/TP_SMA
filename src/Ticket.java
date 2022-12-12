import java.util.Date;

public class Ticket {
    private Date departureDate;
    private Date arrivalDate;
    private String departurePlace;
    private String arrivalPlace;

    // contraintes et préférences du vendeur
    private int preferedProvidingPrice; // prix désiré de vente du vendeur
    private int minimumProvidingPrice; // prix minimum de vente du vendeur
    private Date preferedProvidingDate; // date de vente désirée du vendeur
    private Date latestProvidingDate; // date de vente au plus tard du vendeur

    public Ticket (Date departureDate, Date arrivalDate, String departurePlace, String arrivalPlace, float price, Provider provider) {
        this.departureDate = departureDate;
        this.arrivalDate = arrivalDate;
        this.departurePlace = departurePlace;
        this.arrivalPlace = arrivalPlace;
        this.preferedProvidingPrice = preferedProvidingPrice;
        this.minimumProvidingPrice = minimumProvidingPrice;
        this.preferedProvidingDate = preferedProvidingDate;
        this.latestProvidingDate = latestProvidingDate;
    }

    public Date getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(Date departureDate) {
        this.departureDate = departureDate;
    }

    public Date getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(Date arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public String getDeparturePlace() {
        return departurePlace;
    }

    public void setDeparturePlace(String departurePlace) {
        this.departurePlace = departurePlace;
    }

    public String getArrivalPlace() {
        return arrivalPlace;
    }

    public void setArrivalPlace(String arrivalPlace) {
        this.arrivalPlace = arrivalPlace;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public int getMinimumProvidingPrice() {
        return this.minimumProvidingPrice;
    }

    public void setMinimumProvidingPrice(int minimumProvidingPrice) {
        this.minimumProvidingPrice = minimumProvidingPrice;
    }
}

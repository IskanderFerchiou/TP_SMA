import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Ticket {
    private Date departureDate;
    private Date arrivalDate;
    private String departurePlace;
    private String arrivalPlace;
    private float price;
    private Provider provider;
    private List<Ticket> offerBuyerHistory;
    private List<Ticket> offerProviderHistory;

    public Ticket (Date departureDate, Date arrivalDate, String departurePlace, String arrivalPlace, float price, Provider provider) {
        this.departureDate = departureDate;
        this.arrivalDate = arrivalDate;
        this.departurePlace = departurePlace;
        this.arrivalPlace = arrivalPlace;
        this.price = price;
        this.provider = provider;
        this.offerBuyerHistory = new ArrayList<>();
        this.offerProviderHistory = new ArrayList<>();
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

    public void addOfferBuyer(Ticket ticket) {
        this.offerBuyerHistory.add(ticket);
    }

    public boolean removeOfferBuyer(Ticket ticket) {
        try {
            this.offerBuyerHistory.remove(ticket);
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    public void addOfferProvider(Ticket ticket) {
        this.offerProviderHistory.add(ticket);
    }

    public boolean removeOfferProvider(Ticket ticket) {
        try {
            this.offerProviderHistory.remove(ticket);
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }
}

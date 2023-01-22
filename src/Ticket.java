import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

public class Ticket {
    private Provider provider;

    private String departurePlace;
    private String arrivalPlace;

    // contraintes et préférences du vendeur
    private int preferedProvidingPrice; // prix désiré de vente du vendeur
    private int minimumProvidingPrice; // prix minimum de vente du vendeur
    private LocalDate preferedProvidingDate; // date de vente désirée du vendeur
    private LocalDate latestProvidingDate; // date de vente au plus tard du vendeur

    private final AtomicBoolean available  = new AtomicBoolean(true);

    public Ticket (Provider provider, String departurePlace, String arrivalPlace, int preferedProvidingPrice, int minimumProvidingPrice, LocalDate preferedProvidingDate, LocalDate latestProvidingDate) {
        this.provider = provider;
        this.departurePlace = departurePlace;
        this.arrivalPlace = arrivalPlace;
        this.preferedProvidingPrice = preferedProvidingPrice;
        this.minimumProvidingPrice = minimumProvidingPrice;
        this.preferedProvidingDate = preferedProvidingDate;
        this.latestProvidingDate = latestProvidingDate;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
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

    public int getPreferedProvidingPrice() {
        return this.preferedProvidingPrice;
    }

    public void setPreferedProvidingPrice(int preferedProvidingPrice) {
        this.preferedProvidingPrice = preferedProvidingPrice;
    }

    public int getMinimumProvidingPrice() {
        return this.minimumProvidingPrice;
    }

    public void setMinimumProvidingPrice(int minimumProvidingPrice) {
        this.minimumProvidingPrice = minimumProvidingPrice;
    }

    public LocalDate getPreferedProvidingDate() {
        return this.preferedProvidingDate;
    }

    public void setPreferedProvidingDate(LocalDate preferedProvidingDate) {
        this.preferedProvidingDate = preferedProvidingDate;
    }

    public LocalDate getLatestProvidingDate() {
        return this.latestProvidingDate;
    }

    public void setLatestProvidingDate(LocalDate latestProvidingDate) {
        this.latestProvidingDate = latestProvidingDate;
    }

    public synchronized boolean isNotAvailable() {
        return !this.available.get();
    }

    public void setAvailable(Boolean available) {
        this.available.set(available);
    }

    public int getRemainingDays(LocalDate actualDate) {
        return (int) actualDate.datesUntil(this.latestProvidingDate).count();
        //System.out.println("Remaining days : "+(int)ChronoUnit.DAYS.between(this.latestProvidingDate.toInstant(), actualDate.toInstant()));
         //(int)ChronoUnit.DAYS.between(actualDate, this.latestProvidingDate);
    }


    @Override
    public String toString() {
        return "Ticket (Fournisseur ID : " + provider.getId() +
                ", Destination : " + arrivalPlace +
                ", Prix de départ : " + preferedProvidingPrice +
                ", Prix minimum : " + minimumProvidingPrice +
                ", Date limite de vente : " +  Utils.formatDate(latestProvidingDate) + ")";
    }

    @Override
    public boolean equals(Object obj) {
        Ticket objTicket;
        if(obj instanceof Ticket) {
            objTicket = (Ticket)obj;
            if(objTicket.departurePlace != this.departurePlace) {
                return false;
            }
            if (objTicket.arrivalPlace != this.arrivalPlace) {
                return false;
            }
            return true;
        }
        return false;
    }
}

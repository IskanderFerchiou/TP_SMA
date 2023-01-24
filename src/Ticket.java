import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Ticket {
    private Provider provider;
    private final String departurePlace;
    private final String arrivalPlace;

    // contraintes et préférences du vendeur
    private final int preferedProvidingPrice; // prix désiré de vente du vendeur
    private final int minimumProvidingPrice; // prix minimum de vente du vendeur
    private final LocalDate preferedProvidingDate; // date de vente désirée du vendeur
    private final LocalDate latestProvidingDate; // date de vente au plus tard du vendeur

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

    public String getArrivalPlace() {
        return arrivalPlace;
    }

    public int getPreferedProvidingPrice() {
        return this.preferedProvidingPrice;
    }

    public int getMinimumProvidingPrice() {
        return this.minimumProvidingPrice;
    }

    public LocalDate getPreferedProvidingDate() {
        return this.preferedProvidingDate;
    }

    public LocalDate getLatestProvidingDate() {
        return this.latestProvidingDate;
    }

    public synchronized boolean isNotAvailable() {
        return !this.available.get();
    }

    public void setAvailable(Boolean available) {
        this.available.set(available);
    }

    public int getRemainingDays(LocalDate actualDate) {
        return (int)ChronoUnit.DAYS.between(actualDate, this.latestProvidingDate);
    }

    @Override
    public String toString() {
        return "Ticket (Fournisseur ID : " + provider.getId() +
                ", Destination : " + arrivalPlace +
                ", Prix de départ : " + preferedProvidingPrice +
                ", Prix minimum : " + minimumProvidingPrice +
                ", Date limite de vente : " +  Utils.formatDate(latestProvidingDate) + ")";
    }

    public boolean isSimilar(Object obj) {
        Ticket objTicket;
        if(obj instanceof Ticket) {
            objTicket = (Ticket) obj;
            return objTicket.departurePlace.equals(this.departurePlace) && objTicket.arrivalPlace.equals(this.arrivalPlace);
        }
        return false;
    }
}

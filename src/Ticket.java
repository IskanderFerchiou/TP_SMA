import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class Ticket {
    private final Provider provider;
    private final String departurePlace;
    private final String arrivalPlace;

    // contraintes et préférences du vendeur
    private final int preferredProvidingPrice; // prix désiré de vente du vendeur
    private final int minimumProvidingPrice; // prix minimum de vente du vendeur
    private final LocalDate preferredProvidingDate; // date de vente désirée du vendeur
    private final LocalDate latestProvidingDate; // date de vente au plus tard du vendeur

    private final AtomicBoolean available  = new AtomicBoolean(true);

    public Ticket (Provider provider, String departurePlace, String arrivalPlace, int preferredProvidingPrice, int minimumProvidingPrice, LocalDate preferredProvidingDate, LocalDate latestProvidingDate) {
        this.provider = provider;
        this.departurePlace = departurePlace;
        this.arrivalPlace = arrivalPlace;
        this.preferredProvidingPrice = preferredProvidingPrice;
        this.minimumProvidingPrice = minimumProvidingPrice;
        this.preferredProvidingDate = preferredProvidingDate;
        this.latestProvidingDate = latestProvidingDate;
    }

    public Provider getProvider() {
        return provider;
    }

    public String getArrivalPlace() {
        return arrivalPlace;
    }

    public int getPreferredProvidingPrice() {
        return this.preferredProvidingPrice;
    }

    public int getMinimumProvidingPrice() {
        return this.minimumProvidingPrice;
    }

    public LocalDate getPreferredProvidingDate() {
        return this.preferredProvidingDate;
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
                ", Prix de départ : " + preferredProvidingPrice +
                ", Prix minimum : " + minimumProvidingPrice +
                ", Date limite de vente : " +  Utils.formatDate(latestProvidingDate) + ")";
    }

    public boolean isSimilar(Object obj) {
        Ticket objTicket;
        if(obj instanceof Ticket) {
            objTicket = (Ticket) obj;
            return !Objects.equals(objTicket.provider.getId(), this.provider.getId())
                    && objTicket.departurePlace.equals(this.departurePlace)
                    && objTicket.arrivalPlace.equals(this.arrivalPlace);
        }
        return false;
    }
}

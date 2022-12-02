import java.util.ArrayList;
import java.util.List;

public class Agent {
    private List<Provider> providers;
    private List<Buyer> buyers;

    public Agent() {
        this.providers = new ArrayList<>();
        this.buyers = new ArrayList<>();
    }

    public void addProvider(Provider provider) {
        this.providers.add(provider);
    }

    public boolean removeProvider(Provider provider) {
        try {
            this.providers.remove(provider);
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    public void addBuyer(Buyer buyer) {
        this.buyers.add(buyer);
    }

    public boolean removeBuyer(Buyer buyer) {
        try {
            this.buyers.remove(buyer);
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    public boolean buyTicket(Buyer buyer) {
        //TODO
        //regarder les contraintes du buyer et trouver un ticket qui correspond (parcourir les providers, et parcourir les tickets pour chaque provider)
        //on peut supprimer le ticket de la liste du provider trouvé et le buyer de la liste si jamais on trouve un ticket qui correspond
        return true;
    }
}

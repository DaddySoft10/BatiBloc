package domaine;

public class ZoneOuverture extends Zone {

    public ZoneOuverture(double x, double y, double largeur, double hauteur, String typeForme) {
        super(x, y, largeur, hauteur, typeForme, "Ouverture");
    }

    public ZoneOuverture() {
        super();
        setTypeZone("Ouverture");
    }
}

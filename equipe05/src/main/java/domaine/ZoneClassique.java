package domaine;

public class ZoneClassique extends Zone {

    public ZoneClassique(double x, double y, double largeur, double hauteur, String typeForme) {
        super(x, y, largeur, hauteur, typeForme, "Classique");
    }

    public ZoneClassique() {
        super();
        setTypeZone("Classique");
    }
}

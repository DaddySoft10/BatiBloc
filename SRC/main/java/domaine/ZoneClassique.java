package domaine;

import java.io.Serializable;

public class ZoneClassique extends Zone implements Serializable {
    private static final String TYPE_ZONE = "CLASSIQUE";

    public ZoneClassique() {
        super();
    }

    public ZoneClassique(double x, double y, double largeur, double hauteur, TypeForme typeForme) {
        super(x, y, largeur, hauteur, typeForme);
    }

    public String getTypeZone() {
        return TYPE_ZONE;
    }
}

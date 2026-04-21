package domaine;

import java.io.Serializable;

public class ZoneOuverture extends Zone implements Serializable {

    public ZoneOuverture(double x, double y, double largeur, double hauteur, TypeForme typeForme) {
        super(x, y, largeur, hauteur, typeForme);
    }

    public String getTypeZone() {
        return "OUVERTURE";
    }
}

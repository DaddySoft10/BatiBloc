package domaine;

public class Rectangulaire extends Zone {

    public Rectangulaire(double x, double y, double largeur, double hauteur, String typeZone) {
        super(x, y, largeur, hauteur, "Rectangulaire", typeZone);
    }

    public double calculerAire() {
        return getLargeur() * getHauteur();
    }
}

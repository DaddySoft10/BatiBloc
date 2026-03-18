package domaine;

public class Triangulaire extends Zone {

    public Triangulaire(double x, double y, double largeur, double hauteur, String typeZone) {
        super(x, y, largeur, hauteur, "Triangulaire", typeZone);
    }

    public double calculerAire() {
        return (getLargeur() * getHauteur()) / 2.0;
    }
}

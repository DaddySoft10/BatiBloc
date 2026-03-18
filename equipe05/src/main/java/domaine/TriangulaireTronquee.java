package domaine;

public class TriangulaireTronquee extends Zone {
    private double largeurSommet;

    public TriangulaireTronquee(double x, double y, double largeur, double hauteur, String typeZone) {
        super(x, y, largeur, hauteur, "TriangulaireTronquee", typeZone);
        this.largeurSommet = largeur / 2.0;
    }

    public double getLargeurSommet() { return largeurSommet; }
    public void setLargeurSommet(double largeurSommet) { this.largeurSommet = largeurSommet; }

    public double calculerAire() {
        return ((getLargeur() + largeurSommet) / 2.0) * getHauteur();
    }
}

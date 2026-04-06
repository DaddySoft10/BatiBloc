package domaine;

public class Bloc {

    public static final double LARGEUR_STANDARD = 12.0;
    public static final double HAUTEUR_STANDARD = 8.0;

    private final double x;
    private final double y;
    private final double largeur;
    private final double hauteur;
    private final boolean estCoupe;

    public Bloc(double x, double y, double largeur,
                double hauteur, boolean estCoupe) {
        this.x = x;
        this.y = y;
        this.largeur = largeur;
        this.hauteur = hauteur;
        this.estCoupe = estCoupe;
    }

    public Bloc(double x, double y) {
        this(x, y, LARGEUR_STANDARD, HAUTEUR_STANDARD, false);
    }

    public double getX() { return this.x; }
    public double getY() { return this.y; }
    public double getLargeur() { return this.largeur; }
    public double getHauteur() { return this.hauteur; }
    public boolean isEstCoupe() { return this.estCoupe; }

    @Override
    public String toString() {
        return String.format(
            "Bloc[x=%.1f, y=%.1f, largeur=%.1f, hauteur=%.1f, coupe=%b]",
            this.x, this.y, this.largeur, this.hauteur, this.estCoupe);
    }
}

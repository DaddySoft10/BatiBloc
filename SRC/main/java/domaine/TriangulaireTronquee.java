package domaine;

public class TriangulaireTronquee extends Forme {
    private static final double RATIO_TRONQUATURE = 0.25;

    public TriangulaireTronquee() {
        super(TypeForme.TRIANGULAIRE_TRONQUEE);
    }

    @Override
    public boolean contientPoint(Zone zone, double x, double y) {
        if (zone == null) {
            throw new IllegalArgumentException("La zone ne peut pas etre nulle.");
        }

        double yMin = zone.getY();
        double yMax = zone.getY() + zone.getHauteur();
        if (y < yMin || y > yMax) {
            return false;
        }

        double largeur = zone.getLargeur();
        double retrait = largeur * RATIO_TRONQUATURE;
        double progressionVerticale = (y - yMin) / zone.getHauteur();
        double bordGauche = zone.getX() + retrait * (1.0 - progressionVerticale);
        double bordDroit = zone.getX() + largeur - retrait * (1.0 - progressionVerticale);

        return x >= bordGauche && x <= bordDroit;
    }
}

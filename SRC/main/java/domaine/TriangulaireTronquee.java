package domaine;

public class TriangulaireTronquee extends Forme {

    public TriangulaireTronquee() {
        super(TypeForme.TRIANGULAIRE_TRONQUEE);
    }

    @Override
    public boolean contientPoint(Zone zone, double x, double y) {
        if (zone == null) {
            throw new IllegalArgumentException("La zone ne peut pas etre nulle.");
        }

        double ratioCoupe = zone.getRatioCoupe();
        if (ratioCoupe <= 0.0) ratioCoupe = 0.5;

        double yMin = zone.getY();
        double yMax = zone.getY() + zone.getHauteur();
        double yCoupure = yMin + zone.getHauteur() * ratioCoupe;

        // La partie au-dessus de la coupure est vide
        if (y < yCoupure || y > yMax) {
            return false;
        }

        double largeur = zone.getLargeur();
        // Le retrait lateral correspond a la partie triangulaire non tronquee
        double retrait = largeur * (1.0 - ratioCoupe) / 2.0;
        double progressionVerticale = (y - yMin) / zone.getHauteur();
        double bordGauche = zone.getX() + retrait * (1.0 - progressionVerticale);
        double bordDroit = zone.getX() + largeur - retrait * (1.0 - progressionVerticale);

        return x >= bordGauche && x <= bordDroit;
    }
}

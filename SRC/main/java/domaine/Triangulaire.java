package domaine;

public class Triangulaire extends Forme {
    public Triangulaire() {
        super(TypeForme.TRIANGULAIRE);
    }

    @Override
    public boolean contientPoint(Zone zone, double x, double y) {
        if (zone == null) {
            throw new IllegalArgumentException("La zone ne peut pas etre nulle.");
        }

        double sommetX = zone.getX() + (zone.getLargeur() / 2.0);
        double sommetY = zone.getY();
        double baseGaucheX = zone.getX();
        double baseGaucheY = zone.getY() + zone.getHauteur();
        double baseDroiteX = zone.getX() + zone.getLargeur();
        double baseDroiteY = baseGaucheY;

        return aireTriangle(sommetX, sommetY, baseGaucheX, baseGaucheY, x, y)
                + aireTriangle(baseGaucheX, baseGaucheY, baseDroiteX, baseDroiteY, x, y)
                + aireTriangle(baseDroiteX, baseDroiteY, sommetX, sommetY, x, y)
                <= aireTriangle(sommetX, sommetY, baseGaucheX, baseGaucheY, baseDroiteX, baseDroiteY) + 0.0001;
    }

    private double aireTriangle(double x1, double y1, double x2, double y2, double x3, double y3) {
        return Math.abs((x1 * (y2 - y3) + x2 * (y3 - y1) + x3 * (y1 - y2)) / 2.0);
    }
}

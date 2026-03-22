package domaine;

public class Rectangulaire extends Forme {
    public Rectangulaire() {
        super(TypeForme.RECTANGULAIRE);
    }

    @Override
    public boolean contientPoint(Zone zone, double x, double y) {
        if (zone == null) {
            throw new IllegalArgumentException("La zone ne peut pas etre nulle.");
        }

        return x >= zone.getX()
                && x <= zone.getX() + zone.getLargeur()
                && y >= zone.getY()
                && y <= zone.getY() + zone.getHauteur();
    }
}

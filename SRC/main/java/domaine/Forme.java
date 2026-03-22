package domaine;

public abstract class Forme {
    private final TypeForme typeForme;

    protected Forme(TypeForme typeForme) {
        if (typeForme == null) {
            throw new IllegalArgumentException("Le type de forme ne peut pas etre nul.");
        }
        this.typeForme = typeForme;
    }

    public TypeForme getTypeForme() {
        return this.typeForme;
    }

    public abstract boolean contientPoint(Zone zone, double x, double y);

    public static Forme creerDepuis(TypeForme typeForme) {
        if (typeForme == null) {
            throw new IllegalArgumentException("Le type de forme ne peut pas etre nul.");
        }

        return switch (typeForme) {
            case RECTANGULAIRE -> new Rectangulaire();
            case TRIANGULAIRE -> new Triangulaire();
            case TRIANGULAIRE_TRONQUEE -> new TriangulaireTronquee();
        };
    }
}

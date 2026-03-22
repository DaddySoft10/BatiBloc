package domaine;

public class Zone {
    private double x;
    private double y;
    private double largeur;
    private double hauteur;
    private TypeForme typeForme;

    public Zone() {
        this(0.0, 0.0, 1.0, 1.0, TypeForme.RECTANGULAIRE);
    }

    public Zone(double x, double y, double largeur, double hauteur, TypeForme typeForme) {
        this.setX(x);
        this.setY(y);
        this.setLargeur(largeur);
        this.setHauteur(hauteur);
        this.setTypeForme(typeForme);
    }

    public double getX() {
        return this.x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return this.y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getLargeur() {
        return this.largeur;
    }

    public void setLargeur(double largeur) {
        if (largeur <= 0) {
            throw new IllegalArgumentException("La largeur doit etre superieure a 0.");
        }
        this.largeur = largeur;
    }

    public double getHauteur() {
        return this.hauteur;
    }

    public void setHauteur(double hauteur) {
        if (hauteur <= 0) {
            throw new IllegalArgumentException("La hauteur doit etre superieure a 0.");
        }
        this.hauteur = hauteur;
    }

    public TypeForme getTypeForme() {
        return this.typeForme;
    }

    public void setTypeForme(TypeForme typeForme) {
        if (typeForme == null) {
            throw new IllegalArgumentException("Le type de forme ne peut pas etre nul.");
        }
        this.typeForme = typeForme;
    }

    public Forme getForme() {
        return Forme.creerDepuis(this.typeForme);
    }

    public boolean contientPoint(double x, double y) {
        return this.getForme().contientPoint(this, x, y);
    }
}

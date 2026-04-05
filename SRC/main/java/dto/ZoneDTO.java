package dto;

public class ZoneDTO {
    private final double x;
    private final double y;
    private final double largeur;
    private final double hauteur;
    private final String typeForme;
    private final double ratioCoupe;
    private final String typeZone;

    public ZoneDTO(double x, double y, double largeur, double hauteur, String typeForme, String typeZone, double ratioCoupe) {
        this.x = x;
        this.y = y;
        this.largeur = largeur;
        this.hauteur = hauteur;
        this.typeForme = typeForme == null ? "" : typeForme;
        this.typeZone = typeZone == null ? "" : typeZone;
        this.ratioCoupe = ratioCoupe;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getLargeur() {
        return this.largeur;
    }

    public double getHauteur() {
        return this.hauteur;
    }

    public double getRatioCoupe() { return ratioCoupe; }

    public String getTypeForme() {
        return this.typeForme;
    }

    public String getTypeZone() {
        return this.typeZone;
    }
}

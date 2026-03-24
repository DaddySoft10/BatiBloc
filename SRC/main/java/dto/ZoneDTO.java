package dto;

public class ZoneDTO {
    private final double x;
    private final double y;
    private final double largeur;
    private final double hauteur;
    private final String typeForme;
    private final String typeZone;

    public ZoneDTO(double x, double y, double largeur, double hauteur, String typeForme, String typeZone) {
        this.x = x;
        this.y = y;
        this.largeur = largeur;
        this.hauteur = hauteur;
        this.typeForme = typeForme == null ? "" : typeForme;
        this.typeZone = typeZone == null ? "" : typeZone;
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

    public String getTypeForme() {
        return this.typeForme;
    }

    public String getTypeZone() {
        return this.typeZone;
    }
}

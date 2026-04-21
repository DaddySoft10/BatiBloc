package dto;

public class BlocPlaceDTO {
    private final double x;
    private final double y;
    private final double largeur;
    private final double hauteur;
    private final boolean retaille;

    public BlocPlaceDTO(double x, double y, double largeur, double hauteur, boolean retaille) {
        this.x = x;
        this.y = y;
        this.largeur = largeur;
        this.hauteur = hauteur;
        this.retaille = retaille;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getLargeur() {
        return largeur;
    }

    public double getHauteur() {
        return hauteur;
    }

    public boolean isRetaille() {
        return retaille;
    }
}

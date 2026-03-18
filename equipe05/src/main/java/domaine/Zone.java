package domaine;

public class Zone {
    private double x;
    private double y;
    private double largeur;
    private double hauteur;
    private String typeForme;
    private String typeZone;
    private boolean selectionnee = false;

    public Zone(double x, double y, double largeur, double hauteur, String typeForme, String typeZone) {
        this.x = x;
        this.y = y;
        this.largeur = largeur;
        this.hauteur = hauteur;
        this.typeForme = typeForme;
        this.typeZone = typeZone;
    }

    public Zone() {
    }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double getLargeur() { return largeur; }
    public void setLargeur(double largeur) { this.largeur = largeur; }

    public double getHauteur() { return hauteur; }
    public void setHauteur(double hauteur) { this.hauteur = hauteur; }

    public String getTypeForme() { return typeForme; }
    public void setTypeForme(String typeForme) { this.typeForme = typeForme; }

    public String getTypeZone() { return typeZone; }
    public void setTypeZone(String typeZone) { this.typeZone = typeZone; }

    public boolean isSelectionnee() { return selectionnee; }
    public void setSelectionnee(boolean selectionnee) { this.selectionnee = selectionnee; }

    public boolean contientPoint(double px, double py) {
        return px >= x && px <= x + largeur && py >= y && py <= y + hauteur;
    }

    public Zone copier() {
        Zone copie = new Zone(x, y, largeur, hauteur, typeForme, typeZone);
        copie.setSelectionnee(this.selectionnee);
        return copie;
    }

    @Override
    public String toString() {
        return "Zone{typeForme='" + typeForme + "', typeZone='" + typeZone +
               "', x=" + x + ", y=" + y + ", largeur=" + largeur + ", hauteur=" + hauteur +
               ", selectionnee=" + selectionnee + '}';
    }
}

package domaine;

public class ZoneBloc extends Zone {
    private static final String TYPE_ZONE = "BLOC";

    public ZoneBloc() {
        super();
    }

    public ZoneBloc(double x, double y, double largeur, double hauteur, TypeForme typeForme) {
        super(x, y, largeur, hauteur, typeForme);
    }

    public String getTypeZone() {
        return TYPE_ZONE;
    }
}

package domaine;

import java.util.ArrayList;
import java.util.List;

public final class SimulateurPlacement {
    public static final double BLOC_LARGEUR = 12.0;
    public static final double BLOC_HAUTEUR = 8.0;
    public static final double MIN_RETAILLE = 6.0;

    private SimulateurPlacement() {
    }

    public static List<ZoneBloc.BlocPlace> simuler(ZoneBloc zoneBloc) {
        if (zoneBloc == null) {
            throw new IllegalArgumentException("La zone de blocs ne peut pas etre nulle.");
        }
        return new ArrayList<>();
    }

    public static List<ZoneBloc.BlocPlace> simulerZoneRectangulaire(double largeurPouces, double hauteurPouces) {
        if (largeurPouces <= 0.0) {
            throw new IllegalArgumentException("La largeur doit etre superieure a 0.");
        }
        if (hauteurPouces <= 0.0) {
            throw new IllegalArgumentException("La hauteur doit etre superieure a 0.");
        }

        List<ZoneBloc.BlocPlace> blocsPlaces = new ArrayList<>();
        int nombreRangees = (int) Math.ceil(hauteurPouces / BLOC_HAUTEUR);

        for (int indexRangee = 0; indexRangee < nombreRangees; indexRangee++) {
            double y = indexRangee * BLOC_HAUTEUR;
            blocsPlaces.addAll(placerBlocsDansRangee(y, largeurPouces));
        }

        return blocsPlaces;
    }

    private static List<ZoneBloc.BlocPlace> placerBlocsDansRangee(double y, double largeurRangee) {
        List<ZoneBloc.BlocPlace> blocsPlaces = new ArrayList<>();
        int nombreBlocsComplets = (int) (largeurRangee / BLOC_LARGEUR);
        double x = 0.0;

        for (int indexBloc = 0; indexBloc < nombreBlocsComplets; indexBloc++) {
            blocsPlaces.add(new ZoneBloc.BlocPlace(x, y, BLOC_LARGEUR, BLOC_HAUTEUR, false));
            x += BLOC_LARGEUR;
        }

        double largeurRestante = largeurRangee - (nombreBlocsComplets * BLOC_LARGEUR);
        if (largeurRestante > 0.0) {
            blocsPlaces.add(new ZoneBloc.BlocPlace(x, y, largeurRestante, BLOC_HAUTEUR, true));
        }

        return blocsPlaces;
    }
}

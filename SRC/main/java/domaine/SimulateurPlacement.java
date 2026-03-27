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
}

package domaine.simulation;

import domaine.ZoneBloc;
import domaine.SimulateurPlacement;
import java.util.List;

public class PlacementTriangulaireStrategy implements PlacementStrategy {
    private static final double METRES_VERS_POUCES = 39.3701;

    @Override
    public List<ZoneBloc.BlocPlace> simuler(ZoneBloc zone) {
        double largeurPouces = zone.getLargeur() * METRES_VERS_POUCES;
        double hauteurPouces = zone.getHauteur() * METRES_VERS_POUCES;
        return SimulateurPlacement.simulerZoneTriangulaire(largeurPouces, hauteurPouces);
    }
}
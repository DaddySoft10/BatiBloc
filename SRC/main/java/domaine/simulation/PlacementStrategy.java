package domaine.simulation;

import domaine.ZoneBloc;
import java.util.List;

public interface PlacementStrategy {
    List<ZoneBloc.BlocPlace> simuler(ZoneBloc zone);
}
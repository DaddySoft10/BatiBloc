package domaine;

import java.util.ArrayList;
import java.util.List;

public class Facade {
    private final List<Zone> zones;

    public Facade() {
        this.zones = new ArrayList<>();
    }

    public void ajouterZone(Zone zone) {
        if (zone != null) {
            this.zones.add(zone);
        }
    }

    public List<Zone> getZones() {
        return this.zones;
    }
}
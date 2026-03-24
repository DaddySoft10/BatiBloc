package domaine;

import java.util.ArrayList;
import java.util.Collections;
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

    public void supprimerZone(int index) {
        this.validerIndexZone(index);
        this.zones.remove(index);
    }

    public void modifierZone(int index, Zone zone) {
        if (zone == null) {
            throw new IllegalArgumentException("La zone ne peut pas etre nulle.");
        }
        this.validerIndexZone(index);
        this.zones.set(index, zone);
    }

    public List<Zone> getZones() {
        return Collections.unmodifiableList(this.zones);
    }

    private void validerIndexZone(int index) {
        if (index < 0 || index >= this.zones.size()) {
            throw new IllegalArgumentException("Index de zone invalide.");
        }
    }
}

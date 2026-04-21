package domaine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.Serializable;

public class Facade implements Serializable {
    private final List<Zone> zones;
    private double echellePoucesParPixel;

    public Facade() {
        this.zones = new ArrayList<>();
        this.echellePoucesParPixel = 1.0;
    }

    public double getEchellePoucesParPixel() {
        return this.echellePoucesParPixel;
    }

    public void setEchellePoucesParPixel(double echellePoucesParPixel) {
        if (echellePoucesParPixel <= 0.0) {
            throw new IllegalArgumentException("L'echelle doit etre superieure a 0.");
        }
        this.echellePoucesParPixel = echellePoucesParPixel;
    }

    public void ajouterZone(Zone zone) {
        if (zone != null) {
            this.zones.add(zone);
        }
    }

    public void ajouterZone(int index, Zone zone) {
        if (zone != null) {
            if (index < 0 || index > this.zones.size()) {
                throw new IllegalArgumentException("Index de zone invalide.");
            }
            this.zones.add(index, zone);
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

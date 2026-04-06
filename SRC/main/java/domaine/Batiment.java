package domaine;

import java.util.ArrayList;
import java.util.List;

public class Batiment {
    private final List<Facade> facades;
    private final Plan plan;
    private Facade facadeCourante;

    public Batiment() {
        this.facades = new ArrayList<>();
        this.plan = new Plan();
        this.facadeCourante = null;
    }

    public void ajouterZone(Zone zone) {
        if (this.facadeCourante != null) {
            this.facadeCourante.ajouterZone(zone);
        }
    }

    public Facade getFacadeCourante() {
        if (this.facadeCourante == null) {
            return new Facade();
        }
        return this.facadeCourante;
    }

    public Plan getPlan() {
        return this.plan;
    }

    public void ajouterFacade() {
        this.facades.add(new Facade());
    }

    public Facade getFacade(int index) {
        if (index < 0 || index >= this.facades.size()) {
            return null;
        }
        return this.facades.get(index);
    }

    public void setFacadeCourante(int index) {
        if (index < 0) {
            this.facadeCourante = null;
            return;
        }
        if (index < this.facades.size()) {
            this.facadeCourante = this.facades.get(index);
        }
    }

    public void supprimerFacade(int index) {
        if (index >= 0 && index < this.facades.size()) {
            this.facades.remove(index);
        }
    }

    public int getNombreFacades() {
        return this.facades.size();
    }

    public void reinitialiserFacades() {
        this.facades.clear();
        this.facadeCourante = null;
    }
}

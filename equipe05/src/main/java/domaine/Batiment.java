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
        // Initialisation avec une facade par defaut selon le concept du projet
        this.facadeCourante = new Facade();
        this.facades.add(this.facadeCourante);
    }

    public void ajouterZone(Zone zone) {
        if (this.facadeCourante != null) {
            this.facadeCourante.ajouterZone(zone);
        }
    }

    public Facade getFacadeCourante() {
        return this.facadeCourante;
    }

    public Plan getPlan() {
        return this.plan;
    }
}

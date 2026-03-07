package domaine;

import java.util.ArrayList;
import java.util.List;

public class Plan {
    private String cheminFichier;
    private final List<String> vues;

    public Plan() {
        this.cheminFichier = "";
        this.vues = new ArrayList<>();
    }

    public String getCheminFichier() {
        return this.cheminFichier;
    }

    public List<String> getVues() {
        return new ArrayList<>(this.vues);
    }

    public void definirContenu(String cheminFichier, List<String> nouvellesVues) {
        this.cheminFichier = cheminFichier == null ? "" : cheminFichier;
        this.vues.clear();
        if (nouvellesVues != null) {
            this.vues.addAll(nouvellesVues);
        }
    }
}

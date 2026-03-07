package dto;

import java.util.ArrayList;
import java.util.List;

public class PlanDTO {
    private final String cheminFichier;
    private final List<String> vues;

    public PlanDTO(String cheminFichier, List<String> vues) {
        this.cheminFichier = cheminFichier == null ? "" : cheminFichier;
        this.vues = vues == null ? new ArrayList<>() : new ArrayList<>(vues);
    }

    public String getCheminFichier() {
        return this.cheminFichier;
    }

    public List<String> getVues() {
        return new ArrayList<>(this.vues);
    }
}

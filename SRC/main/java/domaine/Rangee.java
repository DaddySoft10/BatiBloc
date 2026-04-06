package domaine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Rangee {

    private final double y;
    private final double hauteur;
    private final List<Bloc> blocs;

    public Rangee(double y, double hauteur) {
        this.y = y;
        this.hauteur = hauteur;
        this.blocs = new ArrayList<>();
    }

    public void ajouterBloc(Bloc bloc) {
        if (bloc != null) {
            this.blocs.add(bloc);
        }
    }

    public List<Bloc> getBlocs() {
        return Collections.unmodifiableList(this.blocs);
    }

    public double getY() { return this.y; }
    public double getHauteur() { return this.hauteur; }
    public int getNombreBlocs() { return this.blocs.size(); }

    @Override
    public String toString() {
        return String.format(
            "Rangee[y=%.1f, hauteur=%.1f, blocs=%d]",
            this.y, this.hauteur, this.blocs.size());
    }
}

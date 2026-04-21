package domaine;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class ZoneBloc extends Zone implements Serializable {
    private static final String TYPE_ZONE = "BLOC";
    private List<BlocPlace> blocsSimules;

    public ZoneBloc() {
        super();
        this.blocsSimules = new ArrayList<>();
    }

    public ZoneBloc(double x, double y, double largeur, double hauteur, TypeForme typeForme) {
        super(x, y, largeur, hauteur, typeForme);
        this.blocsSimules = new ArrayList<>();
    }

    public String getTypeZone() {
        return TYPE_ZONE;
    }

    public List<BlocPlace> getBlocsSimules() {
        return List.copyOf(this.blocsSimules);
    }

    public void setBlocsSimules(List<BlocPlace> blocsSimules) {
        if (blocsSimules == null) {
            this.blocsSimules = new ArrayList<>();
            return;
        }
        this.blocsSimules = new ArrayList<>(blocsSimules);
    }

    public static class BlocPlace implements Serializable {
        private final double x;
        private final double y;
        private final double largeur;
        private final double hauteur;
        private final boolean retaille;

        public BlocPlace(double x, double y, double largeur, double hauteur, boolean retaille) {
            if (largeur <= 0.0) {
                throw new IllegalArgumentException("La largeur doit etre superieure a 0.");
            }
            if (hauteur <= 0.0) {
                throw new IllegalArgumentException("La hauteur doit etre superieure a 0.");
            }

            this.x = x;
            this.y = y;
            this.largeur = largeur;
            this.hauteur = hauteur;
            this.retaille = retaille;
        }

        public double getX() {
            return this.x;
        }

        public double getY() {
            return this.y;
        }

        public double getLargeur() {
            return this.largeur;
        }

        public double getHauteur() {
            return this.hauteur;
        }

        public boolean isRetaille() {
            return this.retaille;
        }
    }
}

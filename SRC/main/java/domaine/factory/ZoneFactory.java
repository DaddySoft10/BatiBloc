package domaine.factory;

import domaine.TypeForme;
import domaine.TypeZone;
import domaine.Zone;
import domaine.ZoneBloc;
import domaine.ZoneClassique;
import domaine.ZoneOuverture;

public final class ZoneFactory {

    private ZoneFactory() {
    }

    public static Zone creer(
            double x,
            double y,
            double largeur,
            double hauteur,
            TypeForme typeForme,
            TypeZone typeZone
    ) {
        if (largeur <= 0 || hauteur <= 0) {
            throw new IllegalArgumentException("La largeur et la hauteur doivent etre superieures a 0.");
        }

        return switch (typeZone) {
            case BLOC -> new ZoneBloc(x, y, largeur, hauteur, typeForme);
            case CLASSIQUE -> new ZoneClassique(x, y, largeur, hauteur, typeForme);
            case OUVERTURE -> new ZoneOuverture(x, y, largeur, hauteur, typeForme);
        };
    }

    public static Zone creerDepuisTexte(
            double x,
            double y,
            double largeur,
            double hauteur,
            String typeForme,
            String typeZone
    ) {
        return creer(
                x,
                y,
                largeur,
                hauteur,
                TypeForme.fromLabel(typeForme),
                TypeZone.fromLabel(typeZone)
        );
    }
}
package domaine;

import java.util.Locale;

public final class ZoneFactory {

    private ZoneFactory() {
    }

    public static Zone creerZone(
            double x,
            double y,
            double largeur,
            double hauteur,
            String typeForme,
            String typeZone
    ) {
        TypeForme forme = convertirTypeForme(typeForme);
        String typeZoneNormalise = normaliserTexte(typeZone);

        return switch (typeZoneNormalise) {
            case "BLOC", "BLOCS" ->
                    new ZoneBloc(x, y, largeur, hauteur, forme);

            case "CLASSIQUE" ->
                    new ZoneClassique(x, y, largeur, hauteur, forme);

            //case "OUVERTURE" ->
                    //new ZoneOuverture(x, y, largeur, hauteur, forme);

            default ->
                    throw new IllegalArgumentException("Type de zone invalide : " + typeZone);
        };
    }

    private static TypeForme convertirTypeForme(String typeForme) {
        String typeFormeNormalise = normaliserTexte(typeForme);

        return switch (typeFormeNormalise) {
            case "RECTANGULAIRE", "RECTANGLE" ->
                    TypeForme.RECTANGULAIRE;

            case "TRIANGULAIRE", "TRIANGLE" ->
                    TypeForme.TRIANGULAIRE;

            case "TRIANGULAIRE_TRONQUEE",
                 "TRIANGULAIRE TRONQUEE",
                 "TRIANGLE TRONQUE",
                 "TRIANGLE TRONQUEE" ->
                    TypeForme.TRIANGULAIRE_TRONQUEE;

            default ->
                    throw new IllegalArgumentException("Type de forme invalide : " + typeForme);
        };
    }

    private static String normaliserTexte(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            throw new IllegalArgumentException("La valeur ne peut pas etre vide.");
        }

        return valeur.trim().toUpperCase(Locale.ROOT);
    }
}
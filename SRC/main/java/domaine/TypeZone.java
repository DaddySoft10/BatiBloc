package domaine;

import java.util.Locale;

public enum TypeZone {
    BLOC,
    CLASSIQUE,
    OUVERTURE;

    public static TypeZone fromLabel(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            throw new IllegalArgumentException("Le type de zone ne peut pas etre vide.");
        }

        String v = valeur.trim().toUpperCase(Locale.ROOT);

        return switch (v) {
            case "BLOC", "BLOCS" -> BLOC;
            case "CLASSIQUE" -> CLASSIQUE;
            case "OUVERTURE" -> OUVERTURE;
            default -> throw new IllegalArgumentException("Type de zone invalide : " + valeur);
        };
    }
}
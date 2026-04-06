package domaine;

public enum TypeForme {
    RECTANGULAIRE,
    TRIANGULAIRE,
    TRIANGULAIRE_TRONQUEE;

    public static TypeForme fromLabel(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            throw new IllegalArgumentException("Le type de forme ne peut pas etre vide.");
        }

        String v = valeur.trim().toUpperCase(java.util.Locale.ROOT);

        return switch (v) {
            case "RECTANGULAIRE", "RECTANGLE" -> RECTANGULAIRE;
            case "TRIANGULAIRE", "TRIANGLE" -> TRIANGULAIRE;
            case "TRIANGULAIRE_TRONQUEE", "TRIANGULAIRE TRONQUEE", "TRIANGLE TRONQUE" -> TRIANGULAIRE_TRONQUEE;
            default -> throw new IllegalArgumentException("Type de forme invalide : " + valeur);
        };
    }
    }


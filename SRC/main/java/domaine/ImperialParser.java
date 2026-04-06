package domaine;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImperialParser {

    private static final double POUCES_PAR_METRE = 39.3701;
    private static final double METRES_PAR_POUCE = 0.0254;

    private ImperialParser() {}

    /**
     * Parse une chaine vers des pouces.
     * Formats acceptes :
     *   "3' 6"" → 42.0 pouces
     *   "3'"    → 36.0 pouces
     *   "6""    → 6.0 pouces
     *   "42"    → 42.0 pouces (brut)
     */
    public static double parsePouces(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            throw new IllegalArgumentException(
                "La valeur ne peut pas etre vide.");
        }
        String v = valeur.trim();

        Pattern pPiedsEtPouces = Pattern.compile(
            "(\\d+\\.?\\d*)'\\s*(\\d+\\.?\\d*)\"");
        Matcher m = pPiedsEtPouces.matcher(v);
        if (m.find()) {
            double pieds = Double.parseDouble(m.group(1));
            double pouces = Double.parseDouble(m.group(2));
            return pieds * 12.0 + pouces;
        }

        Pattern pPieds = Pattern.compile("(\\d+\\.?\\d*)'");
        m = pPieds.matcher(v);
        if (m.find()) {
            return Double.parseDouble(m.group(1)) * 12.0;
        }

        Pattern pPouces = Pattern.compile("(\\d+\\.?\\d*)\"");
        m = pPouces.matcher(v);
        if (m.find()) {
            return Double.parseDouble(m.group(1));
        }

        try {
            return Double.parseDouble(v.replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "Format invalide : " + valeur);
        }
    }

    /**
     * Formate des pouces en format imperial long.
     * Ex : 42.0 → "3' 6.00""
     */
    public static String formatterImperial(double pouces) {
        if (pouces < 0) {
            throw new IllegalArgumentException(
                "Les pouces ne peuvent pas etre negatifs.");
        }
        int pieds = (int) (pouces / 12.0);
        double poucesRestants = pouces % 12.0;
        return pieds + "' "
            + String.format("%.2f", poucesRestants) + "\"";
    }

    /**
     * Formate des pouces en format court.
     * Ex : 42.0 → "3'6.0""
     */
    public static String formatterImperialCourt(double pouces) {
        if (pouces < 0) {
            throw new IllegalArgumentException(
                "Les pouces ne peuvent pas etre negatifs.");
        }
        int pieds = (int) (pouces / 12.0);
        double poucesRestants = pouces % 12.0;
        return pieds + "'"
            + String.format("%.1f", poucesRestants) + "\"";
    }

    public static double poucesEnMetres(double pouces) {
        return pouces * METRES_PAR_POUCE;
    }

    public static double metresEnPouces(double metres) {
        return metres * POUCES_PAR_METRE;
    }
}

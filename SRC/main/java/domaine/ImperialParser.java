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
     *   "1 1/4"  → 1.25 pouces
     *   "1/4"    → 0.25 pouces
     */
    public static double parsePouces(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            throw new IllegalArgumentException("La valeur ne peut pas etre vide.");
        }
        String v = valeur.trim();
        v = v.replace("¼", " 1/4").replace("½", " 1/2").replace("¾", " 3/4");

        // Cas 1: Pieds et pouces (ex: 3' 6")
        Pattern pPiedsEtPouces = Pattern.compile("([\\d\\s/\\.\\,]+)'\\s*([\\d\\s/\\.\\,]+)\"?");
        Matcher m = pPiedsEtPouces.matcher(v);
        if (m.find()) {
            double pieds = parseNombre(m.group(1));
            String poucesStr = m.group(2).replace("\"", "").replace("''", "");
            double pouces = parseNombre(poucesStr);
            return pieds * 12.0 + pouces;
        }

        // Cas 2: Seulement des pieds (ex: 3')
        Pattern pPieds = Pattern.compile("^([\\d\\s/\\.\\,]+)'$");
        m = pPieds.matcher(v);
        if (m.find()) {
            return parseNombre(m.group(1)) * 12.0;
        }

        // Cas 3: Seulement des pouces (on enleve tous les guillemets et on parse)
        String poucesStr = v.replace("\"", "").replace("''", "").trim();
        try {
            return parseNombre(poucesStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Format invalide : " + valeur);
        }
    }

    private static double parseNombre(String nombreStr) {
        String n = nombreStr.trim().replace(',', '.');
        if (n.contains("/")) {
            String[] parts = n.split("\\s+");
            if (parts.length == 2) {
                double entier = Double.parseDouble(parts[0]);
                String[] frac = parts[1].split("/");
                double fraction = Double.parseDouble(frac[0]) / Double.parseDouble(frac[1]);
                return entier + fraction;
            } else if (parts.length == 1) {
                String[] frac = parts[0].split("/");
                return Double.parseDouble(frac[0]) / Double.parseDouble(frac[1]);
            }
        }
        return Double.parseDouble(n);
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

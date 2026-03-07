package domaine;

public class Controller {
    private final Batiment batiment;

    public Controller() {
        this.batiment = new Batiment();
    }

    public Batiment getBatiment() {
        return this.batiment;
    }


    public String simulerPlacement(double largeurMetres, double hauteurMetres) {
        //  (1 mètre = 39.3701 pouces)
        double largeurPouces = largeurMetres * 39.3701;
        double hauteurPouces = hauteurMetres * 39.3701;

        // Constantes du domaine
        final double BLOC_L = 12.0;
        final double BLOC_H = 8.0;
        final double PRIX_UNITAIRE = 20.0; // Basé sur le calcul24 blocs = 480$ (480/24 = 20)
        final double MIN_RETAILLE = 6.0;

        // 1 Calcul (Hauteur)
        int nbRangees = (int) (hauteurPouces / BLOC_H);

        // 2 Analysela largeur et de la retaille
        int nbColonnesPleines = (int) (largeurPouces / BLOC_L);
        double retaille = largeurPouces % BLOC_L;

        int nbBlocsCoupes;

        // 3 Application de la règle d'emprunt
        if (retaille > 0 && retaille < MIN_RETAILLE) {

            nbColonnesPleines -= 1;
            nbBlocsCoupes = 2;
        } else if (retaille >= MIN_RETAILLE) {

            nbBlocsCoupes = 1;
        } else {

            nbBlocsCoupes = 0;
        }

        //Bilan et Estimation
        int blocsParRangee = nbColonnesPleines + nbBlocsCoupes;
        int nbTotalBlocs = blocsParRangee * nbRangees;
        double coutTotal = nbTotalBlocs * PRIX_UNITAIRE;

        //la réponse
        return String.format(java.util.Locale.US,
                "Bilan de la simulation :\n\n" +
                        "Dimensions converties : %.2f\" L x %.2f\" H\n" +
                        "Nombre de rangées : %d\n" +
                        "Blocs par rangée : %d\n" +
                        "------------------------------------\n" +
                        "- Nombre total de blocs : %d blocs\n" +
                        "- Coût estimé : %.2f $",
                largeurPouces, hauteurPouces, nbRangees, blocsParRangee, nbTotalBlocs, coutTotal);
    }

    public void ajouterZone(double x, double y, double largeur, double hauteur, String typeForme, String typeZone) {
        Zone nouvelleZone = new Zone();
        this.batiment.ajouterZone(nouvelleZone);
    }
}
package domaine;

import java.util.ArrayList;
import java.util.List;

public final class SimulateurPlacement {
    public static final double BLOC_LARGEUR = 12.0;
    public static final double BLOC_HAUTEUR = 8.0;
    public static final double MIN_RETAILLE = 6.0;
    private static final double EPSILON = 0.0001;

    private SimulateurPlacement() {
    }

    public static List<ZoneBloc.BlocPlace> simuler(ZoneBloc zoneBloc) {
        if (zoneBloc == null) {
            throw new IllegalArgumentException("La zone de blocs ne peut pas etre nulle.");
        }
        return new ArrayList<>();
    }

    public static List<ZoneBloc.BlocPlace> simulerZoneRectangulaire(double largeurPouces, double hauteurPouces) {
        if (largeurPouces <= 0.0) {
            throw new IllegalArgumentException("La largeur doit etre superieure a 0.");
        }
        if (hauteurPouces <= 0.0) {
            throw new IllegalArgumentException("La hauteur doit etre superieure a 0.");
        }

        List<ZoneBloc.BlocPlace> blocsPlaces = new ArrayList<>();
        int nombreRangees = (int) Math.ceil(hauteurPouces / BLOC_HAUTEUR);
        double restantPourProchaineRangee = 0.0;

        for (int indexRangee = 0; indexRangee < nombreRangees; indexRangee++) {
            double y = indexRangee * BLOC_HAUTEUR;
            ResultatRangee resultatRangee = placerBlocsDansRangee(y, largeurPouces, restantPourProchaineRangee);
            blocsPlaces.addAll(resultatRangee.getBlocs());
            restantPourProchaineRangee = resultatRangee.getRestantPourProchaineRangee();
        }

        return blocsPlaces;
    }

    public static List<ZoneBloc.BlocPlace> simulerZoneTriangulaire(double largeurBasePouces, double hauteurPouces) {
        if (largeurBasePouces <= 0.0) {
            throw new IllegalArgumentException("La largeur de base doit etre superieure a 0.");
        }
        if (hauteurPouces <= 0.0) {
            throw new IllegalArgumentException("La hauteur doit etre superieure a 0.");
        }

        List<ZoneBloc.BlocPlace> blocsPlaces = new ArrayList<>();
        int nombreRangees = (int) Math.ceil(hauteurPouces / BLOC_HAUTEUR);
        double restantPourProchaineRangee = 0.0;

        for (int indexRangee = 0; indexRangee < nombreRangees; indexRangee++) {
            double y = indexRangee * BLOC_HAUTEUR;
            double ratioVertical = y / hauteurPouces;
            double largeurRangee = normaliserLongueur(largeurBasePouces * (1.0 - ratioVertical));
            double offsetX = normaliserLongueur((largeurBasePouces - largeurRangee) / 2.0);

            ResultatRangee resultatRangee = placerBlocsDansRangee(y, largeurRangee, offsetX, restantPourProchaineRangee);
            blocsPlaces.addAll(resultatRangee.getBlocs());
            restantPourProchaineRangee = resultatRangee.getRestantPourProchaineRangee();
        }

        return blocsPlaces;
    }

    public static List<ZoneBloc.BlocPlace> simulerZoneTriangulaireTronquee(double largeurBasePouces,
                                                                            double largeurSommetPouces,
                                                                            double hauteurPouces) {
        if (largeurBasePouces <= 0.0) {
            throw new IllegalArgumentException("La largeur de base doit etre superieure a 0.");
        }
        if (largeurSommetPouces <= 0.0) {
            throw new IllegalArgumentException("La largeur du sommet doit etre superieure a 0.");
        }
        if (hauteurPouces <= 0.0) {
            throw new IllegalArgumentException("La hauteur doit etre superieure a 0.");
        }
        if (largeurSommetPouces - largeurBasePouces > EPSILON) {
            throw new IllegalArgumentException("La largeur du sommet ne peut pas exceder la largeur de base.");
        }

        List<ZoneBloc.BlocPlace> blocsPlaces = new ArrayList<>();
        int nombreRangees = (int) Math.ceil(hauteurPouces / BLOC_HAUTEUR);
        double restantPourProchaineRangee = 0.0;

        for (int indexRangee = 0; indexRangee < nombreRangees; indexRangee++) {
            double y = indexRangee * BLOC_HAUTEUR;
            double ratioVertical = y / hauteurPouces;
            double largeurRangee = normaliserLongueur(largeurBasePouces
                    + ((largeurSommetPouces - largeurBasePouces) * ratioVertical));
            double offsetX = normaliserLongueur((largeurBasePouces - largeurRangee) / 2.0);

            ResultatRangee resultatRangee = placerBlocsDansRangee(y, largeurRangee, offsetX, restantPourProchaineRangee);
            blocsPlaces.addAll(resultatRangee.getBlocs());
            restantPourProchaineRangee = resultatRangee.getRestantPourProchaineRangee();
        }

        return blocsPlaces;
    }

    private static ResultatRangee placerBlocsDansRangee(double y, double largeurRangee, double longueurDepart) {
        return placerBlocsDansRangee(y, largeurRangee, 0.0, longueurDepart);
    }

    private static ResultatRangee placerBlocsDansRangee(double y, double largeurRangee, double offsetX, double longueurDepart) {
        List<ZoneBloc.BlocPlace> blocsPlaces = new ArrayList<>();
        double x = offsetX;
        double largeurRestante = largeurRangee;
        double restantPourProchaineRangee = 0.0;

        if (estStrictementPositive(longueurDepart)) {
            if (longueurDepart - largeurRangee > EPSILON) {
                return creerRangeeRedistribuee(y, largeurRangee, offsetX);
            }

            if (longueurDepart + EPSILON < MIN_RETAILLE) {
                return creerRangeeRedistribuee(y, largeurRangee, offsetX);
            }

            ajouterBloc(blocsPlaces, x, y, longueurDepart);
            x += longueurDepart;
            largeurRestante -= longueurDepart;
        }

        int nombreBlocsComplets = (int) Math.floor((largeurRestante + EPSILON) / BLOC_LARGEUR);
        for (int indexBloc = 0; indexBloc < nombreBlocsComplets; indexBloc++) {
            blocsPlaces.add(new ZoneBloc.BlocPlace(x, y, BLOC_LARGEUR, BLOC_HAUTEUR, false));
            x += BLOC_LARGEUR;
        }

        largeurRestante -= nombreBlocsComplets * BLOC_LARGEUR;
        if (estZero(largeurRestante)) {
            return new ResultatRangee(blocsPlaces, 0.0);
        }

        if (largeurRestante + EPSILON < MIN_RETAILLE) {
            return creerRangeeRedistribuee(y, largeurRangee, offsetX);
        }

        ajouterBloc(blocsPlaces, x, y, largeurRestante);
        restantPourProchaineRangee = BLOC_LARGEUR - largeurRestante;
        return new ResultatRangee(blocsPlaces, normaliserLongueur(restantPourProchaineRangee));
    }

    private static ResultatRangee creerRangeeRedistribuee(double y, double largeurRangee) {
        return creerRangeeRedistribuee(y, largeurRangee, 0.0);
    }

    private static ResultatRangee creerRangeeRedistribuee(double y, double largeurRangee, double offsetX) {
        List<ZoneBloc.BlocPlace> blocsPlaces = new ArrayList<>();
        double x = offsetX;
        int nombreBlocsCentraux = (int) Math.floor((largeurRangee - (2.0 * MIN_RETAILLE) + EPSILON) / BLOC_LARGEUR);

        if (nombreBlocsCentraux < 0) {
            nombreBlocsCentraux = 0;
        }

        double largeurExtremite = (largeurRangee - (nombreBlocsCentraux * BLOC_LARGEUR)) / 2.0;
        largeurExtremite = normaliserLongueur(largeurExtremite);

        if (largeurExtremite + EPSILON < MIN_RETAILLE) {
            throw new IllegalArgumentException("Impossible de respecter la retaille minimale pour cette rangee.");
        }

        ajouterBloc(blocsPlaces, x, y, largeurExtremite);
        x += largeurExtremite;

        for (int indexBloc = 0; indexBloc < nombreBlocsCentraux; indexBloc++) {
            blocsPlaces.add(new ZoneBloc.BlocPlace(x, y, BLOC_LARGEUR, BLOC_HAUTEUR, false));
            x += BLOC_LARGEUR;
        }

        ajouterBloc(blocsPlaces, x, y, largeurExtremite);

        double restantPourProchaineRangee = BLOC_LARGEUR - largeurExtremite;
        if (restantPourProchaineRangee + EPSILON < MIN_RETAILLE) {
            restantPourProchaineRangee = 0.0;
        }

        return new ResultatRangee(blocsPlaces, normaliserLongueur(restantPourProchaineRangee));
    }

    private static void ajouterBloc(List<ZoneBloc.BlocPlace> blocsPlaces, double x, double y, double largeur) {
        double largeurNormalisee = normaliserLongueur(largeur);
        boolean retaille = Math.abs(largeurNormalisee - BLOC_LARGEUR) > EPSILON;
        blocsPlaces.add(new ZoneBloc.BlocPlace(x, y, largeurNormalisee, BLOC_HAUTEUR, retaille));
    }

    private static boolean estZero(double valeur) {
        return Math.abs(valeur) <= EPSILON;
    }

    private static boolean estStrictementPositive(double valeur) {
        return valeur > EPSILON;
    }

    private static double normaliserLongueur(double valeur) {
        if (estZero(valeur)) {
            return 0.0;
        }
        return Math.round(valeur * 1000.0) / 1000.0;
    }

    private static final class ResultatRangee {
        private final List<ZoneBloc.BlocPlace> blocs;
        private final double restantPourProchaineRangee;

        private ResultatRangee(List<ZoneBloc.BlocPlace> blocs, double restantPourProchaineRangee) {
            this.blocs = blocs;
            this.restantPourProchaineRangee = restantPourProchaineRangee;
        }

        private List<ZoneBloc.BlocPlace> getBlocs() {
            return this.blocs;
        }

        private double getRestantPourProchaineRangee() {
            return this.restantPourProchaineRangee;
        }
    }
}

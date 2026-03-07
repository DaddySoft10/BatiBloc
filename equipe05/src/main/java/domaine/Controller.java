package domaine;

import dto.PlanDTO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Controller {
    private final Batiment batiment;
    private int indexVueCourante;
    private List<BufferedImage> imagesVues;

    public Controller() {
        this.batiment = new Batiment();
        this.indexVueCourante = -1;
        this.imagesVues = new ArrayList<>();
    }

    public int importerPlanPdf(String cheminFichier) throws IOException {
        if (cheminFichier == null || cheminFichier.isBlank()) {
            throw new IllegalArgumentException("Le chemin du fichier PDF est invalide.");
        }

        File fichier = new File(cheminFichier);
        if (!fichier.exists() || !fichier.isFile()) {
            throw new IOException("Le fichier PDF est introuvable: " + cheminFichier);
        }

        if (!cheminFichier.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Le fichier selectionne doit etre un PDF.");
        }

        try (PDDocument document = Loader.loadPDF(fichier)) {
            int nombrePages = document.getNumberOfPages();
            List<String> vues = new ArrayList<>();
            List<BufferedImage> nouvellesImagesVues = new ArrayList<>();
            PDFRenderer renderer = new PDFRenderer(document);

            for (int i = 0; i < nombrePages; i++) {
                vues.add("Vue " + (i + 1));
                nouvellesImagesVues.add(renderer.renderImageWithDPI(i, 120, ImageType.RGB));
            }

            this.batiment.getPlan().definirContenu(cheminFichier, vues);
            this.imagesVues = nouvellesImagesVues;
            this.indexVueCourante = nombrePages > 0 ? 0 : -1;
            return nombrePages;
        }
    }

    public List<String> getVuesDuPlan() {
        return this.batiment.getPlan().getVues();
    }

    public PlanDTO getPlanCourant() {
        return new PlanDTO(
                this.batiment.getPlan().getCheminFichier(),
                this.batiment.getPlan().getVues()
        );
    }

    public int getIndexVueCourante() {
        return this.indexVueCourante;
    }

    public String getNomVueCourante() {
        List<String> vues = this.batiment.getPlan().getVues();
        if (this.indexVueCourante < 0 || this.indexVueCourante >= vues.size()) {
            return "";
        }
        return vues.get(this.indexVueCourante);
    }

    public void selectionnerVue(int index) {
        List<String> vues = this.batiment.getPlan().getVues();
        if (index < 0 || index >= vues.size()) {
            throw new IllegalArgumentException("Index de vue invalide.");
        }
        this.indexVueCourante = index;
    }

    public BufferedImage getImageVueCourante() {
        if (this.indexVueCourante < 0 || this.indexVueCourante >= this.imagesVues.size()) {
            return null;
        }
        return this.imagesVues.get(this.indexVueCourante);
    }

    public int getNombreZonesFacadeCourante() {
        return this.batiment.getFacadeCourante().getZones().size();
    }

    public String simulerPlacement(double largeurMetres, double hauteurMetres) {
        //  (1 metre = 39.3701 pouces)
        double largeurPouces = largeurMetres * 39.3701;
        double hauteurPouces = hauteurMetres * 39.3701;

        // Constantes du domaine
        final double BLOC_L = 12.0;
        final double BLOC_H = 8.0;
        final double PRIX_UNITAIRE = 20.0; // Base sur le calcul24 blocs = 480$ (480/24 = 20)
        final double MIN_RETAILLE = 6.0;

        // 1 Calcul (Hauteur)
        int nbRangees = (int) (hauteurPouces / BLOC_H);

        // 2 Analyse de la largeur et de la retaille
        int nbColonnesPleines = (int) (largeurPouces / BLOC_L);
        double retaille = largeurPouces % BLOC_L;

        int nbBlocsCoupes;

        // 3 Application de la regle d'emprunt
        if (retaille > 0 && retaille < MIN_RETAILLE) {
            nbColonnesPleines -= 1;
            nbBlocsCoupes = 2;
        } else if (retaille >= MIN_RETAILLE) {
            nbBlocsCoupes = 1;
        } else {
            nbBlocsCoupes = 0;
        }

        // Bilan et estimation
        int blocsParRangee = nbColonnesPleines + nbBlocsCoupes;
        int nbTotalBlocs = blocsParRangee * nbRangees;
        double coutTotal = nbTotalBlocs * PRIX_UNITAIRE;

        // Reponse
        return String.format(java.util.Locale.US,
                "Bilan de la simulation :\n\n" +
                        "Dimensions converties : %.2f\" L x %.2f\" H\n" +
                        "Nombre de rangees : %d\n" +
                        "Blocs par rangee : %d\n" +
                        "------------------------------------\n" +
                        "- Nombre total de blocs : %d blocs\n" +
                        "- Cout estime : %.2f $",
                largeurPouces, hauteurPouces, nbRangees, blocsParRangee, nbTotalBlocs, coutTotal);
    }

    public void ajouterZone(double x, double y, double largeur, double hauteur, String typeForme, String typeZone) {
        Zone nouvelleZone = new Zone();
        this.batiment.ajouterZone(nouvelleZone);
    }
}

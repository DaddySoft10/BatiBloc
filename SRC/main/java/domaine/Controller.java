package domaine;

import dto.PlanDTO;
import dto.ZoneDTO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Controller {
    private static final double ECHELLE_PAR_DEFAUT_METRES_PAR_PIXEL = 1.0;

    private final Batiment batiment;
    private int indexVueCourante;
    private int indexZoneSelectionnee;
    private List<BufferedImage> imagesVues;
    private double metresParPixel;

    public Controller() {
        this.batiment = new Batiment();
        this.indexVueCourante = -1;
        this.indexZoneSelectionnee = -1;
        this.imagesVues = new ArrayList<>();
        this.metresParPixel = ECHELLE_PAR_DEFAUT_METRES_PAR_PIXEL;
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

    public int getNombreVues() {
        return this.batiment.getPlan().getVues().size();
    }

    public void rognerVueCourante(int x, int y, int largeur, int hauteur) {
        Rectangle zoneImage = new Rectangle(x, y, largeur, hauteur);
        if (zoneImage.width <= 0 || zoneImage.height <= 0) {
            throw new IllegalArgumentException("Zone de rognage invalide.");
        }
        if (this.indexVueCourante < 0 || this.indexVueCourante >= this.imagesVues.size()) {
            throw new IllegalStateException("Aucune vue courante selectionnee.");
        }

        BufferedImage imageSource = this.imagesVues.get(this.indexVueCourante);
        Rectangle zoneValide = this.normaliserZoneDansImage(zoneImage, imageSource);
        BufferedImage imageRognee = imageSource.getSubimage(
                zoneValide.x,
                zoneValide.y,
                zoneValide.width,
                zoneValide.height
        );
        this.imagesVues.set(this.indexVueCourante, copierImage(imageRognee));
    }

    public void ajouterVueRognee(int x, int y, int largeur, int hauteur, String nomVue) {
        Rectangle zoneImage = new Rectangle(x, y, largeur, hauteur);
        if (nomVue == null || nomVue.isBlank()) {
            throw new IllegalArgumentException("Le nom de la vue est invalide.");
        }
        if (zoneImage.width <= 0 || zoneImage.height <= 0) {
            throw new IllegalArgumentException("Zone de rognage invalide.");
        }
        if (this.indexVueCourante < 0 || this.indexVueCourante >= this.imagesVues.size()) {
            throw new IllegalStateException("Aucune vue courante selectionnee.");
        }

        BufferedImage imageSource = this.imagesVues.get(this.indexVueCourante);
        Rectangle zoneValide = this.normaliserZoneDansImage(zoneImage, imageSource);
        BufferedImage imageRognee = imageSource.getSubimage(
                zoneValide.x,
                zoneValide.y,
                zoneValide.width,
                zoneValide.height
        );

        this.batiment.getPlan().ajouterVue(nomVue.trim());
        this.imagesVues.add(copierImage(imageRognee));
        this.indexVueCourante = this.imagesVues.size() - 1;
    }

    public void supprimerVue(int index) {
        List<String> vues = this.batiment.getPlan().getVues();
        if (index < 0 || index >= vues.size()) {
            throw new IllegalArgumentException("Index de vue invalide.");
        }

        this.batiment.getPlan().supprimerVue(index);

        if (index < this.imagesVues.size()) {
            this.imagesVues.remove(index);
        }

        int nbVuesRestantes = this.batiment.getPlan().getVues().size();
        if (nbVuesRestantes == 0) {
            this.indexVueCourante = -1;
            return;
        }

        if (this.indexVueCourante >= nbVuesRestantes) {
            this.indexVueCourante = nbVuesRestantes - 1;
        } else if (this.indexVueCourante == index) {
            this.indexVueCourante = Math.min(index, nbVuesRestantes - 1);
        } else if (this.indexVueCourante > index) {
            this.indexVueCourante -= 1;
        }
    }

    public int getNombreZonesFacadeCourante() {
        return this.batiment.getFacadeCourante().getZones().size();
    }

    public double getMetresParPixel() {
        return this.metresParPixel;
    }

    public void definirMetresParPixel(double metresParPixel) {
        if (metresParPixel <= 0.0) {
            throw new IllegalArgumentException("L'echelle doit etre superieure a 0.");
        }
        this.metresParPixel = metresParPixel;
    }

    public double convertirPixelsEnCoordonneeReelle(double pixels) {
        return pixels * this.metresParPixel;
    }

    public double convertirCoordonneeReelleEnPixels(double coordonneeReelle) {
        return coordonneeReelle / this.metresParPixel;
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
        double xReel = this.convertirPixelsEnCoordonneeReelle(x);
        double yReel = this.convertirPixelsEnCoordonneeReelle(y);
        Zone nouvelleZone = this.creerZone(xReel, yReel, largeur, hauteur, typeForme, typeZone);
        this.batiment.ajouterZone(nouvelleZone);
        this.indexZoneSelectionnee = this.batiment.getFacadeCourante().getZones().size() - 1;
    }

    public void supprimerZone(int index) {
        this.batiment.getFacadeCourante().supprimerZone(index);
        if (this.indexZoneSelectionnee == index) {
            this.indexZoneSelectionnee = -1;
        } else if (this.indexZoneSelectionnee > index) {
            this.indexZoneSelectionnee -= 1;
        }
    }

    public void modifierZone(int index, double x, double y, double largeur, double hauteur, String typeForme, String typeZone) {
        Zone zoneModifiee = this.creerZone(x, y, largeur, hauteur, typeForme, typeZone);
        this.batiment.getFacadeCourante().modifierZone(index, zoneModifiee);
        this.indexZoneSelectionnee = index;
    }

    public List<ZoneDTO> getZones() {
        List<ZoneDTO> zones = new ArrayList<>();
        for (Zone zone : this.batiment.getFacadeCourante().getZones()) {
            zones.add(this.convertirEnZoneDTO(zone));
        }
        return zones;
    }

    public int selectionnerZone(double x, double y) {
        List<Zone> zones = this.batiment.getFacadeCourante().getZones();
        for (int i = zones.size() - 1; i >= 0; i--) {
            if (zones.get(i).contientPoint(x, y)) {
                this.indexZoneSelectionnee = i;
                return i;
            }
        }

        this.indexZoneSelectionnee = -1;
        return -1;
    }

    private Rectangle normaliserZoneDansImage(Rectangle zoneImage, BufferedImage imageSource) {
        int x = Math.max(0, zoneImage.x);
        int y = Math.max(0, zoneImage.y);
        int maxLargeur = imageSource.getWidth() - x;
        int maxHauteur = imageSource.getHeight() - y;
        int largeur = Math.min(zoneImage.width, maxLargeur);
        int hauteur = Math.min(zoneImage.height, maxHauteur);

        if (largeur <= 0 || hauteur <= 0) {
            throw new IllegalArgumentException("Zone de rognage hors limites.");
        }
        return new Rectangle(x, y, largeur, hauteur);
    }

    private BufferedImage copierImage(BufferedImage imageSource) {
        BufferedImage copie = new BufferedImage(imageSource.getWidth(), imageSource.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = copie.createGraphics();
        g2d.drawImage(imageSource, 0, 0, null);
        g2d.dispose();
        return copie;
    }

    private Zone creerZone(double x, double y, double largeur, double hauteur, String typeForme, String typeZone) {
        TypeForme forme = this.convertirTypeForme(typeForme);
        String typeZoneNormalise = this.normaliserTexte(typeZone);

        return switch (typeZoneNormalise) {
            case "BLOC", "BLOCS" -> new ZoneBloc(x, y, largeur, hauteur, forme);
            case "CLASSIQUE" -> new ZoneClassique(x, y, largeur, hauteur, forme);
            default -> throw new IllegalArgumentException("Type de zone invalide.");
        };
    }

    private TypeForme convertirTypeForme(String typeForme) {
        String typeFormeNormalise = this.normaliserTexte(typeForme);

        return switch (typeFormeNormalise) {
            case "RECTANGULAIRE", "RECTANGLE" -> TypeForme.RECTANGULAIRE;
            case "TRIANGULAIRE", "TRIANGLE" -> TypeForme.TRIANGULAIRE;
            case "TRIANGULAIRE_TRONQUEE", "TRIANGULAIRE TRONQUEE", "TRIANGLE TRONQUE" -> TypeForme.TRIANGULAIRE_TRONQUEE;
            default -> throw new IllegalArgumentException("Type de forme invalide.");
        };
    }

    private ZoneDTO convertirEnZoneDTO(Zone zone) {
        String typeZone = "ZONE";
        if (zone instanceof ZoneBloc) {
            typeZone = "BLOC";
        } else if (zone instanceof ZoneClassique) {
            typeZone = "CLASSIQUE";
        }

        return new ZoneDTO(
                zone.getX(),
                zone.getY(),
                zone.getLargeur(),
                zone.getHauteur(),
                zone.getTypeForme().name(),
                typeZone
        );
    }

    private String normaliserTexte(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            throw new IllegalArgumentException("La valeur ne peut pas etre vide.");
        }
        return valeur.trim().toUpperCase(Locale.ROOT);
    }
}

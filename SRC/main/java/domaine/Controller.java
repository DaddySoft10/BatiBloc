package domaine;

import dto.PlanDTO;
import dto.ZoneDTO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.Loader;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import domaine.factory.ZoneFactory;
import domaine.TypeZone;
import domaine.TypeForme;

public class Controller {
    private static final double RATIO_LARGEUR_SOMMET_TRIANGLE_TRONQUE = 0.5;

    private Batiment batiment;
    private final domaine.utilitaires.CommandManager commandManager;
    private int indexVueCourante;
    private int indexZoneSelectionnee;
    private List<BufferedImage> imagesVues;
    private int nombreTotalBlocs;
    private String dernierMessage;
    private double prixParBloc;
    private int indexZoneTracking;
    private double posXInitialeTracking;
    private double posYInitialeTracking;

    public Controller() {
        this.batiment = new Batiment();
        this.commandManager = new domaine.utilitaires.CommandManager();
        this.indexVueCourante = -1;
        this.indexZoneSelectionnee = -1;
        this.imagesVues = new ArrayList<>();
        this.nombreTotalBlocs = 0;
        this.dernierMessage = "";
        this.prixParBloc = 20.0;
        this.indexZoneTracking = -1;
        this.posXInitialeTracking = 0.0;
        this.posYInitialeTracking = 0.0;
    }

    public double getPrixParBloc() {
        return this.prixParBloc;
    }

    public void setPrixParBloc(double prix) {
        if (prix < 0.0) {
            throw new IllegalArgumentException("Le prix par bloc ne peut pas etre negatif.");
        }
        this.prixParBloc = prix;
    }

    public String getDernierMessage() {
        String msg = this.dernierMessage;
        this.dernierMessage = "";
        return msg;
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

        try (PDDocument document = Loader.loadPDF(fichier))  {
            int nombrePages = document.getNumberOfPages();
            List<String> vues = new ArrayList<>();
            List<BufferedImage> nouvellesImagesVues = new ArrayList<>();
            PDFRenderer renderer = new PDFRenderer(document);

            for (int i = 0; i < nombrePages; i++) {
                vues.add("Vue " + (i + 1));
                nouvellesImagesVues.add(renderer.renderImageWithDPI(i, 120, ImageType.RGB));
            }

            this.batiment.getPlan().definirContenu(cheminFichier, vues);
            this.batiment.reinitialiserFacades();
            for (int i = 0; i < nombrePages; i++) {
                this.batiment.ajouterFacade();
            }
            this.imagesVues = nouvellesImagesVues;
            this.indexVueCourante = nombrePages > 0 ? 0 : -1;
            if (nombrePages > 0) {
                this.batiment.setFacadeCourante(0);
            }
            this.indexZoneSelectionnee = -1;
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
        this.batiment.setFacadeCourante(index);
        this.indexZoneSelectionnee = -1;
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
        BufferedImage nouvelleImage = copierImage(imageRognee);
        BufferedImage ancienneImage = copierImage(imageSource);
        int indexVue = this.indexVueCourante;

        this.commandManager.executeCommand(new domaine.utilitaires.Command() {
            @Override
            public void execute() {
                imagesVues.set(indexVue, nouvelleImage);
            }

            @Override
            public void undo() {
                imagesVues.set(indexVue, ancienneImage);
            }
        });
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
        this.batiment.ajouterFacade();
        this.imagesVues.add(copierImage(imageRognee));
        this.indexVueCourante = this.imagesVues.size() - 1;
        this.batiment.setFacadeCourante(this.indexVueCourante);
        this.indexZoneSelectionnee = -1;
    }

    public void supprimerVue(int index) {
        List<String> vues = this.batiment.getPlan().getVues();
        if (index < 0 || index >= vues.size()) {
            throw new IllegalArgumentException("Index de vue invalide.");
        }

        this.batiment.getPlan().supprimerVue(index);
        this.batiment.supprimerFacade(index);

        if (index < this.imagesVues.size()) {
            this.imagesVues.remove(index);
        }

        int nbVuesRestantes = this.batiment.getPlan().getVues().size();
        if (nbVuesRestantes == 0) {
            this.indexVueCourante = -1;
            this.batiment.setFacadeCourante(-1);
            this.indexZoneSelectionnee = -1;
            return;
        }

        if (this.indexVueCourante >= nbVuesRestantes) {
            this.indexVueCourante = nbVuesRestantes - 1;
        } else if (this.indexVueCourante == index) {
            this.indexVueCourante = Math.min(index, nbVuesRestantes - 1);
        } else if (this.indexVueCourante > index) {
            this.indexVueCourante -= 1;
        }
        this.batiment.setFacadeCourante(this.indexVueCourante);
        this.indexZoneSelectionnee = -1;
    }

    public int getNombreZonesFacadeCourante() {
        return this.batiment.getFacadeCourante().getZones().size();
    }

    public double getEchellePoucesParPixel() {
        if (this.batiment.getFacadeCourante() == null) {
            return 1.0; // scale default
        }
        return this.batiment.getFacadeCourante().getEchellePoucesParPixel();
    }

    public void definirEchellePoucesParPixel(double echellePoucesParPixel) {
        if (echellePoucesParPixel <= 0.0) {
            throw new IllegalArgumentException("L'echelle doit etre superieure a 0.");
        }
        if (this.batiment.getFacadeCourante() != null) {
            this.batiment.getFacadeCourante().setEchellePoucesParPixel(echellePoucesParPixel);
        }
    }

    public double convertirPixelsEnCoordonneeReelle(double pixels) {
        return pixels * this.getEchellePoucesParPixel();
    }

    public double convertirCoordonneeReelleEnPixels(double coordonneeReelle) {
        return coordonneeReelle / this.getEchellePoucesParPixel();
    }

    public String simulerPlacement(double largeurPouces, double hauteurPouces) {

        // Constantes du domaine
        final double BLOC_L = domaine.SimulateurPlacement.BLOC_LARGEUR;
        final double BLOC_H = domaine.SimulateurPlacement.BLOC_HAUTEUR;
        final double PRIX_UNITAIRE = this.prixParBloc;
        final double MIN_RETAILLE = domaine.SimulateurPlacement.MIN_RETAILLE;

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

    public void ajouterZone(double x, double y, double largeur, double hauteur,
                                        String typeForme, String typeZone, double ratioCoupe) {
        double xReel = this.convertirPixelsEnCoordonneeReelle(x);
        double yReel = this.convertirPixelsEnCoordonneeReelle(y);
        double largeurReelle = this.convertirPixelsEnCoordonneeReelle(largeur);
        double hauteurReelle = this.convertirPixelsEnCoordonneeReelle(hauteur);

        if ("BLOC".equals(typeZone) && "RECTANGULAIRE".equals(typeForme)) {
            int nbRangees = (int) Math.ceil(hauteurReelle / domaine.SimulateurPlacement.BLOC_HAUTEUR);
            double hauteurAjustee = nbRangees * domaine.SimulateurPlacement.BLOC_HAUTEUR;
            if (Math.abs(hauteurAjustee - hauteurReelle) > 0.001) {
                this.dernierMessage = "Hauteur ajustee de " + domaine.ImperialParser.formatterImperialCourt(hauteurReelle) 
                                    + " a " + domaine.ImperialParser.formatterImperialCourt(hauteurAjustee);
                hauteurReelle = hauteurAjustee;
            } else {
                this.dernierMessage = "";
            }
        }

        Zone nouvelleZone = ZoneFactory.creerDepuisTexte(
                xReel, yReel, largeurReelle, hauteurReelle, typeForme, typeZone, ratioCoupe
        );

        this.commandManager.executeCommand(new domaine.utilitaires.Command() {
            private final int index = batiment.getFacadeCourante().getZones().size();

            @Override
            public void execute() {
                batiment.getFacadeCourante().ajouterZone(index, nouvelleZone);
                indexZoneSelectionnee = index;
                lancerSimulationToutesLesZones();
            }

            @Override
            public void undo() {
                batiment.getFacadeCourante().supprimerZone(index);
                indexZoneSelectionnee = -1;
                lancerSimulationToutesLesZones();
            }
        });
    }

    public void ajouterZoneDepuisPanneau(double x, double y, double largeur, double hauteur, String typeForme, String typeZone, double ratioCoupe) {
        if ("BLOC".equals(typeZone) && "RECTANGULAIRE".equals(typeForme)) {
            int nbRangees = (int) Math.ceil(hauteur / domaine.SimulateurPlacement.BLOC_HAUTEUR);
            double hauteurAjustee = nbRangees * domaine.SimulateurPlacement.BLOC_HAUTEUR;
            if (Math.abs(hauteurAjustee - hauteur) > 0.001) {
                this.dernierMessage = "Hauteur ajustee de " + domaine.ImperialParser.formatterImperialCourt(hauteur) 
                                    + " a " + domaine.ImperialParser.formatterImperialCourt(hauteurAjustee);
                hauteur = hauteurAjustee;
            } else {
                this.dernierMessage = "";
            }
        }

        Zone nouvelleZone = ZoneFactory.creerDepuisTexte(x, y, largeur, hauteur, typeForme, typeZone, ratioCoupe);
        
        this.commandManager.executeCommand(new domaine.utilitaires.Command() {
            private final int index = batiment.getFacadeCourante().getZones().size();

            @Override
            public void execute() {
                batiment.getFacadeCourante().ajouterZone(index, nouvelleZone);
                indexZoneSelectionnee = index;
                lancerSimulationToutesLesZones();
            }

            @Override
            public void undo() {
                batiment.getFacadeCourante().supprimerZone(index);
                indexZoneSelectionnee = -1;
                lancerSimulationToutesLesZones();
            }
        });
    }

    public void supprimerZone(int index) {
        List<Zone> zones = this.batiment.getFacadeCourante().getZones();
        if (index < 0 || index >= zones.size()) {
            return;
        }

        Zone zoneSupprimee = zones.get(index);
        final int oldIndexSelectionnee = this.indexZoneSelectionnee;

        this.commandManager.executeCommand(new domaine.utilitaires.Command() {
            @Override
            public void execute() {
                batiment.getFacadeCourante().supprimerZone(index);
                if (indexZoneSelectionnee == index) {
                    indexZoneSelectionnee = -1;
                } else if (indexZoneSelectionnee > index) {
                    indexZoneSelectionnee -= 1;
                }
                lancerSimulationToutesLesZones();
            }

            @Override
            public void undo() {
                batiment.getFacadeCourante().ajouterZone(index, zoneSupprimee);
                indexZoneSelectionnee = oldIndexSelectionnee;
                lancerSimulationToutesLesZones();
            }
        });
    }

    public void modifierZone(int index, double x, double y, double largeur, double hauteur, String typeForme, String typeZone, double ratioCoupe) {
        List<Zone> zones = this.batiment.getFacadeCourante().getZones();
        if (index < 0 || index >= zones.size()) {
            return;
        }

        if ("BLOC".equals(typeZone) && "RECTANGULAIRE".equals(typeForme)) {
            int nbRangees = (int) Math.ceil(hauteur / domaine.SimulateurPlacement.BLOC_HAUTEUR);
            double hauteurAjustee = nbRangees * domaine.SimulateurPlacement.BLOC_HAUTEUR;
            if (Math.abs(hauteurAjustee - hauteur) > 0.001) {
                this.dernierMessage = "Hauteur ajustee de " + domaine.ImperialParser.formatterImperialCourt(hauteur) 
                                    + " a " + domaine.ImperialParser.formatterImperialCourt(hauteurAjustee);
                hauteur = hauteurAjustee;
            } else {
                this.dernierMessage = "";
            }
        }
        
        Zone zoneAncienne = zones.get(index);
        Zone zoneModifiee = ZoneFactory.creerDepuisTexte(x, y, largeur, hauteur, typeForme, typeZone, ratioCoupe);

        this.commandManager.executeCommand(new domaine.utilitaires.Command() {
            @Override
            public void execute() {
                batiment.getFacadeCourante().modifierZone(index, zoneModifiee);
                indexZoneSelectionnee = index;
                lancerSimulationToutesLesZones();
            }

            @Override
            public void undo() {
                batiment.getFacadeCourante().modifierZone(index, zoneAncienne);
                indexZoneSelectionnee = index;
                lancerSimulationToutesLesZones();
            }
        });
    }

    public List<ZoneDTO> getZones() {
        List<ZoneDTO> zones = new ArrayList<>();
        for (Zone zone : this.batiment.getFacadeCourante().getZones()) {
            zones.add(this.convertirEnZoneDTO(zone));
        }
        return zones;
    }

    public int getIndexZoneSelectionnee() {
        return this.indexZoneSelectionnee;
    }

    public ZoneDTO getZoneSelectionnee() {
        if (this.indexZoneSelectionnee < 0) {
            return null;
        }

        List<Zone> zones = this.batiment.getFacadeCourante().getZones();
        if (this.indexZoneSelectionnee >= zones.size()) {
            return null;
        }

        return this.convertirEnZoneDTO(zones.get(this.indexZoneSelectionnee));
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

    public void deselectionnerToutesLesZones() {
        this.indexZoneSelectionnee = -1;
    }

    public void deplacerZone(int index, double dx, double dy) {
        List<Zone> zones = this.batiment.getFacadeCourante().getZones();
        if (index < 0 || index >= zones.size()) {
            return;
        }

        Zone zoneAncienne = zones.get(index);
        // On doit utiliser factory pour creer une copie profonde si on bouge
        String typeZoneStr;
        if (zoneAncienne instanceof ZoneBloc) typeZoneStr = "BLOC";
        else if (zoneAncienne instanceof ZoneClassique) typeZoneStr = "CLASSIQUE";
        else typeZoneStr = "OUVERTURE";

        Zone zoneDeplacee = ZoneFactory.creerDepuisTexte(
                zoneAncienne.getX() + dx,
                zoneAncienne.getY() + dy,
                zoneAncienne.getLargeur(), zoneAncienne.getHauteur(),
                zoneAncienne.getTypeForme().name(),
                typeZoneStr,
                zoneAncienne.getRatioCoupe()
        );

        this.commandManager.executeCommand(new domaine.utilitaires.Command() {
            @Override
            public void execute() {
                batiment.getFacadeCourante().modifierZone(index, zoneDeplacee);
                indexZoneSelectionnee = index;
                lancerSimulationToutesLesZones(); // TODO: call only for visual feedback or do it in one go
            }

            @Override
            public void undo() {
                batiment.getFacadeCourante().modifierZone(index, zoneAncienne);
                indexZoneSelectionnee = index;
                lancerSimulationToutesLesZones();
            }
        });
    }

    public void demarrerTracking(int index) {
        List<Zone> zones = this.batiment.getFacadeCourante().getZones();
        if (index < 0 || index >= zones.size()) return;
        Zone zone = zones.get(index);
        this.indexZoneTracking = index;
        this.posXInitialeTracking = zone.getX();
        this.posYInitialeTracking = zone.getY();
    }

    public void deplacerZoneDirect(int index, double dx, double dy) {
        List<Zone> zones = this.batiment.getFacadeCourante().getZones();
        if (index < 0 || index >= zones.size()) return;

        Zone zoneAncienne = zones.get(index);
        String typeZoneStr;
        if (zoneAncienne instanceof ZoneBloc) typeZoneStr = "BLOC";
        else if (zoneAncienne instanceof ZoneClassique) typeZoneStr = "CLASSIQUE";
        else typeZoneStr = "OUVERTURE";

        Zone zoneDeplacee = ZoneFactory.creerDepuisTexte(
                zoneAncienne.getX() + dx,
                zoneAncienne.getY() + dy,
                zoneAncienne.getLargeur(), zoneAncienne.getHauteur(),
                zoneAncienne.getTypeForme().name(),
                typeZoneStr,
                zoneAncienne.getRatioCoupe()
        );
        this.batiment.getFacadeCourante().modifierZone(index, zoneDeplacee);
        this.indexZoneSelectionnee = index;
        this.lancerSimulationToutesLesZones();
    }

    public void finaliserDeplacement(int index) {
        List<Zone> zones = this.batiment.getFacadeCourante().getZones();
        if (index < 0 || index >= zones.size() || index != this.indexZoneTracking) {
            this.indexZoneTracking = -1;
            return;
        }

        Zone zoneFinale = zones.get(index);
        double xFinal = zoneFinale.getX();
        double yFinal = zoneFinale.getY();
        double xInitial = this.posXInitialeTracking;
        double yInitial = this.posYInitialeTracking;
        this.indexZoneTracking = -1;

        if (xFinal == xInitial && yFinal == yInitial) {
            return;
        }

        String typeZoneStr;
        if (zoneFinale instanceof ZoneBloc) typeZoneStr = "BLOC";
        else if (zoneFinale instanceof ZoneClassique) typeZoneStr = "CLASSIQUE";
        else typeZoneStr = "OUVERTURE";

        Zone zoneInitiale = ZoneFactory.creerDepuisTexte(
                xInitial, yInitial,
                zoneFinale.getLargeur(), zoneFinale.getHauteur(),
                zoneFinale.getTypeForme().name(),
                typeZoneStr,
                zoneFinale.getRatioCoupe()
        );
        Zone zoneFinaleCapture = zoneFinale;

        this.commandManager.registerCommand(new domaine.utilitaires.Command() {
            @Override
            public void execute() {
                batiment.getFacadeCourante().modifierZone(index, zoneFinaleCapture);
                indexZoneSelectionnee = index;
                lancerSimulationToutesLesZones();
            }

            @Override
            public void undo() {
                batiment.getFacadeCourante().modifierZone(index, zoneInitiale);
                indexZoneSelectionnee = index;
                lancerSimulationToutesLesZones();
            }
        });
    }

    public void supprimerZoneSelectionnee() {
        this.supprimerZone(this.indexZoneSelectionnee);
    }

    public boolean zoneContientPoint(int index, double x, double y) {
        List<Zone> zones = this.batiment.getFacadeCourante().getZones();
        if (index < 0 || index >= zones.size()) {
            return false;
        }
        return zones.get(index).contientPoint(x, y);
    }

    public void lancerSimulationToutesLesZones() {
        int total = 0;

        // Vider les anciens blocs de toutes les zones
        for (Zone zone : this.batiment.getFacadeCourante().getZones()) {
            if (zone instanceof ZoneBloc zoneBloc) {
                zoneBloc.setBlocsSimules(new java.util.ArrayList<>());
            }
        }

        // Simuler pour toute la facade
        List<ZoneBloc.BlocPlace> blocsGlobaux = domaine.SimulateurPlacement.simulerFacade(this.batiment.getFacadeCourante().getZones());

        // Répartir les blocs vers les zones visuelles pour affichage
        for (ZoneBloc.BlocPlace bloc : blocsGlobaux) {
            ZoneBloc cible = null;
            for (Zone zone : this.batiment.getFacadeCourante().getZones()) {
                if (zone instanceof ZoneBloc zoneBloc) {
                    if (bloc.getX() + 0.1 >= zone.getX() && bloc.getX() + 0.1 <= zone.getX() + zone.getLargeur() &&
                        bloc.getY() + 0.1 >= zone.getY() && bloc.getY() + 0.1 <= zone.getY() + zone.getHauteur()) {
                        cible = zoneBloc;
                        break;
                    }
                }
            }
            if (cible != null) {
                double relX = bloc.getX() - cible.getX();
                double relY = bloc.getY() - cible.getY();
                
                List<ZoneBloc.BlocPlace> liste = new java.util.ArrayList<>(cible.getBlocsSimules());
                liste.add(new ZoneBloc.BlocPlace(relX, relY, bloc.getLargeur(), bloc.getHauteur(), bloc.isRetaille()));
                cible.setBlocsSimules(liste);
                total++;
            }
        }

        this.nombreTotalBlocs = total;
    }

    public void annulerAction() {
        if (this.commandManager.canUndo()) {
            this.commandManager.undo();
        }
    }

    public void refaireAction() {
        if (this.commandManager.canRedo()) {
            this.commandManager.redo();
        }
    }

    public int getNombreTotalBlocs() {
        return this.nombreTotalBlocs;
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

    public void sauvegarderProjet(String cheminFichier) throws java.io.IOException {
        java.util.List<byte[]> imagesSer = new java.util.ArrayList<>();
        if (this.imagesVues != null) {
            for (BufferedImage img : this.imagesVues) {
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                javax.imageio.ImageIO.write(img, "PNG", baos);
                imagesSer.add(baos.toByteArray());
            }
        }
        
        ProjetData data = new ProjetData(this.batiment, this.indexVueCourante, imagesSer);
        
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(new java.io.FileOutputStream(cheminFichier))) {
            oos.writeObject(data);
        }
    }

    public void chargerProjet(String cheminFichier) throws java.io.IOException, ClassNotFoundException {
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.FileInputStream(cheminFichier))) {
            ProjetData data = (ProjetData) ois.readObject();
            this.batiment = data.getBatiment();
            this.indexVueCourante = data.getIndexVueCourante();
            
            if (data.getImagesVuesPng() != null) {
                this.imagesVues = new java.util.ArrayList<>();
                for (byte[] bytes : data.getImagesVuesPng()) {
                    java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(bytes);
                    this.imagesVues.add(javax.imageio.ImageIO.read(bais));
                }
            } else {
                this.imagesVues = new java.util.ArrayList<>();
            }
            
            this.indexZoneSelectionnee = -1;
            this.commandManager.clear();
        }
    }

    private ZoneDTO convertirEnZoneDTO(Zone zone) {
        String typeZone;
        java.util.List<dto.BlocPlaceDTO> dtos = new java.util.ArrayList<>();
        if (zone instanceof ZoneBloc zb) {
            typeZone = "BLOC";
            for (ZoneBloc.BlocPlace bp : zb.getBlocsSimules()) {
                dtos.add(new dto.BlocPlaceDTO(bp.getX(), bp.getY(), bp.getLargeur(), bp.getHauteur(), bp.isRetaille()));
            }
        } else if (zone instanceof ZoneClassique) {
            typeZone = "CLASSIQUE";
        } else {
            typeZone = "OUVERTURE";
        }

        return new ZoneDTO(
                zone.getX(),
                zone.getY(),
                zone.getLargeur(),
                zone.getHauteur(),
                zone.getTypeForme().name(),
                typeZone,
                zone.getRatioCoupe(),
                dtos
        );
    }

    private void invaliderSimulationBlocs() {
        this.nombreTotalBlocs = 0;

        for (Zone zone : this.batiment.getFacadeCourante().getZones()) {
            if (zone instanceof ZoneBloc zoneBloc) {
                zoneBloc.setBlocsSimules(List.of());
            }
        }
    }
}


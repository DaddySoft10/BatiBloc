package vue;

import dto.ZoneDTO;
import vue.drawer.AfficheurBatiment;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.List;

public class DrawingPanel extends JPanel {
    private static final double FACTEUR_ZOOM = 1.12;
    private static final double ZOOM_MIN = 1.0e-6;
    private static final int SEUIL_CREATION_MIN_PX = 5;

    private final MainWindow mainWindow;
    private final AfficheurBatiment afficheur;
    private double zoomFactor;
    private double offsetX;
    private double offsetY;
    private ModeInteraction modeActuel;
    private boolean rognageEnCours;
    private int xDepartImage;
    private int yDepartImage;
    private Rectangle selectionImage;
    private Rectangle selectionAffichee;
    private boolean creationEnCours;
    private PointImage pointCreationDepartImage;
    private PointImage pointCreationCourantImage;
    private Rectangle creationAffichee;
    private boolean deplacementZoneEnCours;
    private int indexZoneDeplacement;
    private double dernierXMonde;
    private double dernierYMonde;
    private boolean tronquageEnCours;
    private int yTronquagePanel;
    private int indexZoneTronquage;

    // Resizing feature
    public enum ResizeHandle {
        TOP_LEFT, TOP, TOP_RIGHT, RIGHT, BOTTOM_RIGHT, BOTTOM, BOTTOM_LEFT, LEFT, NONE;
    }
    private boolean redimensionnementEnCours;
    private ResizeHandle poigneeActive;
    private double resizeStartXMonde;
    private double resizeStartYMonde;
    private double resizeInitialLargeur;
    private double resizeInitialHauteur;
    private double resizeInitialX;
    private double resizeInitialY;
    private dto.ZoneDTO zoneApercuRedimensionnement;

    public DrawingPanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.setBackground(Color.WHITE);
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        this.afficheur = new AfficheurBatiment();
        this.zoomFactor = 1.0;
        this.offsetX = 0.0;
        this.offsetY = 0.0;
        this.modeActuel = ModeInteraction.CREATION;
        this.rognageEnCours = false;
        this.selectionImage = null;
        this.selectionAffichee = null;
        this.creationEnCours = false;
        this.pointCreationDepartImage = null;
        this.pointCreationCourantImage = null;
        this.creationAffichee = null;
        this.deplacementZoneEnCours = false;
        this.indexZoneDeplacement = -1;
        this.dernierXMonde = 0.0;
        this.dernierYMonde = 0.0;
        this.tronquageEnCours = false;
        this.yTronquagePanel = 0;
        this.indexZoneTronquage = -1;

        this.redimensionnementEnCours = false;
        this.poigneeActive = ResizeHandle.NONE;
        this.zoneApercuRedimensionnement = null;

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {

                if (e.getButton() == MouseEvent.BUTTON1 && modeActuel == ModeInteraction.TRONQUAGE) {
                    demarrerTronquage(e.getX(), e.getY());
                    return;
                }

                if (e.getButton() == MouseEvent.BUTTON1 && modeActuel == ModeInteraction.SELECTION) {
                    ResizeHandle poignee = obtenirPoigneeSurvollee(e.getX(), e.getY());
                    if (poignee != ResizeHandle.NONE) {
                        demarrerRedimensionnement(e.getX(), e.getY(), poignee);
                    } else {
                        demarrerDeplacementZone(e.getX(), e.getY());
                    }
                }
                if (e.getButton() == MouseEvent.BUTTON1 && modeActuel == ModeInteraction.CREATION) {
                    demarrerCreationZone(e.getX(), e.getY());
                    return;
                }
                if (!estModeRognage()) {
                    return;
                }
                BufferedImage image = mainWindow.getController().getImageVueCourante();
                if (image == null) {
                    return;
                }
                RenderContext context = calculerContexteRendu(image);
                if (context == null) {
                    return;
                }
                PointImage point = convertirPointPanelVersImage(e.getX(), e.getY(), image, context, true);
                if (point == null) {
                    return;
                }


                xDepartImage = point.x;
                yDepartImage = point.y;
                rognageEnCours = true;
                selectionImage = null;
                selectionAffichee = null;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (redimensionnementEnCours) {
                    terminerRedimensionnement();
                    return;
                }

                if (deplacementZoneEnCours) {
                    terminerDeplacementZone();
                }

                if (tronquageEnCours) {
                    terminerTronquage(e.getX(), e.getY());
                    return;
                }

                if (modeActuel == ModeInteraction.CREATION && creationEnCours) {
                    terminerCreationZone(e.getX(), e.getY());
                    return;
                }

                if (!estModeRognage() || !rognageEnCours) {
                    return;
                }

                rognageEnCours = false;
                mettreAJourSelectionDepuisSouris(e.getX(), e.getY());
                
                // Auto-rogner apres le trace et retourner en mode SELECTION
                Rectangle sel = getSelectionRognageImage();
                if (sel != null && sel.width > 5 && sel.height > 5) {
                    try {
                        mainWindow.getController().rognerVueCourante(sel.x, sel.y, sel.width, sel.height);
                        effacerSelectionRognage();
                        mainWindow.activerModeSelection();
                        repaint();
                    } catch (Exception ignored) {
                        // Erreur de rognage geree silencieusement
                    }
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                gererClicSelection(e);
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                double[] coords = convertirEcranVersMonde(e.getX(), e.getY());
                if (coords != null && mainWindow != null) {
                    mainWindow.mettreAJourCoordonnees(coords[0], coords[1]);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (redimensionnementEnCours) {
                    mettreAJourRedimensionnementDepuisSouris(e.getX(), e.getY());
                    return;
                }
                if (deplacementZoneEnCours) {
                    deplacerZoneDepuisSouris(e.getX(), e.getY());
                    return;
                }
                if (modeActuel == ModeInteraction.CREATION && creationEnCours) {
                    mettreAJourCreationDepuisSouris(e.getX(), e.getY());
                    return;
                }
                if (estModeRognage() && rognageEnCours) {
                    mettreAJourSelectionDepuisSouris(e.getX(), e.getY());
                }
                if (tronquageEnCours) {
                    mettreAJourTronquageDepuisSouris(e.getX(), e.getY());
                    return;
                }
            }
        });

        this.addMouseWheelListener(this::gererZoomMolette);
    }

    public void setModeActuel(ModeInteraction mode) {
        if (mode == null) {
            return;
        }
        boolean quitteModeRognage = this.modeActuel == ModeInteraction.ROGNAGE && mode != ModeInteraction.ROGNAGE;
        boolean quitteModeCreation = this.modeActuel == ModeInteraction.CREATION && mode != ModeInteraction.CREATION;
        this.modeActuel = mode;
        this.rognageEnCours = false;
        this.annulerDeplacementZone();
        if (quitteModeRognage) {
            this.selectionImage = null;
            this.selectionAffichee = null;
        }
        if (quitteModeCreation) {
            this.annulerCreationZone();
        }
        this.repaint();
    }

    public ModeInteraction getModeActuel() {
        return this.modeActuel;
    }

    public void setModeRognageActif(boolean actif) {
        this.setModeActuel(actif ? ModeInteraction.ROGNAGE : ModeInteraction.CREATION);
    }

    public Rectangle getSelectionRognageImage() {
        if (this.selectionImage == null || this.selectionImage.width <= 1 || this.selectionImage.height <= 1) {
            return null;
        }
        return new Rectangle(this.selectionImage);
    }

    public void effacerSelectionRognage() {
        this.selectionImage = null;
        this.selectionAffichee = null;
        this.rognageEnCours = false;
        this.repaint();
    }

    public void reinitialiserVue() {
        this.zoomFactor = 1.0;
        this.offsetX = 0.0;
        this.offsetY = 0.0;
        this.repaint();
    }

    public void zoomerDepuisCentre(double facteur) {
        if (facteur <= 0.0) {
            return;
        }

        BufferedImage image = this.mainWindow.getController().getImageVueCourante();
        if (image == null) {
            return;
        }

        int[] centre = this.getCentreImageAffichee();
        if (centre == null) {
            return;
        }

        ajusterZoom(facteur, centre[0], centre[1], image);
    }

    public double getZoomFactor() {
        return this.zoomFactor;
    }

    public void definirZoomFactor(double nouveauZoom) {
        if (nouveauZoom <= 0.0) {
            throw new IllegalArgumentException("Le zoom doit etre superieur a 0.");
        }

        BufferedImage image = this.mainWindow.getController().getImageVueCourante();
        if (image == null) {
            return;
        }

        int[] centre = this.getCentreImageAffichee();
        if (centre == null) {
            return;
        }

        double facteur = nouveauZoom / this.zoomFactor;
        ajusterZoom(facteur, centre[0], centre[1], image);
    }

    public double[] convertirEcranVersMonde(int xEcran, int yEcran) {
        BufferedImage image = this.mainWindow.getController().getImageVueCourante();
        RenderContext context = this.calculerContexteRendu(image);
        if (image == null || context == null
                || context.largeur <= 0 || context.hauteur <= 0) {
            return null;
        }
        double ratioX = (xEcran - context.x) / context.largeur;
        double ratioY = (yEcran - context.y) / context.hauteur;
        double mondeX = ratioX * image.getWidth();
        double mondeY = ratioY * image.getHeight();
        return new double[]{mondeX, mondeY};
    }

    public int[] convertirMondeVersEcran(double xMonde, double yMonde) {
        BufferedImage image = this.mainWindow.getController().getImageVueCourante();
        RenderContext context = this.calculerContexteRendu(image);
        if (image == null || context == null
                || image.getWidth() <= 0 || image.getHeight() <= 0) {
            return null;
        }
        int xEcran = (int) Math.round(context.x + (xMonde / image.getWidth()) * context.largeur);
        int yEcran = (int) Math.round(context.y + (yMonde / image.getHeight()) * context.hauteur);
        return new int[]{xEcran, yEcran};
    }

    public void pannerVue(int deltaX, int deltaY) {
        this.offsetX += deltaX;
        this.offsetY += deltaY;
        this.repaint();
    }

    private void gererZoomMolette(MouseWheelEvent e) {
        BufferedImage image = this.mainWindow.getController().getImageVueCourante();
        if (image == null) {
            return;
        }
        double facteur = Math.pow(FACTEUR_ZOOM, -e.getPreciseWheelRotation());
        ajusterZoom(facteur, e.getX(), e.getY(), image);
    }

    private void ajusterZoom(double facteur, int pointZoomX, int pointZoomY, BufferedImage image) {
        RenderContext avant = this.calculerContexteRendu(image);
        if (avant == null || avant.largeur <= 0.0 || avant.hauteur <= 0.0) {
            return;
        }

        double u = (pointZoomX - avant.x) / avant.largeur;
        double v = (pointZoomY - avant.y) / avant.hauteur;
        this.zoomFactor = Math.max(ZOOM_MIN, this.zoomFactor * facteur);

        RenderContext apres = this.calculerContexteRendu(image);
        if (apres == null) {
            return;
        }

        double nouveauX = apres.x + (u * apres.largeur);
        double nouveauY = apres.y + (v * apres.hauteur);

        this.offsetX += pointZoomX - nouveauX;
        this.offsetY += pointZoomY - nouveauY;
        this.repaint();
    }

    private void demarrerCreationZone(int xPixels, int yPixels) {
        BufferedImage image = this.mainWindow.getController().getImageVueCourante();
        RenderContext context = this.calculerContexteRendu(image);
        PointImage pointImage = this.convertirPointPanelVersImage(xPixels, yPixels, image, context, false);
        if (pointImage == null) {
            return;
        }

        this.creationEnCours = true;
        this.pointCreationDepartImage = pointImage;
        this.pointCreationCourantImage = pointImage;
        this.creationAffichee = new Rectangle(xPixels, yPixels, 1, 1);
        this.repaint();
    }

    private void mettreAJourSelectionDepuisSouris(int xPanel, int yPanel) {
        BufferedImage image = this.mainWindow.getController().getImageVueCourante();
        if (image == null) {
            return;
        }
        RenderContext context = this.calculerContexteRendu(image);
        PointImage pointActuel = this.convertirPointPanelVersImage(xPanel, yPanel, image, context, true);
        if (pointActuel == null) {
            return;
        }

        int xMin = Math.min(this.xDepartImage, pointActuel.x);
        int yMin = Math.min(this.yDepartImage, pointActuel.y);
        int xMax = Math.max(this.xDepartImage, pointActuel.x);
        int yMax = Math.max(this.yDepartImage, pointActuel.y);
        this.selectionImage = new Rectangle(xMin, yMin, Math.max(1, xMax - xMin), Math.max(1, yMax - yMin));

        int xPanelMin = (int) Math.round(context.x + ((double) xMin / image.getWidth()) * context.largeur);
        int yPanelMin = (int) Math.round(context.y + ((double) yMin / image.getHeight()) * context.hauteur);
        int xPanelMax = (int) Math.round(context.x + ((double) xMax / image.getWidth()) * context.largeur);
        int yPanelMax = (int) Math.round(context.y + ((double) yMax / image.getHeight()) * context.hauteur);
        this.selectionAffichee = new Rectangle(
                xPanelMin,
                yPanelMin,
                Math.max(1, xPanelMax - xPanelMin),
                Math.max(1, yPanelMax - yPanelMin)
        );

        this.repaint();
    }

    private void mettreAJourCreationDepuisSouris(int xPanel, int yPanel) {
        BufferedImage image = this.mainWindow.getController().getImageVueCourante();
        if (image == null || this.pointCreationDepartImage == null) {
            return;
        }
        RenderContext context = this.calculerContexteRendu(image);
        PointImage pointActuel = this.convertirPointPanelVersImage(xPanel, yPanel, image, context, true);
        if (pointActuel == null) {
            return;
        }

        this.pointCreationCourantImage = pointActuel;
        this.creationAffichee = this.construireRectangleAffiche(
                context,
                image,
                this.pointCreationDepartImage,
                this.pointCreationCourantImage
        );
        this.repaint();
    }

    private void terminerCreationZone(int xPanel, int yPanel) {
        this.mettreAJourCreationDepuisSouris(xPanel, yPanel);

        Rectangle rectangleCreation = this.creationAffichee;
        PointImage pointDepart = this.pointCreationDepartImage;
        PointImage pointCourant = this.pointCreationCourantImage;

        if (rectangleCreation != null
                && pointDepart != null
                && pointCourant != null
                && rectangleCreation.width > SEUIL_CREATION_MIN_PX
                && rectangleCreation.height > SEUIL_CREATION_MIN_PX) {
            int xMin = Math.min(pointDepart.x, pointCourant.x);
            int yMin = Math.min(pointDepart.y, pointCourant.y);
            int largeur = Math.abs(pointCourant.x - pointDepart.x);
            int hauteur = Math.abs(pointCourant.y - pointDepart.y);

            if (largeur > 0 && hauteur > 0) {
                this.mainWindow.getController().ajouterZone(
                        xMin,
                        yMin,
                        largeur,
                        hauteur,
                        this.mainWindow.getFormeSaisie(),
                        this.mainWindow.getTypeZoneSelectionne(),
                        0.0

                );
                this.mainWindow.getController().selectionnerZone(
                        xMin + largeur / 2.0,
                        yMin + hauteur / 2.0
                );

                this.mainWindow.rafraichirPanneauDroit();
                this.mainWindow.mettreAJourNombreTotalBlocs();
                this.mainWindow.activerModeSelection();
            }
        }

        this.annulerCreationZone();
        this.repaint();
    }

    private Rectangle construireRectangleAffiche(
            RenderContext context,
            BufferedImage image,
            PointImage pointDepart,
            PointImage pointCourant
    ) {
        if (context == null || image == null || pointDepart == null || pointCourant == null) {
            return null;
        }

        int xMin = Math.min(pointDepart.x, pointCourant.x);
        int yMin = Math.min(pointDepart.y, pointCourant.y);
        int xMax = Math.max(pointDepart.x, pointCourant.x);
        int yMax = Math.max(pointDepart.y, pointCourant.y);

        int xPanelMin = (int) Math.round(context.x + ((double) xMin / image.getWidth()) * context.largeur);
        int yPanelMin = (int) Math.round(context.y + ((double) yMin / image.getHeight()) * context.hauteur);
        int xPanelMax = (int) Math.round(context.x + ((double) xMax / image.getWidth()) * context.largeur);
        int yPanelMax = (int) Math.round(context.y + ((double) yMax / image.getHeight()) * context.hauteur);

        return new Rectangle(
                xPanelMin,
                yPanelMin,
                Math.max(1, xPanelMax - xPanelMin),
                Math.max(1, yPanelMax - yPanelMin)
        );
    }

    private void annulerCreationZone() {
        this.creationEnCours = false;
        this.pointCreationDepartImage = null;
        this.pointCreationCourantImage = null;
        this.creationAffichee = null;
    }

    private void demarrerDeplacementZone(int xPanel, int yPanel) {
        if (this.creationEnCours || this.rognageEnCours || this.modeActuel != ModeInteraction.SELECTION) {
            return;
        }

        int indexSelectionne = this.mainWindow.getController().getIndexZoneSelectionnee();
        if (indexSelectionne < 0) {
            return;
        }

        double[] coordonneesMonde = this.convertirEcranVersMonde(xPanel, yPanel);
        if (coordonneesMonde == null
                || !this.mainWindow.getController().zoneContientPoint(indexSelectionne, coordonneesMonde[0], coordonneesMonde[1])) {
            return;
        }

        this.deplacementZoneEnCours = true;
        this.indexZoneDeplacement = indexSelectionne;
        this.dernierXMonde = coordonneesMonde[0];
        this.dernierYMonde = coordonneesMonde[1];
    }

    private void deplacerZoneDepuisSouris(int xPanel, int yPanel) {
        double[] coordonneesMonde = this.convertirEcranVersMonde(xPanel, yPanel);
        if (coordonneesMonde == null) {
            return;
        }

        double dx = coordonneesMonde[0] - this.dernierXMonde;
        double dy = coordonneesMonde[1] - this.dernierYMonde;
        if (dx == 0.0 && dy == 0.0) {
            return;
        }

        this.mainWindow.getController().deplacerZone(this.indexZoneDeplacement, dx, dy);
        this.dernierXMonde = coordonneesMonde[0];
        this.dernierYMonde = coordonneesMonde[1];
        this.mainWindow.rafraichirPanneauDroit();
        this.repaint();
    }

    private void terminerDeplacementZone() {
        this.annulerDeplacementZone();
        this.repaint();
    }

    private void annulerDeplacementZone() {
        this.deplacementZoneEnCours = false;
        this.indexZoneDeplacement = -1;
        this.dernierXMonde = 0.0;
        this.dernierYMonde = 0.0;
    }

    private void gererClicSelection(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1
                || e.getClickCount() != 1
                || this.modeActuel != ModeInteraction.SELECTION
                || this.creationEnCours
                || this.rognageEnCours) {
            return;
        }

        double[] coordonneesMonde = this.convertirEcranVersMonde(e.getX(), e.getY());
        if (coordonneesMonde == null) {
            return;
        }

        int selectionAvant = this.mainWindow.getController().getIndexZoneSelectionnee();
        int selectionApres = this.mainWindow.getController().selectionnerZone(coordonneesMonde[0], coordonneesMonde[1]);
        if (selectionApres < 0) {
            this.mainWindow.getController().deselectionnerToutesLesZones();
        }

        if (selectionAvant != this.mainWindow.getController().getIndexZoneSelectionnee()) {
            this.mainWindow.rafraichirPanneauDroit();
        }

        this.repaint();
    }

    private RenderContext calculerContexteRendu(BufferedImage imageVue) {
        if (imageVue == null) {
            return null;
        }

        int panelLargeur = this.getWidth();
        int panelHauteur = this.getHeight();
        int imageLargeur = imageVue.getWidth();
        int imageHauteur = imageVue.getHeight();
        if (panelLargeur <= 0 || panelHauteur <= 0 || imageLargeur <= 0 || imageHauteur <= 0) {
            return null;
        }

        double ratioLargeur = (double) panelLargeur / imageLargeur;
        double ratioHauteur = (double) panelHauteur / imageHauteur;
        double ratioBase = Math.min(ratioLargeur, ratioHauteur);

        double baseLargeur = imageLargeur * ratioBase;
        double baseHauteur = imageHauteur * ratioBase;
        double baseX = (panelLargeur - baseLargeur) / 2.0;
        double baseY = (panelHauteur - baseHauteur) / 2.0;

        double largeurRendue = baseLargeur * this.zoomFactor;
        double hauteurRendue = baseHauteur * this.zoomFactor;
        double xRendu = baseX + this.offsetX;
        double yRendu = baseY + this.offsetY;
        return new RenderContext(baseX, baseY, xRendu, yRendu, largeurRendue, hauteurRendue);
    }

    private PointImage convertirPointPanelVersImage(
            int xPanel,
            int yPanel,
            BufferedImage image,
            RenderContext context,
            boolean contraindreDansImage
    ) {
        if (image == null || context == null || context.largeur <= 0.0 || context.hauteur <= 0.0) {
            return null;
        }

        double ratioX = (xPanel - context.x) / context.largeur;
        double ratioY = (yPanel - context.y) / context.hauteur;

        if (!contraindreDansImage && (ratioX < 0.0 || ratioX > 1.0 || ratioY < 0.0 || ratioY > 1.0)) {
            return null;
        }

        ratioX = Math.max(0.0, Math.min(ratioX, 1.0));
        ratioY = Math.max(0.0, Math.min(ratioY, 1.0));

        int xImage = Math.max(0, Math.min((int) Math.floor(ratioX * image.getWidth()), image.getWidth() - 1));
        int yImage = Math.max(0, Math.min((int) Math.floor(ratioY * image.getHeight()), image.getHeight() - 1));
        return new PointImage(xImage, yImage);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        BufferedImage imageVue = this.mainWindow.getController().getImageVueCourante();
        RenderContext context = this.calculerContexteRendu(imageVue);
        if (imageVue != null && context != null) {
            g.drawImage(
                    imageVue,
                    (int) Math.round(context.x),
                    (int) Math.round(context.y),
                    (int) Math.round(context.largeur),
                    (int) Math.round(context.hauteur),
                    null
            );
        }

        if (imageVue != null && context != null) {
            this.dessinerZones(g, imageVue, context);
            this.dessinerZoneSelectionnee(g, imageVue, context);
            this.dessinerBlocsSimulation(g, imageVue, context);
        }

        if (this.afficheur != null) {
            int nombreZones = this.mainWindow.getController().getNombreZonesFacadeCourante();
            this.afficheur.drawBatiment(g, nombreZones);
        }

        String vueCourante = this.mainWindow.getController().getNomVueCourante();
        if (vueCourante != null && !vueCourante.isBlank()) {
            g.setColor(new Color(30, 30, 30));
            g.setFont(new Font("SansSerif", Font.BOLD, 14));
            g.drawString("Affichage: " + vueCourante, 12, 22);
        }

        if (this.estModeRognage()) {
            g.setColor(new Color(30, 144, 255));
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            g.drawString("Mode rognage actif: glisser pour selectionner une zone", 50, 98);

            if (this.selectionAffichee != null) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setStroke(new BasicStroke(2f));
                g2d.setColor(new Color(30, 144, 255));
                g2d.drawRect(
                        this.selectionAffichee.x,
                        this.selectionAffichee.y,
                        this.selectionAffichee.width,
                        this.selectionAffichee.height
                );
                g2d.setColor(new Color(30, 144, 255, 45));
                g2d.fillRect(
                        this.selectionAffichee.x,
                        this.selectionAffichee.y,
                        this.selectionAffichee.width,
                        this.selectionAffichee.height
                );
                g2d.dispose();
            }
        }

        // Afficher info zoom en bas à gauche
        this.dessinerCreationEnCours(g);
        this.dessinerRedimensionnementEnCours(g);
        this.dessinerLigneTronquage(g);
        String infoZoom = String.format("Zoom: %.0f%%", this.zoomFactor * 100);
        g.setColor(new Color(50, 50, 50, 180));
        g.fillRoundRect(8, this.getHeight() - 28, 90, 20, 6, 6);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.drawString(infoZoom, 14, this.getHeight() - 13);
    }

    private void dessinerCreationEnCours(Graphics g) {
        if (!this.creationEnCours || this.creationAffichee == null) {
            return;
        }
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(new Color(34, 139, 34, 70));
        g2d.fillRect(
                this.creationAffichee.x,
                this.creationAffichee.y,
                this.creationAffichee.width,
                this.creationAffichee.height
        );
        g2d.setStroke(new BasicStroke(2f));
        g2d.setColor(new Color(34, 139, 34));
        g2d.drawRect(
                this.creationAffichee.x,
                this.creationAffichee.y,
                this.creationAffichee.width,
                this.creationAffichee.height
        );
        g2d.dispose();
    }

    private void dessinerRedimensionnementEnCours(Graphics g) {
        if (!this.redimensionnementEnCours || this.zoneApercuRedimensionnement == null) {
            return;
        }
        
        BufferedImage imageVue = this.mainWindow.getController().getImageVueCourante();
        RenderContext context = this.calculerContexteRendu(imageVue);
        if (imageVue == null || context == null) return;
        
        double imageX = this.mainWindow.getController().convertirCoordonneeReelleEnPixels(this.zoneApercuRedimensionnement.getX());
        double imageY = this.mainWindow.getController().convertirCoordonneeReelleEnPixels(this.zoneApercuRedimensionnement.getY());
        double imageLargeur = this.zoneApercuRedimensionnement.getLargeur();
        double imageHauteur = this.zoneApercuRedimensionnement.getHauteur();

        int screenX = (int) Math.round(context.x + (imageX / imageVue.getWidth()) * context.largeur);
        int screenY = (int) Math.round(context.y + (imageY / imageVue.getHeight()) * context.hauteur);
        int screenLargeur = (int) Math.round((imageLargeur / imageVue.getWidth()) * context.largeur);
        int screenHauteur = (int) Math.round((imageHauteur / imageVue.getHeight()) * context.hauteur);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(new Color(255, 69, 0, 70)); // Orange rouge basique
        g2d.fillRect(screenX, screenY, screenLargeur, screenHauteur);
        
        float[] dash = {5.0f};
        g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
        g2d.setColor(new Color(255, 69, 0));
        g2d.drawRect(screenX, screenY, screenLargeur, screenHauteur);
        g2d.dispose();
    }

    private boolean estModeRognage() {
        return this.modeActuel == ModeInteraction.ROGNAGE;
    }

    private void dessinerZones(Graphics g, BufferedImage imageVue, RenderContext context) {
        if (g == null || imageVue == null || context == null) {
            return;
        }

        List<ZoneDTO> zones = this.mainWindow.getController().getZones();

        for (ZoneDTO zone : zones) {
            String typeForme = zone.getTypeForme();
            if (!"RECTANGULAIRE".equals(typeForme)
                    && !"TRIANGULAIRE".equals(typeForme)
                    && !"TRIANGULAIRE_TRONQUEE".equals(typeForme)) {
                continue;
            }

            Color couleurRemplissage;
            Color couleurContour;
            switch (zone.getTypeZone()) {
                case "BLOC":
                    couleurRemplissage = new Color(70, 130, 180, 80);
                    couleurContour = new Color(70, 130, 180, 255);
                    break;
                case "CLASSIQUE":
                    couleurRemplissage = new Color(34, 139, 34, 80);
                    couleurContour = new Color(34, 139, 34, 255);
                    break;
                case "OUVERTURE":
                    couleurRemplissage = new Color(255, 140, 0, 80);
                    couleurContour = new Color(255, 140, 0, 255);
                    break;
                default:
                    couleurRemplissage = new Color(128, 128, 128, 80);
                    couleurContour = new Color(128, 128, 128, 255);
                    break;
            }

            double imageX = this.mainWindow.getController().convertirCoordonneeReelleEnPixels(zone.getX());
            double imageY = this.mainWindow.getController().convertirCoordonneeReelleEnPixels(zone.getY());
            double imageLargeur = zone.getLargeur();
            double imageHauteur = zone.getHauteur();

            if ("RECTANGULAIRE".equals(typeForme)) {
                int screenX = (int) Math.round(context.x + (imageX / imageVue.getWidth()) * context.largeur);
                int screenY = (int) Math.round(context.y + (imageY / imageVue.getHeight()) * context.hauteur);
                int screenLargeur = (int) Math.round((imageLargeur / imageVue.getWidth()) * context.largeur);
                int screenHauteur = (int) Math.round((imageHauteur / imageVue.getHeight()) * context.hauteur);

                if (screenLargeur <= 0 || screenHauteur <= 0) {
                    continue;
                }

                g.setColor(couleurRemplissage);
                g.fillRect(screenX, screenY, screenLargeur, screenHauteur);

                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setStroke(new BasicStroke(2f));
                g2d.setColor(couleurContour);
                g2d.drawRect(screenX, screenY, screenLargeur, screenHauteur);
                g2d.dispose();

            } else if ("TRIANGULAIRE".equals(typeForme)) {
                int sommetX = (int) Math.round(context.x + ((imageX + imageLargeur / 2) / imageVue.getWidth()) * context.largeur);
                int sommetY = (int) Math.round(context.y + (imageY / imageVue.getHeight()) * context.hauteur);
                int basGX = (int) Math.round(context.x + (imageX / imageVue.getWidth()) * context.largeur);
                int basGY = (int) Math.round(context.y + ((imageY + imageHauteur) / imageVue.getHeight()) * context.hauteur);
                int basDX = (int) Math.round(context.x + ((imageX + imageLargeur) / imageVue.getWidth()) * context.largeur);
                int basDY = basGY;

                java.awt.Polygon triangle = new java.awt.Polygon();
                triangle.addPoint(sommetX, sommetY);
                triangle.addPoint(basGX, basGY);
                triangle.addPoint(basDX, basDY);

                g.setColor(couleurRemplissage);
                g.fillPolygon(triangle);

                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setStroke(new BasicStroke(2f));
                g2d.setColor(couleurContour);
                g2d.drawPolygon(triangle);
                g2d.dispose();

            } else if ("TRIANGULAIRE_TRONQUEE".equals(typeForme)) {
                double ratioCoupe = zone.getRatioCoupe();
                if (ratioCoupe <= 0.0) {
                    ratioCoupe = 0.5;
                }

                double largeurHautImage = imageLargeur * ratioCoupe;
                double retraitImage = (imageLargeur - largeurHautImage) / 2.0;
                double yCoupeImage = imageY + imageHauteur * ratioCoupe;

                int hautGX = (int) Math.round(context.x + ((imageX + retraitImage) / imageVue.getWidth()) * context.largeur);
                int hautDX = (int) Math.round(context.x + ((imageX + imageLargeur - retraitImage) / imageVue.getWidth()) * context.largeur);
                int hautY = (int) Math.round(context.y + (yCoupeImage / imageVue.getHeight()) * context.hauteur);

                int basGX = (int) Math.round(context.x + (imageX / imageVue.getWidth()) * context.largeur);
                int basDX = (int) Math.round(context.x + ((imageX + imageLargeur) / imageVue.getWidth()) * context.largeur);
                int basY = (int) Math.round(context.y + ((imageY + imageHauteur) / imageVue.getHeight()) * context.hauteur);

                java.awt.Polygon trapeze = new java.awt.Polygon();
                trapeze.addPoint(hautGX, hautY);
                trapeze.addPoint(hautDX, hautY);
                trapeze.addPoint(basDX, basY);
                trapeze.addPoint(basGX, basY);

                g.setColor(couleurRemplissage);
                g.fillPolygon(trapeze);

                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setStroke(new BasicStroke(2f));
                g2d.setColor(couleurContour);
                g2d.drawPolygon(trapeze);
                g2d.dispose();
            }
        }
    }

    private void dessinerZoneSelectionnee(Graphics g, BufferedImage imageVue, RenderContext context) {
        if (g == null || imageVue == null || context == null) {
            return;
        }

        int idx = this.mainWindow.getController().getIndexZoneSelectionnee();
        if (idx < 0) {
            return;
        }

        List<ZoneDTO> zones = this.mainWindow.getController().getZones();
        if (idx >= zones.size()) {
            return;
        }
        ZoneDTO zone = zones.get(idx);

        double imageX = this.mainWindow.getController().convertirCoordonneeReelleEnPixels(zone.getX());
        double imageY = this.mainWindow.getController().convertirCoordonneeReelleEnPixels(zone.getY());
        double imageLargeur = zone.getLargeur();
        double imageHauteur = zone.getHauteur();

        int screenX = (int) Math.round(context.x + (imageX / imageVue.getWidth()) * context.largeur);
        int screenY = (int) Math.round(context.y + (imageY / imageVue.getHeight()) * context.hauteur);
        int screenLargeur = (int) Math.round((imageLargeur / imageVue.getWidth()) * context.largeur);
        int screenHauteur = (int) Math.round((imageHauteur / imageVue.getHeight()) * context.hauteur);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setStroke(new BasicStroke(3f));
        g2d.setColor(Color.RED);
        g2d.drawRect(screenX, screenY, screenLargeur, screenHauteur);
        g2d.dispose();

        int taille = 8;
        int[][] poignees = {
                {screenX, screenY},
                {screenX + screenLargeur, screenY},
                {screenX, screenY + screenHauteur},
                {screenX + screenLargeur, screenY + screenHauteur},
                {screenX + screenLargeur / 2, screenY},
                {screenX + screenLargeur / 2, screenY + screenHauteur},
                {screenX, screenY + screenHauteur / 2},
                {screenX + screenLargeur, screenY + screenHauteur / 2}
        };
        for (int[] poignee : poignees) {
            int px = poignee[0] - taille / 2;
            int py = poignee[1] - taille / 2;
            Graphics2D g2dP = (Graphics2D) g.create();
            g2dP.setColor(Color.WHITE);
            g2dP.fillRect(px, py, taille, taille);
            g2dP.setStroke(new BasicStroke(1.5f));
            g2dP.setColor(Color.RED);
            g2dP.drawRect(px, py, taille, taille);
            g2dP.dispose();
        }

        String label = zone.getTypeZone() + " - " + zone.getTypeForme();
        Graphics2D g2dL = (Graphics2D) g.create();
        g2dL.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2dL.setColor(Color.RED);
        g2dL.drawString(label, screenX, screenY + screenHauteur + 15);
        g2dL.dispose();
    }

    private void dessinerBlocsSimulation(Graphics g, BufferedImage imageVue, RenderContext context) {
        if (g == null || imageVue == null || context == null) {
            return;
        }

        List<dto.ZoneDTO> zones = this.mainWindow.getController().getZones();
        if (zones == null || zones.isEmpty()) {
            return;
        }

        double echellePoucesParPixel = this.mainWindow.getController().getEchellePoucesParPixel();
        if (echellePoucesParPixel <= 0) {
            return;
        }

        for (dto.ZoneDTO zone : zones) {
            if (!zone.getTypeZone().equals("BLOC")) {
                continue;
            }

            java.util.List<dto.BlocPlaceDTO> blocs = zone.getBlocsSimules();
            if (blocs == null || blocs.isEmpty()) {
                continue;
            }

            // Calculer le clip shape pour les zones triangulaires/tronquées
            java.awt.Shape clipShape = null;
            String typeForme = zone.getTypeForme();
            
            double zClipImageX = this.mainWindow.getController().convertirCoordonneeReelleEnPixels(zone.getX());
            double zClipImageY = this.mainWindow.getController().convertirCoordonneeReelleEnPixels(zone.getY());
            double zClipImageL = zone.getLargeur() / echellePoucesParPixel;
            double zClipImageH = zone.getHauteur() / echellePoucesParPixel;
            
            if ("TRIANGULAIRE".equals(typeForme)) {
                int sommetX = (int) Math.round(context.x + ((zClipImageX + zClipImageL / 2) / imageVue.getWidth()) * context.largeur);
                int sommetY = (int) Math.round(context.y + (zClipImageY / imageVue.getHeight()) * context.hauteur);
                int basGX = (int) Math.round(context.x + (zClipImageX / imageVue.getWidth()) * context.largeur);
                int basGY = (int) Math.round(context.y + ((zClipImageY + zClipImageH) / imageVue.getHeight()) * context.hauteur);
                int basDX = (int) Math.round(context.x + ((zClipImageX + zClipImageL) / imageVue.getWidth()) * context.largeur);
                
                java.awt.Polygon triClip = new java.awt.Polygon();
                triClip.addPoint(sommetX, sommetY);
                triClip.addPoint(basGX, basGY);
                triClip.addPoint(basDX, basGY);
                clipShape = triClip;
                
            } else if ("TRIANGULAIRE_TRONQUEE".equals(typeForme)) {
                double ratioCoupe = zone.getRatioCoupe();
                if (ratioCoupe <= 0.0) ratioCoupe = 0.5;
                double largeurHautImage = zClipImageL * ratioCoupe;
                double retraitImage = (zClipImageL - largeurHautImage) / 2.0;
                double yCoupeImage = zClipImageY + zClipImageH * ratioCoupe;

                int hautGX = (int) Math.round(context.x + ((zClipImageX + retraitImage) / imageVue.getWidth()) * context.largeur);
                int hautDX = (int) Math.round(context.x + ((zClipImageX + zClipImageL - retraitImage) / imageVue.getWidth()) * context.largeur);
                int hautY = (int) Math.round(context.y + (yCoupeImage / imageVue.getHeight()) * context.hauteur);
                int basGX = (int) Math.round(context.x + (zClipImageX / imageVue.getWidth()) * context.largeur);
                int basDX = (int) Math.round(context.x + ((zClipImageX + zClipImageL) / imageVue.getWidth()) * context.largeur);
                int basY = (int) Math.round(context.y + ((zClipImageY + zClipImageH) / imageVue.getHeight()) * context.hauteur);
                
                java.awt.Polygon trapClip = new java.awt.Polygon();
                trapClip.addPoint(hautGX, hautY);
                trapClip.addPoint(hautDX, hautY);
                trapClip.addPoint(basDX, basY);
                trapClip.addPoint(basGX, basY);
                clipShape = trapClip;
            }

            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setStroke(new BasicStroke(0.8f));
            
            if (clipShape != null) {
                g2d.setClip(clipShape);
            }

            for (dto.BlocPlaceDTO bloc : blocs) {
                double bX = zone.getX() + bloc.getX();
                double bY = zone.getY() + bloc.getY();
                
                double imageX = this.mainWindow.getController().convertirCoordonneeReelleEnPixels(bX);
                double imageY = this.mainWindow.getController().convertirCoordonneeReelleEnPixels(bY);
                double imageLargeur = bloc.getLargeur() / echellePoucesParPixel;
                double imageHauteur = bloc.getHauteur() / echellePoucesParPixel;

                int screenX = (int) Math.round(context.x + (imageX / imageVue.getWidth()) * context.largeur);
                int screenY = (int) Math.round(context.y + (imageY / imageVue.getHeight()) * context.hauteur);
                int screenLargeur = (int) Math.round((imageLargeur / imageVue.getWidth()) * context.largeur);
                int screenHauteur = (int) Math.round((imageHauteur / imageVue.getHeight()) * context.hauteur);

                if (screenLargeur <= 0 || screenHauteur <= 0) {
                    continue;
                }

                if (bloc.isRetaille()) {
                    g2d.setColor(new Color(255, 100, 100, 160)); // Rouge pale pour blocs coupes
                } else {
                    g2d.setColor(new Color(255, 255, 255, 160)); // Blanc pour blocs normaux
                }

                g2d.drawRect(screenX, screenY, screenLargeur, screenHauteur);
            }

            g2d.dispose();

            String texteBlocs = blocs.size() + " blocs";
            Graphics2D g2dT = (Graphics2D) g.create();
            g2dT.setFont(new Font("SansSerif", Font.BOLD, 12));
            
            // Centrage basique sur la base de la zone (pas des blocs)
            double zoneImageX = this.mainWindow.getController().convertirCoordonneeReelleEnPixels(zone.getX());
            double zoneImageY = this.mainWindow.getController().convertirCoordonneeReelleEnPixels(zone.getY());
            double zoneImageLargeur = zone.getLargeur() / echellePoucesParPixel;
            double zoneImageHauteur = zone.getHauteur() / echellePoucesParPixel;

            int zScreenX = (int) Math.round(context.x + (zoneImageX / imageVue.getWidth()) * context.largeur);
            int zScreenY = (int) Math.round(context.y + (zoneImageY / imageVue.getHeight()) * context.hauteur);
            int zScreenLargeur = (int) Math.round((zoneImageLargeur / imageVue.getWidth()) * context.largeur);
            int zScreenHauteur = (int) Math.round((zoneImageHauteur / imageVue.getHeight()) * context.hauteur);

            java.awt.FontMetrics fm = g2dT.getFontMetrics();
            int texteLargeur = fm.stringWidth(texteBlocs);
            int texteX = zScreenX + (zScreenLargeur - texteLargeur) / 2;
            int texteY = zScreenY + (zScreenHauteur / 2);

            g2dT.setColor(Color.BLACK);
            g2dT.drawString(texteBlocs, texteX + 1, texteY + 1);
            g2dT.setColor(Color.WHITE);
            g2dT.drawString(texteBlocs, texteX, texteY);
            g2dT.dispose();
        }
    }

    private ResizeHandle obtenirPoigneeSurvollee(int xPanel, int yPanel) {
        int idx = this.mainWindow.getController().getIndexZoneSelectionnee();
        if (idx < 0) return ResizeHandle.NONE;

        List<ZoneDTO> zones = this.mainWindow.getController().getZones();
        if (idx >= zones.size()) return ResizeHandle.NONE;

        ZoneDTO zone = zones.get(idx);
        BufferedImage imageVue = this.mainWindow.getController().getImageVueCourante();
        RenderContext context = this.calculerContexteRendu(imageVue);
        if (imageVue == null || context == null) return ResizeHandle.NONE;

        double imageX = this.mainWindow.getController().convertirCoordonneeReelleEnPixels(zone.getX());
        double imageY = this.mainWindow.getController().convertirCoordonneeReelleEnPixels(zone.getY());
        double imageLargeur = zone.getLargeur();
        double imageHauteur = zone.getHauteur();

        int screenX = (int) Math.round(context.x + (imageX / imageVue.getWidth()) * context.largeur);
        int screenY = (int) Math.round(context.y + (imageY / imageVue.getHeight()) * context.hauteur);
        int screenLargeur = (int) Math.round((imageLargeur / imageVue.getWidth()) * context.largeur);
        int screenHauteur = (int) Math.round((imageHauteur / imageVue.getHeight()) * context.hauteur);

        int[][] poignees = {
                {screenX, screenY},                                     // TOP_LEFT
                {screenX + screenLargeur, screenY},                     // TOP_RIGHT
                {screenX, screenY + screenHauteur},                     // BOTTOM_LEFT
                {screenX + screenLargeur, screenY + screenHauteur},     // BOTTOM_RIGHT
                {screenX + screenLargeur / 2, screenY},                 // TOP
                {screenX + screenLargeur / 2, screenY + screenHauteur}, // BOTTOM
                {screenX, screenY + screenHauteur / 2},                 // LEFT
                {screenX + screenLargeur, screenY + screenHauteur / 2}  // RIGHT
        };

        ResizeHandle[] valeurs = {
                ResizeHandle.TOP_LEFT, ResizeHandle.TOP_RIGHT, ResizeHandle.BOTTOM_LEFT, ResizeHandle.BOTTOM_RIGHT,
                ResizeHandle.TOP, ResizeHandle.BOTTOM, ResizeHandle.LEFT, ResizeHandle.RIGHT
        };

        int taille = 8;
        for (int i = 0; i < poignees.length; i++) {
            int px = poignees[i][0];
            int py = poignees[i][1];
            if (xPanel >= px - taille && xPanel <= px + taille && yPanel >= py - taille && yPanel <= py + taille) {
                return valeurs[i];
            }
        }
        return ResizeHandle.NONE;
    }

    private void demarrerRedimensionnement(int xPanel, int yPanel, ResizeHandle poignee) {
        int idx = this.mainWindow.getController().getIndexZoneSelectionnee();
        if (idx < 0) return;
        
        double[] coordsMonde = this.convertirEcranVersMonde(xPanel, yPanel);
        if (coordsMonde == null) return;

        ZoneDTO zone = this.mainWindow.getController().getZones().get(idx);
        this.redimensionnementEnCours = true;
        this.poigneeActive = poignee;
        this.resizeStartXMonde = coordsMonde[0];
        this.resizeStartYMonde = coordsMonde[1];
        this.resizeInitialLargeur = zone.getLargeur();
        this.resizeInitialHauteur = zone.getHauteur();
        this.resizeInitialX = zone.getX();
        this.resizeInitialY = zone.getY();
        
        this.zoneApercuRedimensionnement = new ZoneDTO(
                zone.getX(), zone.getY(), zone.getLargeur(), zone.getHauteur(), 
                zone.getTypeForme(), zone.getTypeZone(), zone.getRatioCoupe(), null
        );
    }

    private void mettreAJourRedimensionnementDepuisSouris(int xPanel, int yPanel) {
        if (!this.redimensionnementEnCours || this.zoneApercuRedimensionnement == null) return;

        double[] coordsMonde = this.convertirEcranVersMonde(xPanel, yPanel);
        if (coordsMonde == null) return;

        double deltaX = coordsMonde[0] - this.resizeStartXMonde;
        double deltaY = coordsMonde[1] - this.resizeStartYMonde;

        double minSize = 1.0;
        double newX = this.resizeInitialX;
        double newY = this.resizeInitialY;
        double newWidth = this.resizeInitialLargeur;
        double newHeight = this.resizeInitialHauteur;

        switch (this.poigneeActive) {
            case RIGHT:
                newWidth = Math.max(minSize, this.resizeInitialLargeur + deltaX);
                break;
            case BOTTOM:
                newHeight = Math.max(minSize, this.resizeInitialHauteur + deltaY);
                break;
            case BOTTOM_RIGHT:
                newWidth = Math.max(minSize, this.resizeInitialLargeur + deltaX);
                newHeight = Math.max(minSize, this.resizeInitialHauteur + deltaY);
                break;
            case LEFT:
                newWidth = Math.max(minSize, this.resizeInitialLargeur - deltaX);
                newX = this.resizeInitialX + (this.resizeInitialLargeur - newWidth);
                break;
            case TOP:
                newHeight = Math.max(minSize, this.resizeInitialHauteur - deltaY);
                newY = this.resizeInitialY + (this.resizeInitialHauteur - newHeight);
                break;
            case TOP_LEFT:
                newWidth = Math.max(minSize, this.resizeInitialLargeur - deltaX);
                newX = this.resizeInitialX + (this.resizeInitialLargeur - newWidth);
                newHeight = Math.max(minSize, this.resizeInitialHauteur - deltaY);
                newY = this.resizeInitialY + (this.resizeInitialHauteur - newHeight);
                break;
            case TOP_RIGHT:
                newWidth = Math.max(minSize, this.resizeInitialLargeur + deltaX);
                newHeight = Math.max(minSize, this.resizeInitialHauteur - deltaY);
                newY = this.resizeInitialY + (this.resizeInitialHauteur - newHeight);
                break;
            case BOTTOM_LEFT:
                newWidth = Math.max(minSize, this.resizeInitialLargeur - deltaX);
                newX = this.resizeInitialX + (this.resizeInitialLargeur - newWidth);
                newHeight = Math.max(minSize, this.resizeInitialHauteur + deltaY);
                break;
            default:
                break;
        }

        this.zoneApercuRedimensionnement = new ZoneDTO(
                newX, newY, newWidth, newHeight,
                this.zoneApercuRedimensionnement.getTypeForme(),
                this.zoneApercuRedimensionnement.getTypeZone(),
                this.zoneApercuRedimensionnement.getRatioCoupe(), null
        );
        this.repaint();
    }

    private void terminerRedimensionnement() {
        if (!this.redimensionnementEnCours || this.zoneApercuRedimensionnement == null) return;
        
        int idx = this.mainWindow.getController().getIndexZoneSelectionnee();
        if (idx >= 0) {
            this.mainWindow.getController().modifierZone(
                    idx,
                    this.zoneApercuRedimensionnement.getX(),
                    this.zoneApercuRedimensionnement.getY(),
                    this.zoneApercuRedimensionnement.getLargeur(),
                    this.zoneApercuRedimensionnement.getHauteur(),
                    this.zoneApercuRedimensionnement.getTypeForme(),
                    this.zoneApercuRedimensionnement.getTypeZone(),
                    this.zoneApercuRedimensionnement.getRatioCoupe()
            );
            this.mainWindow.rafraichirPanneauDroit();
            this.mainWindow.mettreAJourNombreTotalBlocs();
        }

        this.redimensionnementEnCours = false;
        this.poigneeActive = ResizeHandle.NONE;
        this.zoneApercuRedimensionnement = null;
        this.repaint();
    }

    private static class PointImage {
        private final int x;
        private final int y;

        private PointImage(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private static class RenderContext {
        private final double baseX;
        private final double baseY;
        private final double x;
        private final double y;
        private final double largeur;
        private final double hauteur;

        private RenderContext(double baseX, double baseY, double x, double y, double largeur, double hauteur) {
            this.baseX = baseX;
            this.baseY = baseY;
            this.x = x;
            this.y = y;
            this.largeur = largeur;
            this.hauteur = hauteur;
        }
    }

    private int[] getCentreImageAffichee() {
        BufferedImage image = this.mainWindow.getController().getImageVueCourante();
        if (image == null) {
            return null;
        }

        RenderContext context = this.calculerContexteRendu(image);
        if (context == null) {
            return null;
        }

        int centreX = (int) Math.round(context.x + context.largeur / 2.0);
        int centreY = (int) Math.round(context.y + context.hauteur / 2.0);

        return new int[]{centreX, centreY};
    }

    private void dessinerLigneTronquage(Graphics g) {
        if (!this.tronquageEnCours) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawLine(0, this.yTronquagePanel, this.getWidth(), this.yTronquagePanel);
        g2d.dispose();
    }

    private void demarrerTronquage(int xPanel, int yPanel) {
        if (this.creationEnCours || this.rognageEnCours || this.deplacementZoneEnCours
                || this.modeActuel != ModeInteraction.TRONQUAGE) {
            return;
        }

        int indexSelectionne = this.mainWindow.getController().getIndexZoneSelectionnee();
        if (indexSelectionne < 0) {
            return;
        }

        ZoneDTO zone = this.mainWindow.getController().getZoneSelectionnee();
        if (zone == null) {
            return;
        }

        if (!"TRIANGULAIRE".equals(zone.getTypeForme())
                && !"TRIANGULAIRE_TRONQUEE".equals(zone.getTypeForme())) {
            return;
        }

        double[] coordonneesMonde = this.convertirEcranVersMonde(xPanel, yPanel);
        if (coordonneesMonde == null) {
            return;
        }

        if (!this.mainWindow.getController().zoneContientPoint(
                indexSelectionne,
                coordonneesMonde[0],
                coordonneesMonde[1])) {
            return;
        }

        this.tronquageEnCours = true;
        this.indexZoneTronquage = indexSelectionne;

        int[] coinHautGauche = this.convertirMondeVersEcran(
                this.mainWindow.getController().convertirCoordonneeReelleEnPixels(zone.getX()),
                this.mainWindow.getController().convertirCoordonneeReelleEnPixels(zone.getY())
        );

        int[] coinBasDroit = this.convertirMondeVersEcran(
                this.mainWindow.getController().convertirCoordonneeReelleEnPixels(zone.getX() + zone.getLargeur()),
                this.mainWindow.getController().convertirCoordonneeReelleEnPixels(zone.getY() + zone.getHauteur())
        );

        if (coinHautGauche == null || coinBasDroit == null) {
            this.tronquageEnCours = false;
            this.indexZoneTronquage = -1;
            return;
        }

        int yHaut = coinHautGauche[1];
        int yBas = coinBasDroit[1];

        this.yTronquagePanel = Math.max(yHaut, Math.min(yPanel, yBas));
        this.repaint();
    }

    private void mettreAJourTronquageDepuisSouris(int xPanel, int yPanel) {
        if (!this.tronquageEnCours || this.indexZoneTronquage < 0) {
            return;
        }

        ZoneDTO zone = this.mainWindow.getController().getZoneSelectionnee();
        if (zone == null) {
            return;
        }

        int[] coinHautGauche = this.convertirMondeVersEcran(
                this.mainWindow.getController().convertirCoordonneeReelleEnPixels(zone.getX()),
                this.mainWindow.getController().convertirCoordonneeReelleEnPixels(zone.getY())
        );

        int[] coinBasDroit = this.convertirMondeVersEcran(
                this.mainWindow.getController().convertirCoordonneeReelleEnPixels(zone.getX() + zone.getLargeur()),
                this.mainWindow.getController().convertirCoordonneeReelleEnPixels(zone.getY() + zone.getHauteur())
        );

        if (coinHautGauche == null || coinBasDroit == null) {
            return;
        }

        int yHaut = coinHautGauche[1];
        int yBas = coinBasDroit[1];

        this.yTronquagePanel = Math.max(yHaut, Math.min(yPanel, yBas));
        this.repaint();
    }

    private void terminerTronquage(int xPanel, int yPanel) {
        if (!this.tronquageEnCours || this.indexZoneTronquage < 0) {
            return;
        }

        ZoneDTO zone = this.mainWindow.getController().getZoneSelectionnee();
        if (zone == null) {
            this.tronquageEnCours = false;
            this.indexZoneTronquage = -1;
            this.repaint();
            return;
        }

        int[] coinHautGauche = this.convertirMondeVersEcran(
                this.mainWindow.getController().convertirCoordonneeReelleEnPixels(zone.getX()),
                this.mainWindow.getController().convertirCoordonneeReelleEnPixels(zone.getY())
        );

        int[] coinBasDroit = this.convertirMondeVersEcran(
                this.mainWindow.getController().convertirCoordonneeReelleEnPixels(zone.getX() + zone.getLargeur()),
                this.mainWindow.getController().convertirCoordonneeReelleEnPixels(zone.getY() + zone.getHauteur())
        );

        if (coinHautGauche == null || coinBasDroit == null) {
            this.tronquageEnCours = false;
            this.indexZoneTronquage = -1;
            this.repaint();
            return;
        }

        int yHaut = coinHautGauche[1];
        int yBas = coinBasDroit[1];
        int hauteurScreen = yBas - yHaut;

        if (hauteurScreen <= 0) {
            this.tronquageEnCours = false;
            this.indexZoneTronquage = -1;
            this.repaint();
            return;
        }

        double ratioCoupeLigne = (double) (this.yTronquagePanel - yHaut) / hauteurScreen;
        ratioCoupeLigne = Math.max(0.05, Math.min(ratioCoupeLigne, 0.95));

        double ratioActuel = zone.getRatioCoupe();
        double nouveauRatioCoupe = Math.max(ratioActuel, ratioCoupeLigne);
        nouveauRatioCoupe = Math.max(0.05, Math.min(nouveauRatioCoupe, 0.95));

        this.mainWindow.getController().modifierZone(
                this.indexZoneTronquage,
                zone.getX(),
                zone.getY(),
                zone.getLargeur(),
                zone.getHauteur(),
                "TRIANGULAIRE_TRONQUEE",
                zone.getTypeZone(),
                nouveauRatioCoupe
        );

        this.tronquageEnCours = false;
        this.indexZoneTronquage = -1;
        this.mainWindow.rafraichirPanneauDroit();
        this.mainWindow.mettreAJourNombreTotalBlocs();
        this.repaint();
    }

    /**
     * Dessine les zones et blocs directement sur un Graphics2D d'export (pour le PNG).
     * Le contexte de rendu utilise l'image a taille reelle (1:1).
     */
    public void dessinerZonesEtBlocsSurImage(java.awt.Graphics2D g2d, java.awt.image.BufferedImage imageVue) {
        if (g2d == null || imageVue == null) return;
        
        // Creer un contexte 1:1 (l'image fait exactement sa taille)
        RenderContext ctx = new RenderContext(0, 0, 0, 0, imageVue.getWidth(), imageVue.getHeight());
        
        this.dessinerZones(g2d, imageVue, ctx);
        this.dessinerBlocsSimulation(g2d, imageVue, ctx);
    }
}

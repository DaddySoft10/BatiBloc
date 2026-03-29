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

    private final MainWindow mainWindow;
    private final AfficheurBatiment afficheur;
    private double zoomFactor;
    private double offsetX;
    private double offsetY;
    private boolean modeRognageActif;
    private boolean rognageEnCours;
    private int xDepartImage;
    private int yDepartImage;
    private Rectangle selectionImage;
    private Rectangle selectionAffichee;

    public DrawingPanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.setBackground(Color.WHITE);
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        this.afficheur = new AfficheurBatiment();
        this.zoomFactor = 1.0;
        this.offsetX = 0.0;
        this.offsetY = 0.0;
        this.modeRognageActif = false;
        this.rognageEnCours = false;
        this.selectionImage = null;
        this.selectionAffichee = null;

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!modeRognageActif) {
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
                if (!modeRognageActif || !rognageEnCours) {
                    return;
                }
                rognageEnCours = false;
                mettreAJourSelectionDepuisSouris(e.getX(), e.getY());
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (modeRognageActif || e.getButton() != MouseEvent.BUTTON1) {
                    return;
                }
                gererClicSouris(e.getX(), e.getY());
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (modeRognageActif && rognageEnCours) {
                    mettreAJourSelectionDepuisSouris(e.getX(), e.getY());
                }
            }
        });

        this.addMouseWheelListener(this::gererZoomMolette);
    }

    public void setModeRognageActif(boolean actif) {
        this.modeRognageActif = actif;
        this.rognageEnCours = false;
        if (!actif) {
            this.selectionImage = null;
            this.selectionAffichee = null;
        }
        this.repaint();
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

        int centreX = this.getWidth() / 2;
        int centreY = this.getHeight() / 2;
        ajusterZoom(facteur, centreX, centreY, image);
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
        int xEcran = (int) Math.round(context.x + (xMonde / image.getWidth())  * context.largeur);
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

        double nouveauX = apres.baseX + (u * apres.largeur);
        double nouveauY = apres.baseY + (v * apres.hauteur);
        this.offsetX += pointZoomX - nouveauX;
        this.offsetY += pointZoomY - nouveauY;
        this.repaint();
    }

    private void gererClicSouris(int xPixels, int yPixels) {
        BufferedImage image = this.mainWindow.getController().getImageVueCourante();
        RenderContext context = this.calculerContexteRendu(image);
        PointImage pointImage = this.convertirPointPanelVersImage(xPixels, yPixels, image, context, false);
        if (pointImage == null) {
            return;
        }

        double largeur = this.mainWindow.getLargeurSaisie();
        double hauteur = this.mainWindow.getHauteurSaisie();
        String forme = this.mainWindow.getFormeSaisie();
        String typeZone = this.mainWindow.getTypeZoneSelectionne();

        this.mainWindow.getController().ajouterZone(pointImage.x, pointImage.y, largeur, hauteur, forme, typeZone);
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

        if (this.modeRognageActif) {
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
        String infoZoom = String.format("Zoom: %.0f%%", this.zoomFactor * 100);
        g.setColor(new Color(50, 50, 50, 180));
        g.fillRoundRect(8, this.getHeight() - 28, 90, 20, 6, 6);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.drawString(infoZoom, 14, this.getHeight() - 13);
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
                int basGX   = (int) Math.round(context.x + (imageX / imageVue.getWidth()) * context.largeur);
                int basGY   = (int) Math.round(context.y + ((imageY + imageHauteur) / imageVue.getHeight()) * context.hauteur);
                int basDX   = (int) Math.round(context.x + ((imageX + imageLargeur) / imageVue.getWidth()) * context.largeur);
                int basDY   = basGY;

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
                double retraitImage = imageLargeur * 0.25;
                int retraitScreen = (int) Math.round((retraitImage / imageVue.getWidth()) * context.largeur);

                int xBase      = (int) Math.round(context.x + (imageX / imageVue.getWidth()) * context.largeur);
                int yBase      = (int) Math.round(context.y + (imageY / imageVue.getHeight()) * context.hauteur);
                int xBaseDroit = (int) Math.round(context.x + ((imageX + imageLargeur) / imageVue.getWidth()) * context.largeur);
                int yBasBas    = (int) Math.round(context.y + ((imageY + imageHauteur) / imageVue.getHeight()) * context.hauteur);

                int hautGX = xBase + retraitScreen;
                int hautGY = yBase;
                int hautDX = xBaseDroit - retraitScreen;
                int hautDY = yBase;
                int basGX  = xBase;
                int basGY  = yBasBas;
                int basDX  = xBaseDroit;
                int basDY  = yBasBas;

                java.awt.Polygon trapeze = new java.awt.Polygon();
                trapeze.addPoint(hautGX, hautGY);
                trapeze.addPoint(hautDX, hautDY);
                trapeze.addPoint(basDX, basDY);
                trapeze.addPoint(basGX, basGY);

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

        // NOTE: Controller.getIndexZoneSelectionnee() n'existe pas encore.
        // La valeur par defaut -1 est utilisee jusqu'a ce que ce getter soit ajoute au Controller.
        int idx = -1;
        // int idx = this.mainWindow.getController().getIndexZoneSelectionnee();
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
            {screenX,                      screenY},
            {screenX + screenLargeur,      screenY},
            {screenX,                      screenY + screenHauteur},
            {screenX + screenLargeur,      screenY + screenHauteur},
            {screenX + screenLargeur / 2,  screenY},
            {screenX + screenLargeur / 2,  screenY + screenHauteur},
            {screenX,                      screenY + screenHauteur / 2},
            {screenX + screenLargeur,      screenY + screenHauteur / 2}
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

        double metresParPixel = this.mainWindow.getController().getMetresParPixel();
        if (metresParPixel <= 0) {
            return;
        }

        for (dto.ZoneDTO zone : zones) {
            if (!zone.getTypeZone().equals("BLOC")) {
                continue;
            }

            double imageX       = this.mainWindow.getController().convertirCoordonneeReelleEnPixels(zone.getX());
            double imageY       = this.mainWindow.getController().convertirCoordonneeReelleEnPixels(zone.getY());
            double imageLargeur = zone.getLargeur() / metresParPixel;
            double imageHauteur = zone.getHauteur() / metresParPixel;

            int screenX       = (int) Math.round(context.x + (imageX / imageVue.getWidth())          * context.largeur);
            int screenY       = (int) Math.round(context.y + (imageY / imageVue.getHeight())         * context.hauteur);
            int screenLargeur = (int) Math.round((imageLargeur / imageVue.getWidth())  * context.largeur);
            int screenHauteur = (int) Math.round((imageHauteur / imageVue.getHeight()) * context.hauteur);

            if (screenLargeur <= 0 || screenHauteur <= 0) {
                continue;
            }

            double poucesParMetre     = 39.3701;
            double largeurZonePouces  = zone.getLargeur() * poucesParMetre;
            double hauteurZonePouces  = zone.getHauteur() * poucesParMetre;

            double pixelsParPouceX = (screenLargeur > 0 && largeurZonePouces > 0)
                    ? screenLargeur / largeurZonePouces : 1.0;
            double pixelsParPouceY = (screenHauteur > 0 && hauteurZonePouces > 0)
                    ? screenHauteur / hauteurZonePouces : 1.0;

            double blocLargeurPx = 12.0 * pixelsParPouceX;
            double blocHauteurPx =  8.0 * pixelsParPouceY;

            if (blocLargeurPx < 2 || blocHauteurPx < 2) {
                continue;
            }

            int nbColonnes = (int) Math.max(1, Math.floor(largeurZonePouces / 12.0));
            int nbRangees  = (int) Math.max(1, Math.floor(hauteurZonePouces /  8.0));
            int nbBlocs    = nbColonnes * nbRangees;

            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setStroke(new BasicStroke(0.8f));
            g2d.setColor(new Color(255, 255, 255, 160));

            for (int col = 1; col < nbColonnes; col++) {
                int xLigne = screenX + (int) Math.round(col * blocLargeurPx);
                g2d.drawLine(xLigne, screenY, xLigne, screenY + screenHauteur);
            }

            for (int row = 1; row < nbRangees; row++) {
                int yLigne = screenY + (int) Math.round(row * blocHauteurPx);
                g2d.drawLine(screenX, yLigne, screenX + screenLargeur, yLigne);
            }

            g2d.dispose();

            String texteBlocs = nbBlocs + " blocs";
            Graphics2D g2dT = (Graphics2D) g.create();
            g2dT.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2dT.setColor(Color.WHITE);

            java.awt.FontMetrics fm = g2dT.getFontMetrics();
            int texteLargeur = fm.stringWidth(texteBlocs);
            int texteX = screenX + (screenLargeur - texteLargeur) / 2;
            int texteY = screenY + (screenHauteur / 2);

            g2dT.setColor(Color.BLACK);
            g2dT.drawString(texteBlocs, texteX + 1, texteY + 1);
            g2dT.setColor(Color.WHITE);
            g2dT.drawString(texteBlocs, texteX, texteY);
            g2dT.dispose();
        }
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
}

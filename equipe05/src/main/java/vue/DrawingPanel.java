package vue;

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
        double largeur = this.mainWindow.getLargeurSaisie();
        double hauteur = this.mainWindow.getHauteurSaisie();
        String forme = this.mainWindow.getFormeSaisie();
        String typeZone = this.mainWindow.getTypeZoneSelectionne();

        this.mainWindow.getController().ajouterZone(xPixels, yPixels, largeur, hauteur, forme, typeZone);
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

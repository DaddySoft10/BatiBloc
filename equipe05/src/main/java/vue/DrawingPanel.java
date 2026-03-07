package vue;

import vue.drawer.AfficheurBatiment;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DrawingPanel extends JPanel {
    private final MainWindow mainWindow;
    private final AfficheurBatiment afficheur;

    public DrawingPanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.setBackground(Color.WHITE);
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        this.afficheur = new AfficheurBatiment();

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                gererClicSouris(e.getX(), e.getY());
            }
        });
    }

    private void gererClicSouris(int xPixels, int yPixels) {
        double largeur = this.mainWindow.getLargeurSaisie();
        double hauteur = this.mainWindow.getHauteurSaisie();
        String forme = this.mainWindow.getFormeSaisie();
        String typeZone = this.mainWindow.getTypeZoneSelectionne();

        double xPouces = (double) xPixels;
        double yPouces = (double) yPixels;

        this.mainWindow.getController().ajouterZone(xPouces, yPouces, largeur, hauteur, forme, typeZone);
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        BufferedImage imageVue = this.mainWindow.getController().getImageVueCourante();
        if (imageVue != null) {
            int panelLargeur = this.getWidth();
            int panelHauteur = this.getHeight();
            int imageLargeur = imageVue.getWidth();
            int imageHauteur = imageVue.getHeight();

            double ratioLargeur = (double) panelLargeur / imageLargeur;
            double ratioHauteur = (double) panelHauteur / imageHauteur;
            double ratio = Math.min(ratioLargeur, ratioHauteur);

            int largeurDessinee = (int) Math.round(imageLargeur * ratio);
            int hauteurDessinee = (int) Math.round(imageHauteur * ratio);
            int x = (panelLargeur - largeurDessinee) / 2;
            int y = (panelHauteur - hauteurDessinee) / 2;

            g.drawImage(imageVue, x, y, largeurDessinee, hauteurDessinee, null);
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
    }
}

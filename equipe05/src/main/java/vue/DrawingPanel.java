package vue;

import vue.drawer.AfficheurBatiment;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DrawingPanel extends JPanel {
    private final MainWindow mainWindow;
    private final AfficheurBatiment afficheur;

    public DrawingPanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.setBackground(Color.WHITE);
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        this.afficheur = new AfficheurBatiment(this.mainWindow.getController().getBatiment());

        // Implémentation du diagramme de séquence 3.1
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                gererClicSouris(e.getX(), e.getY());
            }
        });
    }

    private void gererClicSouris(int xPixels, int yPixels) {
        // Récupération des données validées depuis l'interface
        double largeur = this.mainWindow.getLargeurSaisie();
        double hauteur = this.mainWindow.getHauteurSaisie();
        String forme = this.mainWindow.getFormeSaisie();
        String typeZone = this.mainWindow.getTypeZoneSelectionne();



        double xPouces = (double) xPixels; // Placeholder en attendant la logique de zoom
        double yPouces = (double) yPixels;

        // Appel au contrôleur pour instancier la zone
        this.mainWindow.getController().ajouterZone(xPouces, yPouces, largeur, hauteur, forme, typeZone);

        // Rafraîchissement de la zone de dessin pour afficher la nouvelle zone
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (this.afficheur != null) {
            this.afficheur.drawBatiment(g);
        }
    }
}
package vue.drawer;

import java.awt.Color;
import java.awt.Graphics;

public class AfficheurBatiment {
    public void drawBatiment(Graphics g, int nombreZones) {
        g.setColor(Color.BLUE);
        g.drawString("Simulation BatiBloc active - Pret pour le dessin", 50, 50);
        g.drawString("Nombre de zones: " + nombreZones, 50, 75);
    }
}
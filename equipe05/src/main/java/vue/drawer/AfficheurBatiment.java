package vue.drawer;

import domaine.Batiment;
import java.awt.*;

public class AfficheurBatiment {
    private Batiment batiment;

    public AfficheurBatiment(Batiment batiment) {
        this.batiment = batiment;
    }

    public void drawBatiment(Graphics g) {
        g.setColor(Color.BLUE);
        g.drawString("Simulation BâtiBloc Active - Prêt pour le dessin", 50, 50);
    }
}
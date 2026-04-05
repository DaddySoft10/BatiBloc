package domaine.simulation;

import domaine.TypeForme;

public final class PlacementStrategyFactory {

    private PlacementStrategyFactory() {
    }

    public static PlacementStrategy creer(TypeForme typeForme) {
        return switch (typeForme) {
            case RECTANGULAIRE -> new PlacementRectangulaireStrategy();
            case TRIANGULAIRE -> new PlacementTriangulaireStrategy();
            case TRIANGULAIRE_TRONQUEE -> new PlacementTriangulaireTronqueeStrategy();
        };
    }
}
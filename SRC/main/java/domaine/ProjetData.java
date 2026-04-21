package domaine;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ProjetData implements Serializable {
    private static final long serialVersionUID = 1L;

    private Batiment batiment;
    private int indexVueCourante;
    
    // Les images sérialisées pour s'affranchir du PDF d'origine sur le disque
    private List<byte[]> imagesVuesPng;

    public ProjetData(Batiment batiment, int indexVueCourante, List<byte[]> imagesVuesPng) {
        this.batiment = batiment;
        this.indexVueCourante = indexVueCourante;
        this.imagesVuesPng = imagesVuesPng;
    }

    public Batiment getBatiment() {
        return batiment;
    }

    public int getIndexVueCourante() {
        return indexVueCourante;
    }

    public List<byte[]> getImagesVuesPng() {
        return imagesVuesPng;
    }
}

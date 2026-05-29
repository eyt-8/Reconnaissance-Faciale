/** Importation des classes nécessaires */
import org.ejml.simple.SimpleMatrix;

/**
 * La classe Projection effectue la projection d'images sur la base des
 * eigenfaces et permet de reconstruire des images à partir de coordonnées
 * dans cet espace de caractéristiques
 *
 * @author Maël Lescoulié
 * @version 1.0
 */
public class Projection {
    /** Les eigenfaces (base et valeurs propres) utilisées pour la projection */
    private Eigenfaces eigenfaces;
    /** Coordonnées de la dernière projection effectuée (vecteur colonne) */
    private SimpleMatrix coords;

    /**
     * Constructeur principal de la projection
     * @param eigenfaces objet contenant la base d'eigenfaces et le visage moyen
     */
    public Projection(Eigenfaces eigenfaces) {
        this.eigenfaces=eigenfaces;
    }

    /**
     * Projette une image sur la base d'eigenfaces.
     * @param img l'image à projeter
     * @return un vecteur de coordonnées (coefficients) dans la base des eigenfaces
     */
    public SimpleMatrix projeter(ImageVect img) {
        img.vectoriser();
        SimpleMatrix vImage = img.getVecteurCol();
        SimpleMatrix visageMoyen = this.eigenfaces.getVisageMoyen();
        SimpleMatrix vCentre = vImage.minus(visageMoyen);
        SimpleMatrix baseEigenfaces = this.eigenfaces.getBase();
        this.coords = vCentre.mult(baseEigenfaces);
        return this.coords;
    }

    /**
     * Reconstruit une image à partir de coordonnées dans l'espace des eigenfaces.
     * @param coords vecteur de coefficients dans la base des eigenfaces
     * @return une instance d'Image reconstruite
     */
    public ImageVect reconstruire(SimpleMatrix coords) {
        SimpleMatrix baseEigenfaces = this.eigenfaces.getBase();
        SimpleMatrix vCentreReconstruit = coords.mult(baseEigenfaces.transpose());
        SimpleMatrix visageMoyen = this.eigenfaces.getVisageMoyen();
        SimpleMatrix vImagePixels = vCentreReconstruit.plus(visageMoyen);
        
        int largeur = this.eigenfaces.getLargeur();
        int longueur = this.eigenfaces.getLongueur();

        ImageVect imgReconstruite = new ImageVect(vImagePixels, largeur, longueur);
        return imgReconstruite;
    }

    /**
     * Calcule l'erreur de reconstruction entre deux images
     * @param j image originale
     * @param jp image reconstruite
     * @return la distance (erreur) entre les deux images
     */
    public double erreurReconstruction(ImageVect j, ImageVect jp) {
        j.vectoriser();
        SimpleMatrix vOriginal = j.getVecteurCol();
        jp.vectoriser();
        SimpleMatrix vReconstruit = jp.getVecteurCol();
        SimpleMatrix difference = vOriginal.minus(vReconstruit);
        double erreur = difference.normF();
        return erreur;
    }

    /**
     * Calcule la variance expliquée cumulée par les valeurs propres.
     * @return vecteur colonne contenant la variance cumulée (entre 0 et 1)
     */
    public SimpleMatrix varianceCumulee() {
        SimpleMatrix valPropres = this.eigenfaces.getValPropres();
        int taille = valPropres.getNumElements();
        SimpleMatrix varianceCumulee = new SimpleMatrix(taille,1);
        double total = valPropres.elementSum();
        double cumul = 0;
        for (int i=0 ; i<taille ; i++) {
            cumul += valPropres.get(i);
            varianceCumulee.set(i, cumul/total);
        }
        return varianceCumulee;
    }
}

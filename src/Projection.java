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
    private Acp acp;
    private SimpleMatrix coords;

    /**
     * Constructeur principal de la projection
     * @param eigenfaces objet contenant la base d'eigenfaces et le visage moyen
     */
    public Projection(Eigenfaces eigenfaces, Acp acp) {
        this.eigenfaces=eigenfaces;
        this.acp = acp;
    }

    public Eigenfaces getEigenfaces() {
        return eigenfaces;
    }

    /**
     * Projette une image sur la base d'origine en passant par la base réduite.
     * @return un vecteur de coordonnées (coefficients) dans la base "d'origine" mais ne correspondant pas à l'image (sous la base AtA)
     */
    public SimpleMatrix projection_inv_ortho() {
        SimpleMatrix liste_vp = eigenfaces.getValPropres();
        
        // nb_vp => Nombre de valeurs propres
        int nb_vp = liste_vp.getNumRows();
        
        // m_vp => matrice des valeurs propres
        SimpleMatrix m_vp = new SimpleMatrix(nb_vp,nb_vp);
        m_vp.zero();
        
        // On récupère la matrice diagonale modifiée
        
        for (int i=0;i<nb_vp;i++) {
        	if (i<=eigenfaces.getK()) {
                m_vp.set(i, i, liste_vp.get(i,0));
        	}
        	else {
        		m_vp.set(i, i, 0);
        	}
        }
        
        SimpleMatrix P = eigenfaces.getSvd().getVectPropATA();
        
        SimpleMatrix v_reduit = P.mult(m_vp).mult(P.transpose()).mult(P.transpose()).mult(this.coords);
        return v_reduit;
    }
    
    
    /* A faire
    public SimpleMatrix projection_inv() {
    	this.projection_inv_ortho();
    	
    }
    */
    
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
        // Regarder la taille de la base
        this.coords = acp.getMatrice_centree().transpose().mult(vCentre);
        
        SimpleMatrix v_ortho = new SimpleMatrix(this.coords.getNumRows(),this.coords.getNumCols());
        v_ortho.zero();
        
        SimpleMatrix vec_propres = this.eigenfaces.getSvd().getVectPropATA();
        
        // On projette sur la base orthogonale
        
        for (int i=0;i<this.coords.getNumCols();i++) {
        	v_ortho = v_ortho.plus(vec_propres.getRow(i).scale(vec_propres.getRow(i).dot(this.coords)).transpose());
        }
        this.coords = v_ortho;
        return this.coords;
    }
    
    

    /**
     * Reconstruit une image à partir de coordonnées dans l'espace des eigenfaces.
     * @param coords vecteur de coefficients dans la base des eigenfaces
     * @return une instance d'Image reconstruite
     */
    public ImageVect reconstruire(SimpleMatrix coords) {
        SimpleMatrix baseEigenfaces = this.eigenfaces.getBase();
        SimpleMatrix vCentreReconstruit = baseEigenfaces.mult(coords);
        SimpleMatrix visageMoyen = this.eigenfaces.getVisageMoyen();
        SimpleMatrix vImagePixels = vCentreReconstruit.plus(visageMoyen);
        
        int nbPixels = vImagePixels.getNumRows();
        int cote = (int) Math.sqrt(nbPixels);

        ImageVect imgReconstruite = new ImageVect(vImagePixels, cote, cote);
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

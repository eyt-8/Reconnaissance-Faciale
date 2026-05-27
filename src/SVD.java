/** @author : SOULEZ-DAMAZIE Soraya*/
//On importe la classe SimpleMatrix de la bibliothèque
import org.ejml.simple.SimpleMatrix;
//Définition de la classe SVD
public class SVD {
    
    // Attributs tels qu'écrits sur ton diagramme
    private SimpleMatrix matriceVarCov;
    private SimpleMatrix bValSinguliere;
    private SimpleMatrix vectPropATA;

    /**
     * Calcule la matrice AtA (Matrice de Variance-Covariance)
     * @param A La matrice des visages centrés
     */
    public void calculerMatriceVarCov(SimpleMatrix A) {
        // On respecte le nom de l'attribut du diagramme
        this.matriceVarCov = A.transpose().mult(A);
    }

    /**
     * Méthode présente sur notre diagramme (+)
     * Elle est censée calculer les Valeurs Singulières (BV)
     */
    public void calculerBV() {
        if (matriceVarCov == null) {
            System.out.println("Erreur : Calculez d'abord la matrice de Var-Cov !");
            return;
        }
        
        // On utilise l'outil SVD intégré à SimpleMatrix
        // .svd() décompose la matrice en U, W (valeurs singulières), et V
        var svdResult = matriceVarCov.svd();
        
        /* On remplit les attributs du diagramme avec les résultats
        * .getW() : Récupère la matrice des Valeurs Singulières.
        * .getV() : Récupère la matrice des Vecteurs Propres (ou vecteurs singuliers à droite)
        */
        this.bValSinguliere = svdResult.getW(); // Matrice diagonale des valeurs singulières
        this.vectPropATA = svdResult.getV();    // Vecteurs propres
    }

    // Getters pour que les autres classes (comme 'Propre') puissent voir les résultats
    public SimpleMatrix getMatriceVarCov() { 
    	return matriceVarCov; 
    }
    public SimpleMatrix getbValSinguliere() { 
    	return bValSinguliere; 
    }
    public SimpleMatrix getVectPropATA() { 
    	return vectPropATA; 
    }
}
	

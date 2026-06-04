package application.Abstraction;
//On importe la classe SimpleMatrix de la bibliothèque
import org.ejml.simple.SimpleMatrix;

/**@author : SOULEZ-DAMAZIE Soraya et Virgile CAUMONT*/

//Définition de la classe SVD
public class SVD {
    
    // Attributs tels qu'écrits sur ton diagramme
    private SimpleMatrix matriceVarCov;
    private SimpleMatrix bValSinguliere;
    private SimpleMatrix vectPropATA;
    private SimpleMatrix U;
    /** Matrice centrée (A) conservée pour calculer les eigenfaces (U = A * V) */
    private SimpleMatrix matriceCentree;
    
    /**On crée une instance de la classe Propre
    *SVD délègue le calcul des valeurs/vecteurs propres 
    * à cette classe spécialisée. Cela permet de séparer la logique mathématique (EIG) 
    * du traitement des données SVD.
    * */
    private Propre calculPropre;
    
    public SVD(SimpleMatrix matrice) {
    	//On initialise l'objet Propre
    	this.matriceCentree = matrice;
    	this.calculerMatriceVarCov(matrice);
    	this.calculerBV();
    	
    }
    
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
        
        calculPropre = new Propre(this.matriceVarCov);
        
        //La décomposition
        calculPropre.decomposer();
        
        //Récupération des résultats pour remplir les attributs de SVD
        
        // 1. Gestion des valeurs propres (extraction de la diagonale en vecteur colonne)
        this.bValSinguliere = calculPropre.getVectD();
        
        // 2. Gestion des vecteurs propres (Vecteurs de AtA -> Vraies Eigenfaces U)
        SimpleMatrix V = calculPropre.getMatP();
        SimpleMatrix U = this.matriceCentree.mult(V); // U = A * V
        
        // Normalisation de chaque eigenface (colonne de U)
        for (int j = 0; j < U.getNumCols(); j++) {
            SimpleMatrix colonne = U.getColumn(j);
            double norme = colonne.normF();
            if (norme > 1e-12) {
                colonne = colonne.divide(norme);
            }
            U.insertIntoThis(0, j, colonne);
        }
        
        this.vectPropATA = V;
        this.U = U;

        System.out.println("Décomposition Réussie");
    }
    
    //Les Getters 
    public SimpleMatrix getbValSinguliere() {
    	return this.bValSinguliere;
    }
    
    public SimpleMatrix getVectPropATA() {
    	return this.vectPropATA;
    }

    public SimpleMatrix getU() {
        return this.U;
    }
    
    public SimpleMatrix getMatriceVarCov() {
    	return this.matriceVarCov;
    }

    @Override
    public String toString() {
        return "SVD [matriceVarCov=" + matriceVarCov 
             + ", bValSinguliere=" + bValSinguliere 
             + ", vectPropATA=" + vectPropATA 
             + ", U=" + U
             + ", calculPropre=" + calculPropre + "]";
    }
        
        
}
	

/** @author : SOULEZ-DAMAZIE Soraya et Virgile CAUMONT*/


//On importe la classe SimpleMatrix de la bibliothèque
import org.ejml.simple.SimpleMatrix;


//Définition de la classe SVD
public class SVD {
    
    // Attributs tels qu'écrits sur ton diagramme
    private SimpleMatrix matriceVarCov;
    private SimpleMatrix bValSinguliere;
    private SimpleMatrix vectPropATA;
    
    /**On crée une instance de la classe Propre
    *SVD délègue le calcul des valeurs/vecteurs propres 
    * à cette classe spécialisée. Cela permet de séparer la logique mathématique (EIG) 
    * du traitement des données SVD.
    * */
    private Propre calculPropre;
    
    public SVD(SimpleMatrix matrice) {
    	//On initialise l'objet Propre
    	
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
        //matD -> valeurs singulières au carré
        this.bValSinguliere = calculPropre.getMatD();
        
        //matP -> vecteurs propres de AtA
        this.vectPropATA = calculPropre.getMatP();
        
        System.out.println("Décomposition Réussie");
    }
    
    //Les Getters 
    public SimpleMatrix getbValSinguliere() {
    	return this.bValSinguliere;
    }
    
    public SimpleMatrix getVectPropATA() {
    	return this.vectPropATA;
    }

	@Override
	public String toString() {
		return "SVD [matriceVarCov=" + matriceVarCov + ", bValSinguliere=" + bValSinguliere + ", vectPropATA="
				+ vectPropATA + ", calculPropre=" + calculPropre + "]";
	}
        
        
}
	

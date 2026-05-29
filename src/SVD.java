/** @author : SOULEZ-DAMAZIE Soraya*/

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
    
    public SVD() {
    	//On initialise l'objet Propre
    	this.calculPropre = new Propre();
    	
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
        
        //On donne la matrice à la classe 'Propre'
        calculPropre.setMatrice(matriceVarCov);
        
        //La décomposition
        calculPropre.decomposer();
        
        //Récupération des résultats pour remplir les attributs de SVD
        //matD -> valurs singulières au carré
        this.bValSinguliere = calculPropre.getMatD();
        
        //matP -> vecteurs propres de AtA
        this.vectPropATA = calculPropre.getMatP();
        
        System.out.println("Décomposition Ok");
    }
    
    //Les Getters 
    public SimpleMatrix getbValSinguliere() {
    	return bValSinguliere;
    }
    
    public SimpleMatrix getVectPropATA() {
    	return vectPropATA;
    }
        
}
	

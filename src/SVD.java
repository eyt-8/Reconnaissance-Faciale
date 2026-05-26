/** @author : SOULEZ-DAMAZIE Soraya*/
//On importe la classe SimpleMatrix de la bibliothèque
import org.ejml.simple.SimpleMatrix;
//Définition de la classe SVD
public class SVD {
	 // Déclaration d'un attribut privé pour stocker les valeurs singulières.
    // "v_singu" contiendra l'importance de chaque caractéristique du visage.
    private SimpleMatrix v_singu;

    // Déclaration d'un attribut privé pour stocker la matrice V de la décomposition.
    private SimpleMatrix V;

    /**
     * Cette méthode sert à faire le calcul mathématique : Transposée de A multipliée par A.
     * @param A : C'est notre matrice "assemblage" (les images centrées mises côte à côte).
     * @return : Le résultat est une matrice carrée qui montre comment les images sont liées entre elles.
     */
    public SimpleMatrix calculerAtA(SimpleMatrix A) {
        
        // 1. A.transpose() : 
        // Cette fonction crée une nouvelle matrice où les lignes de A deviennent des colonnes.
        
        // 2. .mult(A) : 
        // On prend la matrice qu'on vient de faire pivoter et on la multiplie par la matrice A originale.
        
        // 3. SimpleMatrix result : 
        // On crée une variable appelée "result" pour ranger le résultat final de ce calcul.
        SimpleMatrix result = A.transpose().mult(A);
        
        // On renvoie la matrice calculée pour qu'elle puisse être utilisée par d'autres classes
        return result;
    }
}
	

import org.ejml.simple

import org.ejml.simple.SimpleMatrix;.SimpleMatrix;

public class App {

    // // 4. Eigenfaces
    //     Eigenfaces eigenfaces = new Eigenfaces();
    //     // Attention au format retourné par svd.getbValSinguliere() : Eigenfaces.construire() s'attend 
    //     // à recevoir un vecteur colonne pour les valeurs propres, et non une matrice diagonale
    //     eigenfaces.construire(svd.getbValSinguliere(), svd.getVectPropATA()); 
    //     eigenfaces.setVisageMoyen(acp.getVisageMoyen()); // Setter à ajouter dans Eigenfaces
    //     eigenfaces.selectionnerK(0.95); // On garde 95% de la variance
    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");

        BaseDeDonnees bdd = new BaseDeDonnees();
        bdd.associerIdNom();
        List<ImageVect> images = bdd.getReferences();
        SimpleMatrix matriceComplete = bdd.matriceTot(images);
        


        // Chargement des images : matrice => vecteurs => matrices des images (Danika)
        // Requiert BDD et ImageVect
        
        // Calculer le visage moyen => centrer la matrice image (Virgile)
        // Requiert ACP
        
        // Calcul de la matrice de variance / covariance + réduction des dimensions (Remplaçants de Soraya => Maël et Nylan)
        // Requiert SVD (Maël) et Propre (Nylan) => Eigenfaces (Nylan)
        
        // Prendre image qui sera reconnue : Danika
        // Requiert : BaseDeDonnees
        
        // Reconnaître l'image : Remplaçants de Soraya => Virgile
        // Requiert : Maël (Projection) et Reconnaissance (Virgile)
        
        // Calculer l'erreur (Virgile)
        // Requiert : Comparaison (Virgile)
    }
}

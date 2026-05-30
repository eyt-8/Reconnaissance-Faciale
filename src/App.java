import java.util.ArrayList;
import java.util.List;


import org.ejml.simple.SimpleMatrix;

public class App {

    public static void main(String[] args) throws Exception {

        // Chargement des images : matrice => vecteurs => matrices des images (Danika)
        // Requiert BDD et ImageVect
        BaseDeDonnees bdd = new BaseDeDonnees();
        bdd.associerIdNom();
        List<ImageVect> images = new ArrayList<>();
        images = bdd.getReferences();
        
        SimpleMatrix matriceComplete = bdd.matriceTot(images);
        
        Acp acp = new Acp(bdd);
        
        SVD svd = new SVD(acp.getMatrice_centree());
        
        Eigenfaces faces = new Eigenfaces();
        
        faces.construire(svd.getbValSinguliere(),svd.getVectPropATA());
        
        List<ImageVect> images_test = bdd.getTests();
        
        Projection p = new Projection(faces);
        Reconnaissance rec = new Reconnaissance(bdd,p,100);
        
        // Visage moyen à récupérer
        faces.setVisageMoyen(acp.getVisage_moyen());
        
        String prediction = rec.identifier(images_test.get(0));

        // Chargement des images : matrice => vecteurs => matrices des images (Danika)
        // Requiert BDD et ImageVect
        
        // Calculer le visage moyen => centrer la matrice image (Virgile)
        // Requiert ACP
        
        // Calcul de la matrice de variance / covariance + réduction des dimensions (Remplaçants de Soraya => Maël et Nylan)
        // Requiert SVD (Maël) et Propre (Nylan) => Eigenfaces (Nylan)
        
    //     Eigenfaces eigenfaces = new Eigenfaces();
    //     // Attention au format retourné par svd.getbValSinguliere() : Eigenfaces.construire() s'attend 
    //     // à recevoir un vecteur colonne pour les valeurs propres, et non une matrice diagonale
    //     eigenfaces.construire(svd.getbValSinguliere(), svd.getVectPropATA()); 
    //     eigenfaces.setVisageMoyen(acp.getVisageMoyen()); // Setter à ajouter dans Eigenfaces
    //     eigenfaces.selectionnerK(0.95); // On garde 95% de la variance
        // Prendre image qui sera reconnue : Danika
        // Requiert : BaseDeDonnees
        
        // Reconnaître l'image : Remplaçants de Soraya => Virgile
        // Requiert : Maël (Projection) et Reconnaissance (Virgile)
        
        // Calculer l'erreur (Virgile)
        // Requiert : Comparaison (Virgile)
    }
}

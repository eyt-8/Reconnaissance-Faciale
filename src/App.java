// import java.util.List;

public class App {

    public static void main(String[] args) throws Exception {

        // Chargement des images : matrice => vecteurs => matrices des images (Danika)
        // Requiert BDD et ImageVect
        BaseDeDonnees bdd = new BaseDeDonnees();
        bdd.associerIdNom();
        
        Acp acp = new Acp(bdd);
        
        SVD svd = new SVD(acp.getMatrice_centree());
        
        Eigenfaces faces = new Eigenfaces(svd);
        
        faces.construire();
        
        // List<ImageVect> images_test = bdd.getTests();
        
        Projection p = new Projection(faces,acp);
        Reconnaissance rec = new Reconnaissance(bdd,p,Double.MAX_VALUE);
        
        // Visage moyen à récupérer
        faces.setVisageMoyen(acp.getVisage_moyen());
        
        // 1. Charger une image bien précise en indiquant son chemin d'accès
        ImageVect monImageTest = new ImageVect("donnees/test/1.jpg");
        // 2. Demander au système de l'identifier (ici en utilisant la distance de Mahalanobis)
        String prediction = rec.identifier(monImageTest, "euclidienne"); // tu peux aussi mettre "euclidienne"
        // 3. Afficher le résultat dans la console
        System.out.println("La personne reconnue sur cette image est : " + prediction);

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

// import java.util.List;

import java.util.Scanner;

import org.ejml.simple.SimpleMatrix;

public class App {

    public static void main(String[] args) throws Exception {

    	
    	System.out.println("[+] Chargement de la base de données et calcul des Eigenfaces...");
    	
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
        
        // Calcul de l'erreur de reconstitution de la base d'apprentissage
        
        
        double eqmTotal=0;
        double biaisTotal = 0;
        /*
        for (ImageVect img : bdd.getReferences()) {
            SimpleMatrix coords = p.projeter(img);
            ImageVect reconstruit = p.reconstruire(coords);
            Comparaison  comp = new Comparaison(img.getVecteurCol(), reconstruit.getVecteurCol());
            eqmTotal   += comp.calcul_eqm();
            biaisTotal += comp.biais();
        }
        */
        double eqmMoy  = eqmTotal   / bdd.getReferences().size();
        double biasMoy = biaisTotal / bdd.getReferences().size();
        
        System.out.println("Erreur de reconstitution dans la base d'apprentissage");
        
        Reconnaissance rec = new Reconnaissance(bdd,p,Double.MAX_VALUE);
        
        // Visage moyen à récupérer
        faces.setVisageMoyen(acp.getVisage_moyen());
        
        System.out.println("[+] Chargement terminé !");
        
        // 1. Charger une image bien précise en indiquant son chemin d'accès
        Scanner scanner = new Scanner(System.in);
        
        // 1. Choix de l'image de test
        System.out.println("===================================================================");
        System.out.println("Entrez le chemin de l'image de test [Défaut: donnees/test/1.jpg] : ");
        System.out.println("===================================================================");
        
        // Montre tous les choix possibles d'images (donc celles présentes dans test à l'origine)
        
        System.out.println("Sélectionnez l'image de test à analyser :");
        for (int i = 1; i <= 10; i++) {
            System.out.println(" " + i + ". Image n°" + i + " (donnees/test/" + i + ".jpg)");
        }
        
        boolean imageValide = false;
        String cheminImage = "";
        
        while (!imageValide) {
            System.out.print("Entrez le numéro de l'image (1 à 10) [Défaut: 2] : ");
            String saisie = scanner.nextLine().trim();
            
            if (saisie.isEmpty()) {
                cheminImage = "donnees/test/2.jpg";
                imageValide = true;
            } else {
                try {
                    int choixImage = Integer.parseInt(saisie);
                    if (choixImage >= 1 && choixImage <= 10) {
                        cheminImage = "donnees/test/" + choixImage + ".jpg";
                        imageValide = true;
                    } else {
                        System.out.println("[!] Numéro invalide. Choisissez un nombre entre 1 et 10.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("[!] Entrée invalide. Veuillez saisir un nombre.");
                }
            }
        }
        
        // Choix des types de distance
        
        boolean distanceValide = false;
        String typeDistance = "";
        while (!distanceValide) {
            System.out.println("-------------------------------------------------------");
            System.out.println("Choisissez la métrique de distance souhaitée :");
            System.out.println(" 1. Euclidienne");
            System.out.println(" 2. Mahalanobis");
            System.out.println(" 3. Cosinus");
            System.out.print("Votre choix (1, 2 ou 3) : ");
            
            String choix = scanner.nextLine().trim();
            
            switch (choix) {
                case "1":
                    typeDistance = "euclidienne";
                    distanceValide = true;
                    break;
                case "2":
                    typeDistance = "mahalanobis";
                    distanceValide = true;
                    break;
                case "3":
                    typeDistance = "cosinus";
                    distanceValide = true;
                    break;
                default:
                    System.out.println("[!] Choix invalide. Veuillez entrer 1, 2 ou 3.");
                    break;
            }
        }
        
        scanner.close(); // Fermeture propre du scanner
        
        System.out.println("\n[+] Analyse en cours...");
        ImageVect monImageTest = new ImageVect(cheminImage);
        // 3. Afficher le résultat dans la console
        System.out.println("===================================================================");
        System.out.println("          RÉSULTATS DE LA RECONNAISSANCE ACP           ");
        System.out.println("===================================================================");
     // Demander au système de l'identifier
        String prediction = rec.identifier(monImageTest, typeDistance); // tu peux aussi mettre "euclidienne"
        System.out.println(" Image analysée    : " + cheminImage);
        System.out.println(" Métrique choisie  : " + typeDistance.toUpperCase());
        System.out.println("-------------------------------------------------------");
        System.out.println("La personne reconnue sur cette image est : " + prediction);
        
        // On passe les eigenfaces en image
        
        System.out.println("===================================================================");
        System.out.println("         Passage des Eigenfaces en image (voir répertoire principal)           ");
        System.out.println("===================================================================\n");
        for (int i=0;i<faces.getBase().getNumCols();i++) {
        	ImageVect im = new ImageVect(faces.getBase().getColumn(i),i);
        	System.out.print("-");
        }
        
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

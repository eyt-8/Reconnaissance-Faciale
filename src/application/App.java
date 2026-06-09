package application;
import org.ejml.simple.SimpleMatrix;

import application.Abstraction.Acp;
import application.Abstraction.BaseDeDonnees;
import application.Abstraction.Comparaison;
import application.Abstraction.Eigenfaces;
import application.Abstraction.ImageVect;
import application.Abstraction.Projection;
import application.Abstraction.Reconnaissance;
import application.Abstraction.SVD;

import java.util.List;

/**
 * APPLICATION PRINCIPALE : Benchmark du système de reconnaissance faciale par ACP.
 *
 * Pour chaque seuil de variance (70 %, 80 %, 90 %, 95 %, 99 %), affiche :
 *   - K : nombre d'eigenfaces retenues
 *   - Var% : variance expliquée cumulée
 *   - EQM, REQM, Biais : erreurs de reconstruction moyennes sur la base d'apprentissage
 *   - Taux LOO : taux d'identification par validation croisée leave-one-out
 *                pour chacune des trois distances (euclidienne / cosinus / mahalanobis)
 *
 * Les images de donnees/test/ sont identifiées avec K = 95 % de variance.
 *
 * @author CAUMONT Virgile, LESCOULIÉ Maël, PAILLASSA Nylan, SOULEZ-DAMAZIE Soraya
 */
public class App {

    public static void main(String[] args) throws Exception {
    	
    	// ============================================================
        // INITIALISATION ET CHARGEMENT DES DONNÉES
        // ============================================================

        System.out.println("Benchmark Reconnaissance Faciale par ACP\n");

        // Chargement de la base et calcul ACP / SVD (fait une seule fois)
        BaseDeDonnees bdd = new BaseDeDonnees();
        Acp           acp = new Acp(bdd);
        SVD           svd = new SVD(acp.getMatrice_centree());

        List<ImageVect> refs      = bdd.getReferences();
        List<ImageVect> tests     = bdd.getTests();
        String[]        distances = {"euclidienne", "cosinus", "mahalanobis"};
        double[]        seuilsVar = {0.70, 0.80, 0.90, 0.95, 0.99};
        
        // ============================================================
        // PHASE DE TEST (BOUCLE SUR LES SEUILS DE VARIANCE)
        // ============================================================
        
        // En-tête du tableau de résultats
        System.out.printf("%-6s %-8s %-10s %-10s %-10s  %s%n",
            "K", "Var%", "EQM", "REQM", "Biais",
            "Taux LOO (eucl. / cos. / maha.)");
        System.out.println("-".repeat(78));

        for (double seuilVariance : seuilsVar) {

        	// --- Préparation de l'espace réduit ---
            Eigenfaces faces = new Eigenfaces(svd, acp.getVisage_moyen());
            faces.construire();
            faces.selectionnerK(seuilVariance);
            int k = faces.getK();

            Projection proj = new Projection(faces);

            // --- Calcul des erreurs de reconstruction ---
            double eqmTotal   = 0;
            double biaisTotal = 0;
            for (ImageVect img : refs) {
                SimpleMatrix coords      = proj.projeter(img);
                ImageVect    reconstruit = proj.reconstruire(coords);
                Comparaison  comp = new Comparaison(
                    img.getVecteurCol(), reconstruit.getVecteurCol());
                eqmTotal   += comp.calcul_eqm();
                biaisTotal += comp.biais();
            }
            double eqmMoy  = eqmTotal   / refs.size();
            double biasMoy = biaisTotal / refs.size();

            // --- Calcul du Taux d'Identification (Leave-One-Out : LOO) ---
            Reconnaissance reco = new Reconnaissance(bdd, proj);

            double[] taux = new double[distances.length];
            for (int d = 0; d < distances.length; d++) {
                taux[d] = reco.tauxIdentification(distances[d]);
            }

            // --- Affichage de la ligne de résultat ---
            System.out.printf("%-6d %-8.0f %-10.2f %-10.2f %-10.2f  %.1f%% / %.1f%% / %.1f%%%n",
                k, seuilVariance * 100,
                eqmMoy, Math.sqrt(eqmMoy), biasMoy,
                taux[0] * 100, taux[1] * 100, taux[2] * 100);
        }

        // ============================================================
        // PRÉDICTIONS SUR LES IMAGES INCONNUES (DOSSIER TEST)
        // ============================================================
        
        if (!tests.isEmpty()) {
            System.out.println("\n--- Prédictions sur donnees/test/ (K = 95 % de variance) ---");

            // Configuration optimale pour le test final
            Eigenfaces faces95 = new Eigenfaces(svd, acp.getVisage_moyen());
            faces95.construire();
            faces95.selectionnerK(0.95);
            
            Projection    proj95 = new Projection(faces95);
            Reconnaissance reco95 = new Reconnaissance(bdd, proj95);

            System.out.printf("  %-12s  %-22s  %-22s  %-22s  %-22s%n",
                "Image", "Euclidienne", "Cosinus", "Mahalanobis", "Hotelling");
            System.out.println("  " + "-".repeat(106));

            for (ImageVect imgTest : tests) {
                String predEucl = reco95.identifier(imgTest, "euclidienne");
                String predCos  = reco95.identifier(imgTest, "cosinus");
                String predMaha = reco95.identifier(imgTest, "mahalanobis");
                System.out.printf("  %-12s  %-22s  %-22s  %-22s  %-22s%n",
                    imgTest.getNom(), predEucl, predCos, predMaha);
            }
        } else {
            System.out.println("\nAucune image dans donnees/test/");
        }
    }
}
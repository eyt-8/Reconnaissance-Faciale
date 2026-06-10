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
 * Benchmark du système de reconnaissance faciale par ACP.
 *
 * Pour chaque seuil de variance (70 %, 80 %, 90 %, 95 %, 99 %), affiche :
 *   - K : nombre d'eigenfaces retenues
 *   - Var% : variance expliquée cumulée
 *   - EQM, REQM, Biais : erreurs de reconstruction moyennes sur la base d'apprentissage
 *
 * Les images de donnees/test/ sont identifiées avec K = 95 % de variance.
 * Toutes les méthodes de distance utilisent le critère de Hotelling (alpha = ALPHA_HOTELLING).
 *
 * @author CAUMONT Virgile, LESCOULIÉ Maël, PAILLASSA Nylan, SOULEZ-DAMAZIE Soraya
 */
public class App {

    public static void main(String[] args) throws Exception {

        System.out.println("Benchmark Reconnaissance Faciale par ACP\n");

        BaseDeDonnees bdd = new BaseDeDonnees("donnees/base/");
        Acp acp = new Acp(bdd);
        SVD svd = new SVD(acp.getMatriceCentree());

        List<ImageVect> refs = bdd.getReferences(); 
        List<ImageVect> tests = bdd.getTests();
        double[] seuilsVar = {0.70, 0.80, 0.90, 0.95, 0.99}; // Différents seuils de Hotelling étudiés

        // Erreurs de reconstitution par variance (donc Benchmark)

        System.out.printf("%-6s %-8s %-10s %-10s %-10s%n",
            "K", "Var%", "EQM", "REQM", "Biais");
        System.out.println("-".repeat(50));

        for (double seuilVariance : seuilsVar) {
            Eigenfaces faces = new Eigenfaces(svd, acp.getVisageMoyen());
            faces.construire();
            faces.selectionnerK(seuilVariance);
            int k = faces.getK();

            Projection proj = new Projection(faces);

            double eqmTotal = 0;
            double biaisTotal = 0;
            for (ImageVect img : refs) {
                SimpleMatrix coords = proj.projeter(img);
                ImageVect reconstruit = proj.reconstruire(coords);
                Comparaison comp = new Comparaison(
                    img.getVecteurCol(), reconstruit.getVecteurCol());
                eqmTotal += comp.calcul_eqm();
                biaisTotal += comp.biais();
            }
            double eqmMoy = eqmTotal   / refs.size();
            double biasMoy = biaisTotal / refs.size();

            System.out.printf("%-6d %-8.0f %-10.2f %-10.2f %-10.2f%n",
                k, seuilVariance * 100,
                eqmMoy, Math.sqrt(eqmMoy), biasMoy);
        }

        // Prédictions (alpha = 95%)

        Eigenfaces faces95 = new Eigenfaces(svd, acp.getVisageMoyen());
        faces95.construire();
        faces95.selectionnerK(0.95);
        Projection proj95 = new Projection(faces95);
        Reconnaissance reco95 = new Reconnaissance(bdd, proj95);

        if (!tests.isEmpty()) {
            System.out.println("\n--- Prédictions sur donnees/test/ (K = 95 % de variance, Hotelling alpha = "
                + Reconnaissance.ALPHA_HOTELLING + ") ---");
            System.out.printf("  %-12s  %-22s  %-22s  %-22s%n",
                "Image", "Euclidienne", "Cosinus", "Mahalanobis");
            System.out.println("  " + "-".repeat(82));
            for (ImageVect imgTest : tests) {
                String predEucl = reco95.identifier(imgTest, "euclidienne");
                String predCos  = reco95.identifier(imgTest, "cosinus");
                String predMaha = reco95.identifier(imgTest, "mahalanobis");
                System.out.printf("  %-12s  %-22s  %-22s  %-22s%n",
                    imgTest.getNom(), predEucl, predCos, predMaha);
            }
        }

        // Statistique de Hotelling pour les images dans Test

        double[] alphas  = {0.05, 0.10, 0.20, 0.50, 0.70, 0.90};
        int      K       = faces95.getK();
        int      n       = bdd.getNbImages();
        double   facteur = (double) K * (n - 1) / (n - K);

        // Seuils par alpha (valeurs fixes, indépendantes des images)
        System.out.println("\nSeuils Hotelling (K=" + K + ", n=" + n + ") :");
        System.out.printf("  %-10s  %-14s  %-12s%n", "alpha", "T² seuil", "F seuil");
        System.out.println("  " + "-".repeat(40));
        for (double alpha : alphas) {
            double seuil  = reco95.calculerSeuilHotelling(alpha);
            double fSeuil = seuil / facteur;
            System.out.printf("  %-10.2f  %-14.4f  %-12.4f%n", alpha, seuil, fSeuil);
        }

        // T² et prédictions par alpha pour chaque image de test
        if (!tests.isEmpty()) {
            System.out.println("\n--- Statistiques Hotelling sur donnees/test/");
            System.out.printf("  %-12s  %-10s  %-10s", "Image", "T²", "F stat");
            for (double a : alphas) System.out.printf("  %-18s", String.format("alpha=%.2f", a));
            System.out.println();
            System.out.println("  " + "-".repeat(160));

            for (ImageVect imgTest : tests) {
                String[] detail = reco95.trouverPPVAvecT2(imgTest, "cosinus");
                String   nomPPV = detail[0];
                double   t2     = Double.parseDouble(detail[1]);
                double   fStat  = t2 / facteur;
                System.out.printf("  %-12s  %-10.4f  %-10.4f", imgTest.getNom(), t2, fStat);
                for (double alpha : alphas) {
                    double seuil = reco95.calculerSeuilHotelling(alpha);
                    String pred  = (t2 <= seuil) ? nomPPV : "Inconnu";
                    System.out.printf("  %-18s", pred);
                }
                System.out.println();
            }
        }
    }
}
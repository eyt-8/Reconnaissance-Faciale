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
import java.util.ArrayList;
import java.util.List;

/**
 * APPLICATION PRINCIPALE : Benchmark du système de reconnaissance faciale par ACP.
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

        BaseDeDonnees bdd = new BaseDeDonnees();
        Acp           acp = new Acp(bdd);
        SVD           svd = new SVD(acp.getMatrice_centree());

        List<ImageVect> refs      = bdd.getReferences();
        List<ImageVect> tests     = bdd.getTests();
        double[]        seuilsVar = {0.70, 0.80, 0.90, 0.95, 0.99};

        // ============================================================
        // BENCHMARK : ERREURS DE RECONSTRUCTION PAR SEUIL DE VARIANCE
        // ============================================================

        System.out.printf("%-6s %-8s %-10s %-10s %-10s%n",
            "K", "Var%", "EQM", "REQM", "Biais");
        System.out.println("-".repeat(50));

        for (double seuilVariance : seuilsVar) {
            Eigenfaces faces = new Eigenfaces(svd, acp.getVisage_moyen());
            faces.construire();
            faces.selectionnerK(seuilVariance);
            int k = faces.getK();

            Projection proj = new Projection(faces);

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

            System.out.printf("%-6d %-8.0f %-10.2f %-10.2f %-10.2f%n",
                k, seuilVariance * 100,
                eqmMoy, Math.sqrt(eqmMoy), biasMoy);
        }

        // ============================================================
        // PRÉDICTIONS SUR LES IMAGES DE TEST (K = 95 %, AVEC HOTELLING)
        // ============================================================

        Eigenfaces faces95 = new Eigenfaces(svd, acp.getVisage_moyen());
        faces95.construire();
        faces95.selectionnerK(0.95);
        Projection     proj95 = new Projection(faces95);
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

        // ============================================================
        // LOO HOTELLING : IMPACT D'ALPHA SUR LA DÉTECTION DES INCONNUS
        // ============================================================

        double[] alphas = {0.05, 0.10, 0.20, 0.50, 0.70, 0.90};
        System.out.println("\n--- LOO Hotelling sur toutes les images de référence (euclidienne) ---");
        System.out.printf("  %-6s  %-20s", "#", "Identité réelle");
        for (double a : alphas) System.out.printf("  %-22s", String.format("alpha=%.2f", a));
        System.out.println();
        System.out.println("  " + "-".repeat(162));

        List<List<String[]>> tousResultats = new ArrayList<>();
        for (double alpha : alphas) {
            tousResultats.add(reco95.predictionsDistMin("euclidienne", alpha));
        }
        int nbImages = tousResultats.get(0).size();
        for (int i = 0; i < nbImages; i++) {
            String nomVrai = tousResultats.get(0).get(i)[0];
            System.out.printf("  %-6d  %-20s", i + 1, nomVrai);
            for (List<String[]> res : tousResultats) {
                System.out.printf("  %-22s", res.get(i)[1]);
            }
            System.out.println();
        }
    }
}
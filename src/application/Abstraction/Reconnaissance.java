package application.Abstraction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ejml.simple.SimpleMatrix;
import org.apache.commons.math3.distribution.FDistribution;

/**
 * La classe Reconnaissance gère la phase de test du système.
 * Elle permet d'identifier de nouvelles images (visages) en les projetant
 * dans l'espace des eigenfaces et en calculant la distance avec les signatures connues.
 *
 * @author SOULEZ-DAMAZIE Soraya, PAILLASSA Nylan, CAUMONT Virgile
 * @version 1.2
 */
public class Reconnaissance {

    /** Base de données contenant les images de référence et leurs signatures */
    private BaseDeDonnees baseRef;

    /** Objet gérant la projection mathématique dans l'espace réduit (ACP) */
    private Projection projection;

    /** Méthodes de distance supportées (mêmes clés que le dispatcher distance()). */
    private static final String[] METHODES = {"euclidienne", "cosinus", "mahalanobis"};

    /** Limite de distance au-delà de laquelle le visage est considéré comme inconnu,
     *  calibrée séparément pour chaque méthode (les distances cosinus/Mahalanobis
     *  ne vivent pas sur la même échelle que la distance euclidienne : un seuil
     *  unique comparé indifféremment aux trois ne serait pertinent pour aucune). */
    private Map<String, Double> seuils;

    /** Signatures pré-calculées des images de référence */
    private List<SimpleMatrix> signaturesRef;

    private double derniereDistance = 0.0;
    private double distanceMax;

    public static class DistanceIdentite implements Comparable<DistanceIdentite> {
        public String identite;
        public double distance;
        public DistanceIdentite(String identite, double distance) {
            this.identite = identite;
            this.distance = distance;
        }
        @Override
        public int compareTo(DistanceIdentite o) {
            return Double.compare(this.distance, o.distance);
        }
    }

    private List<DistanceIdentite> resultatsPrecedents = new ArrayList<>();

    public List<DistanceIdentite> getResultatsPrecedents() {
        return this.resultatsPrecedents;
    }

    /** Valeur propre minimale acceptée dans le critère de Hotelling : en dessous de
     *  ce seuil, la composante est ignorée pour éviter une division par une valeur
     *  quasi nulle (qui ferait exploser T² sans information statistique fiable). */
    private static final double LAMBDA_MIN = 1e-12;

    /**
     * Constructeur de la classe Reconnaissance.
     *
     * @param baseRef    l'instance de BaseDeDonnees contenant les signatures d'apprentissage
     * @param projection l'instance de Projection contenant la base d'Eigenfaces
     */
    public Reconnaissance(BaseDeDonnees baseRef, Projection projection) {
        this.baseRef    = baseRef;
        this.projection = projection;
        this.signaturesRef = new ArrayList<>();
        this.precalculerSignatures();
        this.calibrerSeuil();
    }

    /**
     * Projette une fois pour toutes les images de référence.
     */
    private void precalculerSignatures() {
        for (ImageVect img : this.baseRef.getReferences()) {
            this.signaturesRef.add(this.projection.projeter(img));
        }
    }

    /**
     * Tente d'identifier une image test en la comparant avec toutes les signatures de la base.
     *
     * @param test    l'image vectorisée (ImageVect) représentant le visage à identifier
     * @param methode la distance utilisée (euclidienne, cosinus, mahalanobis)
     * @return le nom de la personne si elle est reconnue, ou "Inconnu" si la distance dépasse le seuil
     */
    public String identifier(ImageVect test, String methode) {
        SimpleMatrix coordonneesTest = projection.projeter(test);
        double distanceMinimale = Double.MAX_VALUE;
        String identiteTrouvee = "Inconnu";
        this.distanceMax = 0;
        this.resultatsPrecedents.clear();
        for (int i = 0; i < signaturesRef.size(); i++) {
            double d = distance(coordonneesTest, signaturesRef.get(i), methode);
            this.resultatsPrecedents.add(new DistanceIdentite(baseRef.getIdentite(i), d));
            if (d < distanceMinimale) {
                distanceMinimale = d;
                identiteTrouvee  = baseRef.getIdentite(i);
            }
            else if (d>this.distanceMax){
                this.distanceMax = d;
            }
        }
        java.util.Collections.sort(this.resultatsPrecedents);
        this.derniereDistance = distanceMinimale;
        return (distanceMinimale > seuilPour(methode)) ? "Inconnu" : identiteTrouvee;
    }

    /**
     * Identifie un visage test à l'aide du critère de Hotelling.
     *
     * Contrairement à identifier(), la décision ne s'appuie pas sur le seuil
     * empirique this.seuil (calibrerSeuil()) mais sur un seuil statistique
     * dérivé de la loi de Fisher (calculerSeuilHotelling()).
     *
     * Étapes (voir calculerSeuilHotelling pour le détail du seuil) :
     *   1. projeter le visage test -> coordsTest (K x 1)
     *   2-3. pour chaque référence j, calculer T²_j = Σ (coordsTest_i - coordsRef_i)² / λ_i
     *   4. retenir le minimum T²_min et la référence j* associée
     *   5. calculer T²_seuil = (K(n-1)/(n-K)) * F_{1-alpha}(K, n-K)
     *   6. décider : T²_min <= T²_seuil -> identité de j*, sinon "Inconnu"
     *
     * @param test  l'image vectorisée représentant le visage à identifier
     * @param alpha risque choisi pour le seuil de Hotelling (ex. 0.05)
     * @return le nom de la personne reconnue, ou "Inconnu" si T²_min dépasse T²_seuil
     */
    public String identifierHotelling(ImageVect test, double alpha) {
        // Étape 1 : projection du visage test -> coordsTest (K x 1, K = nb d'eigenfaces retenues)
        SimpleMatrix coordsTest = projection.projeter(test);
        int K = coordsTest.getNumRows();

        // λ_i : valeurs propres associées aux K composantes retenues (K x 1)
        SimpleMatrix lambda = projection.getEigenfaces().getValPropresK();

        // Étapes 2 à 4 : calcul de T²_j pour chaque référence, recherche du minimum
        double t2Min = Double.MAX_VALUE;
        String identiteTrouvee = "Inconnu";
        this.distanceMax = 0;
        this.resultatsPrecedents.clear();
        for (int j = 0; j < signaturesRef.size(); j++) {
            SimpleMatrix coordsRef = signaturesRef.get(j);     // K x 1
            SimpleMatrix ecart = coordsTest.minus(coordsRef);  // K x 1

            double t2 = 0.0;
            for (int i = 0; i < K; i++) {
                double lambda_i = lambda.get(i, 0);
                // Protection numérique : une valeur propre quasi nulle ferait
                // exploser (écart_i)² / λ_i sans apporter d'information fiable ;
                // on ignore alors cette composante.
                if (lambda_i < LAMBDA_MIN) continue;

                double ecart_i = ecart.get(i, 0);
                t2 += (ecart_i * ecart_i) / lambda_i;
            }

            // On alimente resultatsPrecedents/distanceMax comme identifier(),
            // afin que le panneau de reconnaissance (ressemblance, "images les
            // plus proches") fonctionne aussi avec le critère de Hotelling.
            this.resultatsPrecedents.add(new DistanceIdentite(baseRef.getIdentite(j), t2));
            if (t2 < t2Min) {
                t2Min = t2;
                identiteTrouvee = baseRef.getIdentite(j);
            } else if (t2 > this.distanceMax) {
                this.distanceMax = t2;
            }
        }
        java.util.Collections.sort(this.resultatsPrecedents);

        // Étape 5 : seuil de décision T²_seuil (formule détaillée dans calculerSeuilHotelling)
        double t2Seuil = calculerSeuilHotelling(alpha);

        this.derniereDistance = t2Min;

        // Étape 6 : décision
        return (t2Min <= t2Seuil) ? identiteTrouvee : "Inconnu";
    }

    /**
     * Calcule la distance entre les coordonnées projetées de deux visages.
     *
     * @param jp      coordonnées du visage test (K×1)
     * @param jpk     coordonnées du visage de référence (K×1)
     * @param methode euclidienne, cosinus ou mahalanobis
     * @return distance ≥ 0
     */
    public double distance(SimpleMatrix jp, SimpleMatrix jpk, String methode) {
        switch (methode) {
            case "cosinus"     -> { return distance_cosinus(jp, jpk); }
            case "mahalanobis" -> { return distance_mahalanobis(jp, jpk); }
            default            -> { return distance_euclidienne(jp, jpk); }
        }
    }

    /**
     * Distance cosinus entre deux vecteurs de coordonnées : 1 − cos(θ).
     */
    public double distance_cosinus(SimpleMatrix jp, SimpleMatrix jpk) {
        double n1 = jp.normF();
        double n2 = jpk.normF();
        if (n1 < 1e-12 || n2 < 1e-12) return 1.0;
        return 1.0 - (jp.dot(jpk) / (n1 * n2));
    }

    /**
     * Distance euclidienne entre deux vecteurs de coordonnées.
     */
    public double distance_euclidienne(SimpleMatrix jp, SimpleMatrix jpk) {
        return (jp.minus(jpk)).normF();
    }

    /**
     * Distance de Mahalanobis dans l'espace réduit (K composantes sélectionnées).
     * La matrice de covariance est diagonale : d = (jp−jpk)ᵀ × Λ⁻¹ × (jp−jpk).
     */
    public double distance_mahalanobis(SimpleMatrix jp, SimpleMatrix jpk) {
        SimpleMatrix lambdaK = projection.getEigenfaces().getValPropresK(); // K×1
        int k = lambdaK.getNumRows();

        SimpleMatrix lambdaInv = new SimpleMatrix(k, k);
        for (int i = 0; i < k; i++) {
            double lambda = lambdaK.get(i, 0);
            lambdaInv.set(i, i, (lambda > 1e-12) ? 1.0 / lambda : 0.0);
        }

        SimpleMatrix difference = jp.minus(jpk);
        return difference.transpose().mult(lambdaInv).dot(difference);
    }

    // -------------------------------------------------------------------------
    // Calibration du seuil
    // -------------------------------------------------------------------------

    /**
     * Calcule automatiquement, pour CHAQUE méthode de distance, un seuil
     * égal à 1,5 × la distance moyenne (selon cette méthode) entre toutes
     * les paires de signatures de référence.
     *
     * Un seuil par méthode est nécessaire car les distances cosinus ([0, 2])
     * et de Mahalanobis (variance-pondérée) ne vivent pas du tout sur la même
     * échelle que la distance euclidienne (somme de différences de pixels
     */
    public void calibrerSeuil() {
        this.seuils = new HashMap<>();
        for (String methode : METHODES) {
            double seuilMethode = 0;
            if (signaturesRef.size() >= 2) {
                double somme = 0;
                int count = 0;
                for (int i = 0; i < signaturesRef.size(); i++) {
                    for (int j = i + 1; j < signaturesRef.size(); j++) {
                        somme += distance(signaturesRef.get(i), signaturesRef.get(j), methode);
                        count++;
                    }
                }
                seuilMethode = (somme / count) * 1.5;
            }
            this.seuils.put(methode, seuilMethode);
        }
    }

    /**
     * Donne le seuil calibré pour une méthode de distance donnée. Retombe sur
     * le seuil de la méthode "euclidienne" si la méthode demandée est inconnue
     * (même comportement par défaut que le dispatcher distance()).
     */
    private double seuilPour(String methode) {
        return this.seuils.getOrDefault(methode, this.seuils.get("euclidienne"));
    }

    /**
     * Calcule le seuil de décision du critère de Hotelling :
     *     T²_seuil = (K(n-1) / (n-K)) * F_{1-alpha}(K, n-K)
     * où K est le nombre d'eigenfaces retenues, n le nombre d'images
     * d'apprentissage, et F_{1-alpha}(K, n-K) le quantile de la loi de
     * Fisher à (K, n-K) degrés de liberté pour le risque alpha.
     *
     * Contrairement à calibrerSeuil() (seuil empirique calé sur la distance
     * moyenne entre signatures de référence), ce seuil est dérivé
     * théoriquement de la loi de Fisher : il ne dépend que de K, n et alpha.
     *
     * @param alpha risque choisi (ex. 0.05 pour un seuil à 95 %)
     * @return T²_seuil, à comparer au T²_min calculé pour le visage test
     */
    public double calculerSeuilHotelling(double alpha) {
        int K = projection.getEigenfaces().getK();
        int n = baseRef.getNbImages();

        FDistribution loiFisher = new FDistribution(K, n - K);
        double quantile = loiFisher.inverseCumulativeProbability(1 - alpha);

        return ((double) (K * (n - 1)) / (n - K)) * quantile;
    }

    // -------------------------------------------------------------------------
    // Évaluation
    // -------------------------------------------------------------------------

    /**
     * Taux d'identification par validation croisée leave-one-out (LOO)
     * sur la base d'apprentissage.
     *
     * @param methode méthode de distance (euclidienne, cosinus, mahalanobis)
     * @return taux de bonnes identifications entre 0.0 et 1.0
     */
    public double tauxIdentification(String methode) {
        int total = signaturesRef.size();
        if (total == 0) return 0.0;

        int reussites = 0;
        for (int i = 0; i < total; i++) {
            SimpleMatrix sigI        = signaturesRef.remove(i);
            String       veriteTerrain = baseRef.getIdentite(i);

            double distMin         = Double.MAX_VALUE;
            String identiteTrouvee = "Inconnu";

            for (int j = 0; j < signaturesRef.size(); j++) {
                double d = distance(sigI, signaturesRef.get(j), methode);
                if (d < distMin) {
                    distMin         = d;
                    identiteTrouvee = baseRef.getIdentite(j < i ? j : j + 1);
                }
            }
            if (distMin <= seuilPour(methode) && identiteTrouvee.equals(veriteTerrain)) reussites++;
            signaturesRef.add(i, sigI);
        }
        return (double) reussites / total;
    }

    /**
     * Donne le seuil de validation (sous forme de réel sur lequel on prendra l'index le plus petit en dessous) 
     * Après lequel les distances seront enlevées
     * @param pourcentage   le seuil en pourcentage
     * @param listeDist     la liste de toutes les distances
     * @return              le seuil en réel
     */
    public double seuilValidation(double pourcentage,ArrayList<Double> listeDist) {
        return pourcentage*listeDist.size();
    }

    /**
     * Déclenche une série de tests sur la base de données de test et
     * affiche le taux d'identification global dans la console.
     */
    public void testerScenarios() {
        System.out.println("Lancement des scénarios de test...");
        double taux = tauxIdentification("euclidienne");
        System.out.println("Taux d'identification global : " + (taux * 100) + " %");
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /**
     * Seuil calibré pour une méthode de distance donnée
     * (euclidienne, cosinus ou mahalanobis).
     */
    public double getSeuil(String methode) {
        return seuilPour(methode);
    }

    public double getDerniereDistance() {
        return this.derniereDistance;
    }

    public double getDistanceMax() {
        return this.distanceMax;
    }
}

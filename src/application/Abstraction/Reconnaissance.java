package application.Abstraction;
import java.util.ArrayList;
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
    //private static final String[] METHODES = {"euclidienne", "cosinus", "mahalanobis"};

    /** Limite de distance au-delà de laquelle le visage est considéré comme inconnu,
     *  calibrée séparément pour chaque méthode (les distances cosinus/Mahalanobis
     *  ne vivent pas sur la même échelle que la distance euclidienne : un seuil
     *  unique comparé indifféremment aux trois ne serait pertinent pour aucune). */
    private Map<String, Double> seuils;

    /** Signatures pré-calculées des images de référence */
    private List<SimpleMatrix> signaturesRef;

    private int indexDistMin = 0;
    private double derniereDistance = 0.0;
    private double distanceMax;
    private List<DistanceIdentite> resultatsPrecedents = new ArrayList<>();

    /** Risque alpha utilisé pour le seuil du critère de Hotelling (95 %). */
    public static final double ALPHA_HOTELLING = 0.50;

    /** Valeur propre minimale acceptée dans le critère de Hotelling : en dessous de
     *  ce seuil, la composante est ignorée pour éviter une division par une valeur
     *  quasi nulle (qui ferait exploser T^2 sans information statistique fiable). */
    private static final double LAMBDA_MIN = 1e-12;


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

    public List<DistanceIdentite> getResultatsPrecedents() {
        return this.resultatsPrecedents;
    }
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
     * Trouve le plus proche voisin par la méthode de distance choisie.
     * Remplit resultatsPrecedents (classé par distance croissante) et met à jour indexDistMin.
     * Aucun seuil n'est appliqué : la décision connu/inconnu est laissée à l'appelant.
     *
     * @param test    l'image vectorisée à comparer
     * @param methode méthode de distance (euclidienne, cosinus, mahalanobis)
     */
    private void trouverPlusProche(ImageVect test, String methode) {
        SimpleMatrix coordonneesTest = projection.projeter(test);
        double distanceMinimale = Double.MAX_VALUE;
        this.distanceMax = 0;
        this.resultatsPrecedents.clear();
        for (int i = 0; i < signaturesRef.size(); i++) {
            double d = distance(coordonneesTest, signaturesRef.get(i), methode);
            this.resultatsPrecedents.add(new DistanceIdentite(baseRef.getIdentite(i), d));
            if (d < distanceMinimale) {
                distanceMinimale = d;
                this.indexDistMin = i;
            } else if (d > this.distanceMax) {
                this.distanceMax = d;
            }
        }
        java.util.Collections.sort(this.resultatsPrecedents);
        this.derniereDistance = distanceMinimale;
    }

    /**
     * Identifie une image par distance + seuil empirique (utilisé pour l'évaluation LOO).
     *
     * @param test    l'image vectorisée représentant le visage à identifier
     * @param methode la distance utilisée (euclidienne, cosinus, mahalanobis)
     * @return le nom de la personne si elle est reconnue, ou "Inconnu" si la distance dépasse le seuil
     */
    public String identifier(ImageVect test, String methode) {
        this.trouverPlusProche(test, methode);
        return baseRef.getIdentite(this.indexDistMin);
    }

    /**
     * Valide par le critère de Hotelling T² si le candidat à l'index j
     * est suffisamment proche du visage test pour être considéré comme connu.
     *
     * Cette méthode est appelée après identifier() qui a déjà trouvé
     * le plus proche voisin par distance et peuplé resultatsPrecedents.
     * Elle ne modifie pas ce classement.
     *
     * @param test  l'image vectorisée représentant le visage à identifier
     * @param alpha risque choisi pour le seuil de Hotelling (ex. 0.05)
     * @param j     index du candidat retenu par identifier()
     * @return le nom du candidat si T² ≤ seuil, "Inconnu" sinon
     */
    public String identifierHotelling(ImageVect test, double alpha, int j) {
        SimpleMatrix coordsTest = projection.projeter(test);
        int K = coordsTest.getNumRows();
        SimpleMatrix lambda = projection.getEigenfaces().getValPropresK();

        SimpleMatrix coordsRef = signaturesRef.get(j);
        SimpleMatrix ecart = coordsTest.minus(coordsRef);

        double t2 = 0.0;
        for (int i = 0; i < K; i++) {
            double lambda_i = lambda.get(i, 0);
            if (lambda_i < LAMBDA_MIN) continue;
            double ecart_i = ecart.get(i, 0);
            t2 += (ecart_i * ecart_i) / lambda_i;
        }

        this.derniereDistance = t2;

        double t2Seuil = calculerSeuilHotelling(alpha);
        return (t2 <= t2Seuil) ? baseRef.getIdentite(j) : "Inconnu";
    }

    /**
     * Calcule la distance entre les coordonnées projetées de deux visages.
     *
     * @param jp      coordonnées du visage test (Kx1)
     * @param jpk     coordonnées du visage de référence (Kx1)
     * @param methode euclidienne, cosinus ou mahalanobis
     * @return distance >= 0
     */
    public double distance(SimpleMatrix jp, SimpleMatrix jpk, String methode) {
        switch (methode) {
            case "cosinus"     -> { return distance_cosinus(jp, jpk); }
            case "mahalanobis" -> { return distance_mahalanobis(jp, jpk); }
            default            -> { return distance_euclidienne(jp, jpk); }
        }
    }

    /**
     * Distance cosinus entre deux vecteurs de coordonnées : 1 - cos(jp,jpk).
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
     * La matrice de covariance est diagonale : d = (jp-jpk)T x diag(D)^(-1) x (jp-jpk).
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

    /**
     * Contrairement à calibrerSeuil() (seuil empirique calé sur la distance
     * moyenne entre signatures de référence), ce seuil est dérivé
     * théoriquement de la loi de Fisher : il ne dépend que de K, n et alpha.
     *
     * @param alpha risque choisi (ex. 0.05 pour un seuil à 95 %)
     * @return T^2_seuil, à comparer au T^2_min calculé pour le visage test
     */
    public double calculerSeuilHotelling(double alpha) {
        int K = projection.getEigenfaces().getK();
        int n = baseRef.getNbImages();

        FDistribution loiFisher = new FDistribution(K, n - K);
        double quantile = loiFisher.inverseCumulativeProbability(1 - alpha);

        return ((double) (K * (n - 1)) / (n - K)) * quantile;
    }

    // Évaluation

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

    // Getters
    
    public double getDerniereDistance() {
        return this.derniereDistance;
    }

    public double getDistanceMax() {
        return this.distanceMax;
    }

    public int getIndexDistMin(){
        return this.indexDistMin;
    }
}

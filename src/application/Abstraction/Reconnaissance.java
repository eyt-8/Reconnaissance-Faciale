package application.Abstraction;
import java.io.IOException;
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

    /** Risque alpha utilisé pour le seuil du critère de Hotelling (95 %). */
    private static final double ALPHA_HOTELLING = 0.50;

    /** Valeur propre minimale acceptée dans le critère de Hotelling : en dessous de
     *  ce seuil, la composante est ignorée pour éviter une division par une valeur
     *  quasi nulle (qui ferait exploser T^2 sans information statistique fiable). */
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
        //this.calibrerSeuil();
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
        return (this.derniereDistance > seuilPour(methode)) ? "Inconnu" : baseRef.getIdentite(this.indexDistMin);
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
     * Donne le seuil calibré pour une méthode de distance donnée. Retombe sur
     * le seuil de la méthode "euclidienne" si la méthode demandée est inconnue
     * (même comportement par défaut que le dispatcher distance()).
     */
    private double seuilPour(String methode) {
        return this.seuils.getOrDefault(methode, this.seuils.get("euclidienne"));
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

    /**
     * Identifie un visage à partir d'un ImageVect déjà chargé, avec un alpha personnalisable.
     * La distance choisie trouve le plus proche voisin ;
     * le critère de Hotelling (au risque alpha) décide connu ou non.
     *
     * @param imageTest image vectorisée à identifier
     * @param methode   méthode de distance (euclidienne, cosinus, mahalanobis)
     * @param alpha     risque pour le seuil de Hotelling (plus alpha est grand, plus c'est strict)
     * @return nom de la personne reconnue, ou "Inconnu"
     */
    public String identifierAvecHotelling(ImageVect imageTest, String methode, double alpha) {
        this.trouverPlusProche(imageTest, methode);
        return this.identifierHotelling(imageTest, alpha, this.indexDistMin);
    }

    /**
     * Identifie le visage contenu dans un fichier image.
     * La distance choisie trouve le plus proche voisin et classe le top 5 ;
     * le critère de Hotelling décide ensuite si ce candidat est connu ou non.
     *
     * @param cheminFichier chemin absolu vers l'image à identifier
     * @param methode       méthode de distance (euclidienne, cosinus, mahalanobis)
     * @return nom de la personne reconnue, ou "Inconnu"
     * @throws IOException si le fichier image est illisible
     */
    public String identifierFichier(String cheminFichier, String methode) throws IOException {
        ImageVect imageTest = new ImageVect(cheminFichier);
        this.trouverPlusProche(imageTest, methode);
        return this.identifierHotelling(imageTest, ALPHA_HOTELLING, this.indexDistMin);
    }

    // Évaluation

    /**
     * Prédit, pour chaque image de la base d'apprentissage, si elle est connue ou inconnue
     * selon le critère de Hotelling, en mode leave-one-out (LOO) : l'image testée est
     * temporairement retirée de la base avant d'être évaluée.
     *
     * @param methode méthode de distance pour trouver le plus proche voisin
     * @param alpha   risque pour le seuil de Hotelling
     * @return liste de tableaux {nomVrai, nomPredit} pour chaque image de référence
     */
    public List<String[]> preditionsHotellingLOO(String methode, double alpha) {
        List<String[]> resultats = new ArrayList<>();
        int total = signaturesRef.size();
        for (int i = 0; i < total; i++) {
            SimpleMatrix sigI = signaturesRef.remove(i);
            String nomVrai = baseRef.getIdentite(i);
            String nomPredit = this.identifierAvecHotelling(baseRef.getReferences().get(i), methode, alpha);
            resultats.add(new String[]{nomVrai, nomPredit});
            signaturesRef.add(i, sigI);
        }
        return resultats;
    }

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
            SimpleMatrix sigI = signaturesRef.remove(i);
            String veriteTerrain = baseRef.getIdentite(i);

            double distMin = Double.MAX_VALUE;
            String identiteTrouvee = "Inconnu";

            for (int j = 0; j < signaturesRef.size(); j++) {
                double d = distance(sigI, signaturesRef.get(j), methode);
                if (d < distMin) {
                    distMin = d;
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

    // Getters
    
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

    public int getIndexDistMin(){
        return this.indexDistMin;
    }
}

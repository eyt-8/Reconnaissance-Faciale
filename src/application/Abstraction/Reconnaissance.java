package application.Abstraction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.distribution.FDistribution;
import org.ejml.simple.SimpleMatrix;

/**
 * Reconnaissance faciale par ACP/Eigenfaces.
 *
 * Calculer la distance (euclidienne, cosinus ou Mahalanobis) sert à trouver le plus proche voisin parmi les images de référence.
 * et le critère de Hotelling T^2 qui détermine si ce voisin est suffisamment proche pour être considéré comme "connu" ou si le visage est "Inconnu".
 *
 * @author SOULEZ-DAMAZIE Soraya, PAILLASSA Nylan, CAUMONT Virgile
 */
public class Reconnaissance {

    // Constantes

    /**
     * Risque alpha du critère de Hotelling.
     * Plus la valeur est élevée, plus le seuil T^2 est bas et plus le critère
     * est strict (le système retourne "Inconnu" plus facilement).
     */
    public static final double ALPHA_HOTELLING = 0.9;

    /** En dessous de cette valeur propre, la composante est ignorée (évite div/0). */
    private static final double LAMBDA_MIN = 1e-12;

    private final BaseDeDonnees baseRef;
    private final Projection projection;

    /** Projections pré-calculées des images de référence dans l'espace ACP. */
    private final List<SimpleMatrix> signaturesRef;

    /** Résultats triés par distance croissante après le dernier appel à identifier(). */
    private List<DistanceIdentite> derniersResultats = new ArrayList<>();

    // Classe interne

    /** Associe une identité à la distance calculée vis-à-vis du visage testé. */
    public static class DistanceIdentite implements Comparable<DistanceIdentite> {
        public final String identite;
        public final double distance;

        public DistanceIdentite(String identite, double distance) {
            this.identite = identite;
            this.distance = distance;
        }

        @Override
        public int compareTo(DistanceIdentite o) {
            return Double.compare(this.distance, o.distance);
        }
    }

    /**
     * Prend le pourcentage d'images retenu et renvoie l'image à partir de laquelle on ne les prend plus
     * @param pourcentage le pourcentage demandé
     * @return
     */
    public int critereDeux(double pourcentage){
        return (int)Math.floor(pourcentage*baseRef.getNbImages());
    }

    /**
     * Initialise la reconnaissance et précalcule les signatures des références.
     *
     * @param baseRef    base d'images d'apprentissage
     * @param projection moteur de projection dans l'espace des eigenfaces
     */
    public Reconnaissance(BaseDeDonnees baseRef, Projection projection) {
        this.baseRef       = baseRef;
        this.projection    = projection;
        this.signaturesRef = new ArrayList<>();
        for (ImageVect img : baseRef.getReferences()) {
            this.signaturesRef.add(projection.projeter(img));
        }
    }


    /**
     * Identifie un visage à partir de son chemin de fichier.
     * Point d'entrée utilisé par l'interface graphique (Gestionnaire).
     *
     * @param cheminFichier chemin absolu de l'image à identifier
     * @param methode "euclidienne", "cosinus" ou "mahalanobis"
     * @return nom de la personne reconnue, ou "Inconnu"
     */
    public String identifier(String cheminFichier, String methode) throws IOException {
        ImageVect test = new ImageVect(cheminFichier);
        test.vectoriser();
        return identifier(projection.projeter(test), methode, ALPHA_HOTELLING);
    }

    /**
     * Identifie un visage à partir de son ImageVect.
     * Utilisé pour les évaluations et les tests (App.java).
     *
     * @param test    image à identifier
     * @param methode "euclidienne", "cosinus" ou "mahalanobis"
     * @return nom de la personne reconnue, ou "Inconnu"
     */
    public String identifier(ImageVect test, String methode) {
        return identifier(projection.projeter(test), methode, ALPHA_HOTELLING);
    }

    /**
     * Méthode interne d'identification sur des coordonnées déjà projetées.
     *
     * calcule les distances à toutes les références, trouve le plus proche voisin,
     * remplit derniersResultats (triés par distance croissante) puis valide le ppv avec le critère de Hotelling T^2.
     *
     * @param coordsTest coordonnées du visage test dans l'espace ACP
     * @param methode méthode de distance
     * @param alpha risque pour le seuil de Hotelling
     * @return nom du plus proche voisin si T^2 <= seuil, "Inconnu" sinon
     */
    private String identifier(SimpleMatrix coordsTest, String methode, double alpha) {
        derniersResultats.clear();
        int    indexPPV = 0;
        double distMin = Double.MAX_VALUE;

        for (int i = 0; i < signaturesRef.size(); i++) {
            double d = distance(coordsTest, signaturesRef.get(i), methode);
            derniersResultats.add(new DistanceIdentite(baseRef.getIdentite(i), d));
            if (d < distMin) {
                distMin  = d;
                indexPPV  = i;
            }
        }
        Collections.sort(derniersResultats);

        return estConnu(coordsTest, indexPPV, alpha)
               ? baseRef.getIdentite(indexPPV)
               : "Inconnu";
    }

    /**
     * Retourne vrai si le T² du candidat est inférieur au seuil théorique de Hotelling.
     */
    private boolean estConnu(SimpleMatrix coordsTest, int indexCandidat, double alpha) {
        return calculerT2(coordsTest, indexCandidat) <= calculerSeuilHotelling(alpha);
    }

    /**
     * C'est la distance de Mahalanobis au carré dans l'espace ACP réduit.
     */
    private double calculerT2(SimpleMatrix coordsTest, int indexCandidat) {
        SimpleMatrix lambda = projection.getEigenfaces().getValPropresK();
        SimpleMatrix ecart  = coordsTest.minus(signaturesRef.get(indexCandidat));
        int    K = coordsTest.getNumRows();
        int    n = baseRef.getNbImages();
        double t2 = 0.0;
        for (int i = 0; i < K; i++) {
            // λᵢ est l'eigenvalue de A^T*A (somme des carrés) ;
            // la variance réelle de la projection est λᵢ/n
            double li = lambda.get(i, 0) / n;
            if (li < LAMBDA_MIN) continue;
            double ei = ecart.get(i, 0);
            t2 += (ei * ei) / li;
        }
        System.out.print("T^2 :");
        System.out.println(t2);
        return t2;
    }

    /**
     * Seuil théorique issu de la loi de Fisher, fonction de K, n et alpha.
     */
    public double calculerSeuilHotelling(double alpha) {
        int K = projection.getEigenfaces().getK();
        int n = baseRef.getNbImages();
        FDistribution fisher = new FDistribution(K, n - K);
        double quantile = fisher.inverseCumulativeProbability(1.0 - alpha);
        return ((double) K * (n - 1) / (n - K)) * quantile;
    }

    /**
     * Prend le pourcentage d'images retenu et renvoie l'image à partir de laquelle on ne les prend plus
     * @param pourcentage le pourcentage demandé
     * @return
     */
    public int critereDeux(double pourcentage){
        return (int)Math.floor(pourcentage*baseRef.getNbImages());
    }



    private double distance(SimpleMatrix jp, SimpleMatrix jpk, String methode) {
        return switch (methode) {
            case "cosinus" -> distanceCosinus(jp, jpk);
            case "mahalanobis" -> distanceMahalanobis(jp, jpk);
            default -> distanceEuclidienne(jp, jpk);
        };
    }


    private double distanceEuclidienne(SimpleMatrix jp, SimpleMatrix jpk) {
        return jp.minus(jpk).normF();
    }

    private double distanceCosinus(SimpleMatrix jp, SimpleMatrix jpk) {
        double n1 = jp.normF(), n2 = jpk.normF();
        if (n1 < 1e-12 || n2 < 1e-12) return 1.0;
        return 1.0 - jp.dot(jpk) / (n1 * n2);
    }

    private double distanceMahalanobis(SimpleMatrix jp, SimpleMatrix jpk) {
        SimpleMatrix lambdaK   = projection.getEigenfaces().getValPropresK();
        int          k         = lambdaK.getNumRows();
        SimpleMatrix lambdaInv = new SimpleMatrix(k, k);
        for (int i = 0; i < k; i++) {
            double lam = lambdaK.get(i, 0);
            lambdaInv.set(i, i, lam > 1e-12 ? 1.0 / lam : 0.0);
        }
        SimpleMatrix diff = jp.minus(jpk);
        return diff.transpose().mult(lambdaInv).dot(diff);
    }




    /**
     * Trouve le plus proche voisin d'une image test et retourne son T² de Hotelling.
     * Ne modifie pas la base de référence (contrairement au LOO).
     *
     * @return tableau [nomPPV, String.valueOf(t2)]
     */
    public String[] trouverPPVAvecT2(ImageVect test, String methode) {
        SimpleMatrix coordsTest = projection.projeter(test);
        int    indexPPV = 0;
        double distMin  = Double.MAX_VALUE;
        for (int i = 0; i < signaturesRef.size(); i++) {
            double d = distance(coordsTest, signaturesRef.get(i), methode);
            if (d < distMin) { distMin = d; indexPPV = i; }
        }
        double t2 = calculerT2(coordsTest, indexPPV);
        return new String[]{baseRef.getIdentite(indexPPV), String.valueOf(t2)};
    }

        /**
     * Prédictions plus petite distance avec critère de Hotelling, pour un alpha donné.
     * Permet de tester différentes valeurs d'alpha dans App.java.
     * Retourne une liste de paires [nom réel, nom prédit].
     *
     * @param methode méthode de distance pour trouver le plus proche voisin
     * @param alpha   risque pour le critère de Hotelling
     */
    public List<String[]> predictionsDistMin(String methode, double alpha) {
        List<String[]> resultats = new ArrayList<>();
        int n = signaturesRef.size();
        double crDeux = critereDeux(0.2);
        for (int i = 0; i < n; i++) {
            SimpleMatrix coordsTest = signaturesRef.get(i);
            SimpleMatrix sigI = signaturesRef.remove(i);

            int indexPPVReduit = 0;
            double distMin = Double.MAX_VALUE;

            if (crDeux>signaturesRef.size()){
                crDeux = signaturesRef.size();
            }

            for (int j = 0; j < crDeux; j++) {
                double d = distance(coordsTest, signaturesRef.get(j), methode);
                if (d < distMin) { distMin = d; indexPPVReduit = j; }
            }
            int indexPPVOriginal = (indexPPVReduit < i) ? indexPPVReduit : indexPPVReduit + 1;

            // Valider avec Hotelling (signaturesRef est réduite, indexPPVReduit est correct)
            double t2    = calculerT2(coordsTest, indexPPVReduit);
            double seuil = calculerSeuilHotelling(alpha);
            String nomPredit = (t2 <= seuil)
                               ? baseRef.getIdentite(indexPPVOriginal)
                               : "Inconnu";

            resultats.add(new String[]{baseRef.getIdentite(i), nomPredit});
            signaturesRef.add(i, sigI);
        }
        return resultats;
    }


    /** Résultats triés par distance du dernier appel à identifier(). */
    public List<DistanceIdentite> getResultatsPrecedents() {
        return derniersResultats;
    }
}

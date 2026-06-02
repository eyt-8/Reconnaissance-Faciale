import java.util.ArrayList;
import java.util.List;

import org.ejml.simple.SimpleMatrix;

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

    /** Limite de distance au-delà de laquelle le visage est considéré comme inconnu */
    private double seuil;

    /** Signatures pré-calculées des images de référence */
    private List<SimpleMatrix> signaturesRef;

    private double derniereDistance = 0.0;

    /**
     * Constructeur de la classe Reconnaissance.
     *
     * @param baseRef    l'instance de BaseDeDonnees contenant les signatures d'apprentissage
     * @param projection l'instance de Projection contenant la base d'Eigenfaces
     * @param seuil      la distance euclidienne maximale tolérée pour une identification
     */
    public Reconnaissance(BaseDeDonnees baseRef, Projection projection, double seuil) {
        this.baseRef    = baseRef;
        this.projection = projection;
        this.seuil      = seuil;
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

        for (int i = 0; i < signaturesRef.size(); i++) {
            double d = distance(coordonneesTest, signaturesRef.get(i), methode);
            if (d < distanceMinimale) {
                distanceMinimale = d;
                identiteTrouvee  = baseRef.getIdentite(i);
            }
        }
        this.derniereDistance = distanceMinimale;
        return (distanceMinimale > this.seuil) ? "Inconnu" : identiteTrouvee;
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
     * Calcule automatiquement le seuil comme 1,5 × la distance euclidienne
     * moyenne entre toutes les paires de signatures de référence.
     */
    public void calibrerSeuil() {
        if (signaturesRef.size() < 2) return;
        double somme = 0;
        int count = 0;
        for (int i = 0; i < signaturesRef.size(); i++) {
            for (int j = i + 1; j < signaturesRef.size(); j++) {
                somme += distance_euclidienne(signaturesRef.get(i), signaturesRef.get(j));
                count++;
            }
        }
        this.seuil = (somme / count) * 1.5;
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
            if (distMin <= seuil && identiteTrouvee.equals(veriteTerrain)) reussites++;
            signaturesRef.add(i, sigI);
        }
        return (double) reussites / total;
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

    public double getSeuil() {
        return seuil;
    }

    public double getDerniereDistance() {
        return this.derniereDistance;
    }
}

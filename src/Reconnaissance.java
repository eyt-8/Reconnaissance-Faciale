import java.util.ArrayList;
import java.util.Collections;

import org.ejml.simple.SimpleMatrix;

/**
 * La classe Reconnaissance gère la phase de test du système.
 * Elle permet d'identifier de nouvelles images (visages) en les projetant
 * dans l'espace des eigenfaces et en calculant la distance avec les signatures connues.
 * 
 * @author SOULEZ-DAMAZIE Soraya PAILLASSA Nylan CAUMONT Virgile
 * @version 1.0
 */
public class Reconnaissance {
    
    /** Base de données contenant les images de référence et leurs signatures */
    private BaseDeDonnees baseRef; 
    
    /** Objet gérant la projection mathématique dans l'espace réduit (ACP) */
    private Projection projection; 
    
    /** Limite de distance au-delà de laquelle le visage est considéré comme inconnu */
    private double seuil; 
    
    private double derniereDistance = 0.0;

    /**
     * Constructeur de la classe Reconnaissance.
     * 
     * @param baseRef    l'instance de BaseDeDonnees contenant les signatures d'apprentissage
     * @param projection l'instance de Projection contenant la base d'Eigenfaces
     * @param seuil      la distance euclidienne maximale tolérée pour une identification
     */
    public Reconnaissance(BaseDeDonnees baseRef, Projection projection, double seuil) {
        this.baseRef = baseRef;
        this.projection = projection;
        this.seuil = seuil;
    }

    /**
     * Tente d'identifier une image test en la comparant avec toutes les signatures de la base.
     * La méthode projette l'image, trouve la signature la plus proche et vérifie si elle respecte le seuil.
     * 
     * @param test l'image vectorisée (ImageVect) représentant le visage à identifier
     * @param methode la distance utilisée (euclidienne, cosinus)
     * @return le nom de la personne si elle est reconnue, ou "Inconnu" si la distance dépasse le seuil
     */
    public String identifier(ImageVect test, String methode) {
        SimpleMatrix coordonneesTest = projection.projeter(test);
        double distanceMinimale = Double.MAX_VALUE;
        String identiteTrouvee = "Inconnu";

        ArrayList <Double> distances = new ArrayList<Double>();
        
        for (int i = 0; i < baseRef.getReferences().size(); i++) {
            ImageVect refImg = baseRef.getReferences().get(i);
            SimpleMatrix coordonneesRef = projection.projeter(refImg);
            double d = distance(coordonneesTest, coordonneesRef, methode);
            distances.add(d);
            if (d < distanceMinimale) {
                distanceMinimale = d;
                identiteTrouvee = baseRef.getIdentite(i);
            }
        }
        System.out.println(" Distance minimale :"+distanceMinimale);
        this.derniereDistance = distanceMinimale;
        this.confiances(distances);
        return (distanceMinimale > this.seuil) ? "Inconnu" : identiteTrouvee;
    }
    
    /*
     * Retourne la confiance liée à chaque distance dans l'ordre
     * @param distances liste de toutes les distances (ordre non requis)
     */
    public void confiances(ArrayList<Double> distances) {
    	ArrayList<Double> confiances = new ArrayList<Double>();
    	Double somme=0.0;
    	Double maximum = Collections.max(distances);
    	ArrayList<Double> dist_inv = new ArrayList<Double>();
    	
    	// On centre
    	for (int i=0;i<distances.size();i++) {
    		dist_inv.add(maximum-distances.get(i));
    		somme = somme + maximum - distances.get(i);
    	}
    	
    	// On normalise à 100
    	for (int i=0;i<distances.size();i++) {
    		confiances.add((dist_inv.get(i)/somme)*100);
    	}
    	somme = 0.0;
    	for (int i=0;i<distances.size();i++) {
    		somme = somme + confiances.get(i);
    	}
    	// On trie les confiances
    	Collections.sort(confiances);
    	Collections.reverse(confiances);
    	System.out.println("Confiance (%)");
    	System.out.println(confiances.toString());
    }
    
    /**
     * Calcule la distance (euclidienne ou cosinus) entre 
     * les coordonnées projetées de deux visages. Plus la distance est proche de 0, 
     * plus les visages sont similaires.
     * 
     * @param jp  les coordonnées projetées du premier visage (test)
     * @param jpk les coordonnées projetées du deuxième visage (référence k)
     * @param methode le nom de la méthode (euclidienne, cosinus)
     * @return la distance calculée entre les deux vecteurs de coordonnées
     */
    public double distance(SimpleMatrix jp, SimpleMatrix jpk, String methode) {
    	double distance;
        switch (methode) {
        	case "euclidienne" ->
        		distance= this.distance_euclidenne(jp, jpk);
        	case "cosinus" ->
        		distance = 1-this.distance_cosinus(jp, jpk);
        	case "mahalanobis"->
        		distance = this.distance_mahalanobis(jp, jpk);
        	default -> // Par défaut on prend la distance euclidienne
        		distance= this.distance_euclidenne(jp, jpk);
        }
        return distance;
    }
    
    /**
     * Calcule la distance par cosinus (donc la corrélation) entre 
     * les coordonnées projetées de deux visages. Plus la distance est proche de 0, 
     * plus les visages sont similaires.
     * 
     * @param jp  les coordonnées projetées du premier visage (test)
     * @param jpk les coordonnées projetées du deuxième visage (référence k)
     * @return la distance calculée entre les deux vecteurs de coordonnées
     */
    public double distance_cosinus(SimpleMatrix jp, SimpleMatrix jpk) {
        return Math.abs((jp.dot(jpk))/(jp.normF()*jpk.normF()));
    }
    
    /**
     * Calcule la distance euclidienne (Norme de Frobenius pour EJML) entre 
     * les coordonnées projetées de deux visages. Plus la distance est proche de 0, 
     * plus les visages sont similaires.
     * 
     * @param jp  les coordonnées projetées du premier visage (test)
     * @param jpk les coordonnées projetées du deuxième visage (référence k)
     * @return la distance calculée entre les deux vecteurs de coordonnées
     */
    public double distance_euclidenne(SimpleMatrix jp, SimpleMatrix jpk) {
        return (jp.minus(jpk)).normF();
    }
    
    

    /**
     * Calcule le ratio de bonnes identifications par rapport au nombre total de tests.
     * 
     * @return le taux d'identification (entre 0.0 et 1.0)
     */
    public double tauxIdentification() {
        int totalTests = 0; 
        int reussites = 0;
        
        for (ImageVect imgTest : baseRef.getTests()) {
            totalTests++;
            String identiteTrouvee = identifier(imgTest,"euclidienne");
            
            // On déduit le nom attendu à partir du nom du fichier image
            // (Assurez-vous que cette ligne correspond à votre façon de nommer les fichiers tests)
            String nomAttendu = imgTest.getNom().replaceAll("[0-9]", ""); 
            
            if (identiteTrouvee.equals(nomAttendu)) {
                reussites++;
            } else {
                System.out.println("Erreur : " + imgTest.getNom() + " identifié(e) comme " + identiteTrouvee);
            }
        }
        
        if (totalTests == 0) return 0.0;
        return (double) reussites / totalTests;
    }
    
    /**
     * Distance de Mahalanobis dans l'espace réduit (K composantes sélectionnées).
     * La matrice de covariance est diagonale : d = (jp−jpk)ᵀ × Λ⁻¹ × (jp−jpk).
     */
    public double distance_mahalanobis(SimpleMatrix jp, SimpleMatrix jpk) {
        SimpleMatrix lambdaK = projection.getEigenfaces().getValPropres(); // K×1
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
     * Déclenche une série de tests sur la base de données de test et 
     * affiche le taux d'identification global dans la console.
     */
    public void testerScenarios() {
        System.out.println("Lancement des scénarios de test...");
        double taux = tauxIdentification();
        System.out.println("Taux d'identification global : " + (taux * 100) + " %");
    }

    public double getDerniereDistance() {
        return this.derniereDistance;
    }
}

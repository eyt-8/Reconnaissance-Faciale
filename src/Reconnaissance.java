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
        System.out.println("Distance minimale : "+distanceMinimale);
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
    	for (int i=0;i<distances.size();i++) {
    		// On passe les distances entre 0 et 1
    		distances.set(i, 1.0-distances.get(i)/Collections.max(distances));
    		somme = somme + 1.0 - distances.get(i)/Collections.max(distances);
    	}
    	
    	for (int i=0;i<distances.size();i++) {
    		confiances.add(distances.get(i)/somme*100);
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
     * Calcule la distance de Mahalanobis entre 
     * les coordonnées projetées de deux visages. Plus la distance est proche de 0, 
     * plus les visages sont similaires.
     * source : https://link-springer-com.bibdocs.u-cergy.fr/chapter/10.1007/978-3-642-30958-8_17
     * 
     * @param jp  les coordonnées projetées du premier visage (test)
     * @param jpk les coordonnées projetées du deuxième visage (référence k)
     * @return la distance calculée entre les deux vecteurs de coordonnées
     */
    public double distance_mahalanobis(SimpleMatrix jp, SimpleMatrix jpk) {
    	SimpleMatrix propre_vec = projection.getEigenfaces().getValPropres();
        int k = propre_vec.getNumRows(); // Nombre de composantes (K)
    	
    	// La matrice de covariance des coordonnées projetées est diagonale (les valeurs propres)
    	SimpleMatrix propreInv = new SimpleMatrix(k, k);
    	propreInv.zero();
    	for (int i=0; i<k; i++) {
    		propreInv.set(i, i, 1.0 / propre_vec.get(i, 0));
    	}
    	
    	// Différence entre les deux vecteurs de coordonnées (taille K x 1)
    	SimpleMatrix difference = jp.minus(jpk);
        
        // Formule : d = (jp - jpk)^T * D^-1 * (jp - jpk)
        return difference.transpose().mult(propreInv).dot(difference);
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

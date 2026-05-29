import org.ejml.simple.SimpleMatrix;

/**
 * La classe Reconnaissance gère la phase de test du système.
 * Elle permet d'identifier de nouvelles images (visages) en les projetant
 * dans l'espace des eigenfaces et en calculant la distance avec les signatures connues.
 * 
 * @author SOULEZ-DAMAZIE Soraya PAILLASSA Nylan
 * @version 1.0
 */
public class Reconnaissance {
    
    /** Base de données contenant les images de référence et leurs signatures */
    private BaseDeDonnees baseRef; 
    
    /** Objet gérant la projection mathématique dans l'espace réduit (ACP) */
    private Projection projection; 
    
    /** Limite de distance au-delà de laquelle le visage est considéré comme inconnu */
    private double seuil; 
    
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
     * @return le nom de la personne si elle est reconnue, ou "Inconnu" si la distance dépasse le seuil
     */
    public String identifier(ImageVect test) {
        SimpleMatrix coordonneesTest = projection.projeter(test);
        double distanceMinimale = Double.MAX_VALUE;
        String identiteTrouvee = "Inconnu";

        for (int i = 0; i < baseRef.getReferences().size(); i++) {
            SimpleMatrix coordonneesRef = projection.projeter(baseRef.getReferences().get(i));
            double d = distance(coordonneesTest, coordonneesRef);
            if (d < distanceMinimale) {
                distanceMinimale = d;
                identiteTrouvee = baseRef.getIdentite(i);
            }
        }
        
        return (distanceMinimale > seuil) ? "Inconnu" : identiteTrouvee;
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
    public double distance(SimpleMatrix jp, SimpleMatrix jpk) {
        return jp.minus(jpk).normF();
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
            String identiteTrouvee = identifier(imgTest);
            
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
}
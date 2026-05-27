/** @author : SOULEZ-DAMAZIE Soraya*/
import org.ejml.simple.SimpleMatrix;

public class Reconnaissance {
	private BaseDeDonnees baseRef; //Base d'images connues
	private Projection projection; //
	private double seuil; //La limite pour reconnaitre l'identité de la personne
	

    public Reconnaissance(BaseDeDonnees baseRef, Projection projection, double seuil) {
        this.baseRef = baseRef;
        this.projection = projection;
        this.seuil = seuil;
    }

    // 1 : identifier(test: Image) 
    public String identifier(ImageVect test) {
        SimpleMatrix coordonneesTest = projection.projeter(test);
        double distanceMinimale = Double.MAX_VALUE;
        String identiteTrouvee = "Inconnu";

        for (int i = 0; i < baseRef.getSignatures().size(); i++) {
            double d = distance(coordonneesTest, baseRef.getSignatures().get(i));
            if (d < distanceMinimale) {
                distanceMinimale = d;
                identiteTrouvee = baseRef.getNoms().get(i);
            }
        }
        return (distanceMinimale > seuil) ? "Inconnu" : identiteTrouvee;
    }

    
    /**
	 * Calcule la distance entre deux coordonnées de visages 
	 * C'est l'application de la norme Euclidienne ||Jp - Jpk||2
     * 
     * @param jp : Coordonnées projetées du visage test
     * @param jpk : Coordonées d'un visage de référence k
     * @return La distance (plus elle est proche de 0, plus c'est ressemblant)
	 */
    //2 : distance(jp, jpk) 
    public double distance(SimpleMatrix jp, SimpleMatrix jpk) {
        return jp.minus(jpk).normF();
    }

    // 3 : tauxIdentification()
    public double tauxIdentification() {
        // T = Nombre de visages correctement identifiés / Nombre total de visages test
        int totalTests = 0; 
        int reussites = 0;
        
        return (double) reussites / totalTests;
    }

    //4 : testerScenarios()
    public void testerScenarios() {
        System.out.println("Lancement des scénarios de test...");
        // Ici on boucle sur plusieurs images de test pour voir si le seuil est bon.
        // On affiche les erreurs, les faux positifs...
    }
}
	
	
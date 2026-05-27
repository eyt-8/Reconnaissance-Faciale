/** @author : SOULEZ-DAMAZIE Soraya*/
import org.ejml.simple.SimpleMatrix;

public class Reconnaissance {
	private BaseDeDonnees baseRef; //Base d'images connues
	private Projection projection; //
	private double seuil; //La limite pour reconnaitre l'identité de la personne
	
	/**
	 * Calcule la distance entre deux coordonnées de visages 
	 * C'est l'application de la norme Euclidienne ||Jp - Jpk||2
     * 
     * @param jp : Coordonnées projetées du visage test
     * @param jpk : Coordonées d'un visage de référence k
     * @return La distance (plus elle est proche de 0, plus c'est ressemblant)
	 */
	
	public double distance(SimpleMatrix jp, SimpleMatrix jpk) {
		//Calcule de la différence entre les deux coordonnées
		//.minus() correspond à une soustraction 
		SimpleMatrix difference = jp.minus(jpk);
		
		//Calcule de la norme euclidienne 
		// .normF() dans EJML calcule la norme de Frobenius, qui est l'équivalent 
        // de la norme Euclidienne pour les vecteurs
		double dist = difference.normF();
		
		return dist;
	}
	
}
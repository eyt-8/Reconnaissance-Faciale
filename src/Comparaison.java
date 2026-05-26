
import org.ejml.simple.SimpleMatrix;

/**
 * Classe Comparaison
 * @author virgile
 * @version 0.1
*/
public class Comparaison {
	private SimpleMatrix image;
	private SimpleMatrix projete;
	private int taille;
	
	/** 
	 * Constructeur de la classe
	 * @author virgile
	 * @param image matrice correspondant à l'image
	 * @param projete matrice prédite
	 * */
	public Comparaison(SimpleMatrix image, SimpleMatrix projete, int taille) {
		this.image = image;
		this.projete = projete;
		this.taille = taille;
	}
	
	/** 
	 * Erreur moyenne quadratique (EQM)
	 * @author virgile
	 * @return  Erreur moyenne quadratique (EQM)
	 * */
	public double calcul_eqm() {
		double somme = 0;
		// On réalise la somme de la différence des pixels au carré (formule de l'EQM)
		for (int i=0;i<taille;i++) {
			for (int j=0;j<taille;j++) {
				somme = somme + Math.pow(this.image.get(i,j)-this.projete.get(i,j),2);
			}
		}
		return somme;
	}
}

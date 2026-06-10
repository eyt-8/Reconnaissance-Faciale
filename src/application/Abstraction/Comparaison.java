package application.Abstraction;

import org.ejml.simple.SimpleMatrix;

/**
 * Classe Comparaison
 *
 * @author virgile
 * @version 1.1
 */
public class Comparaison {
	/**
	 * Matrice de l'image à l'origine
	 */
	private SimpleMatrix image;
	/**
	 * Matrice déduite de la projection sur l'espace des eigenfaces (la plus proche)
	 * Pour plus d'informations, se référer à {@link Eigenfaces}
	 */
	private SimpleMatrix projete;

	/**
	 * Constructeur (vecteurs colonnes n² * 1)
	 */
	public Comparaison(SimpleMatrix image, SimpleMatrix projete) {
		this.image = image;
		this.projete = projete;
	}

	/**
	 * Erreur Quadratique Moyenne (EQM) : moyenne des écarts au carré pixel par pixel.
	 *
	 * @return EQM >= 0
	 */
	public double calcul_eqm() {
		int n = image.getNumRows();
		double somme = 0;
		for (int i = 0; i < n; i++) {
			double diff = image.get(i, 0) - projete.get(i, 0);
			somme += diff * diff;
		}
		return somme / n;
	}

	/**
	 * Racine de l'erreur moyenne quadratique (REQM)
	 *
	 * @return REQM >= 0
	 */
	public double calcul_reqm() {
		return Math.sqrt(this.calcul_eqm());
	}

	/**
	 * Biais (moyenne des écarts absolus pixel par pixel)
	 *
	 * @return biais >= 0
	 */
	public double biais() {
		int n = image.getNumRows();
		double somme = 0;
		for (int i = 0; i < n; i++) {
			somme += Math.abs(image.get(i, 0) - projete.get(i, 0));
		}
		return somme / n;
	}
}

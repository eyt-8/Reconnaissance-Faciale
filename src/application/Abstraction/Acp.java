package application.Abstraction;
import org.ejml.simple.SimpleMatrix;

/**
 * Classe mettant en place l'ACP
 * @author virgile
 * Représente le visage moyen, un vecteur Sur ejml un vecteur est défini par
 * SimpleMatrix avec seulement une colonne
 */
public class Acp {

	// Attributs
	
	/** 
	 * Vecteur Image moyen 
	 */
	private SimpleMatrix visageMoyen;

	/**
	 * Matrice {@link BaseDeDonnees#matriceImages} centrée
	 */
	private SimpleMatrix matriceCentree;
	/**
	 * Voir {@link BaseDeDonnees}
	 */
	private BaseDeDonnees donnees;

	
	// Constructeurs
	
	/**
     * Constructeur standard à partir d'une base de données.
     * @param donnees La base contenant les images de référence.
     */
	public Acp(BaseDeDonnees donnees) {
		this.donnees = donnees;
		this.calcVisageMoyen(this.donnees.getMatriceImages());
		this.centrer(this.donnees.getMatriceImages());
	}

	
	/**
	 * Constructeur utilisant directement une matrice Utilisée principalement pour
	 * réaliser des tests
	 * 
	 * @param test matrice de test
	 */
	public Acp(SimpleMatrix test) {
		this.donnees = null;
		this.calcVisageMoyen(test);
		this.centrer(test);
	}
	

    //  Calculs

	/*
	 * Permet de calculer le visage moyen à partir de la base de données Modifie
	 * {@link Acp#visage_moyen}
	 * @param visages Utilise {@link BaseDeDonnees#matriceImage} ou une matrice de
	 * test
	 */
	public void calcVisageMoyen(SimpleMatrix visages) {
		// Le nombre d'images correspond au nombre de colonnes 
		int taille = visages.getNumCols();
		// Le nombre de colonnes correspond au nombre de pixels
		int taille_pixels = visages.getNumRows();

		this.visageMoyen = new SimpleMatrix(taille_pixels, 1);

		// On calcule la moyenne sur chaque ligne et on l'ajoute au vecteur du visage
		// moyen

		double somme;

		for (int i = 0; i < taille_pixels; i++) {
			somme = 0;
			for (int j = 0; j < taille; j++) {
				somme = somme + visages.get(i, j);
			}
			this.visageMoyen.set(i, 0, somme / taille);
		}
	}

	/**
	 * Centre la matrice {@link BaseDeDonnees#matriceImages} ou une matrice de test
	 * Modifie {@link Acp#matrice_centree}
	 * 
	 * @param visages {@link BaseDeDonnees#matriceImage} ou une matrice de test
	 */
	public void centrer(SimpleMatrix visages) {
		// Le nombre d'images correspond au nombre de lignes
		int taille = visages.getNumCols();
		// Le nombre de colonnes correspond au nombre de colonnes
		int taille_pixels = visages.getNumRows();

		this.matriceCentree = new SimpleMatrix(taille_pixels, taille);

		double elt = 0;
		for (int i = 0; i < taille_pixels; i++) {
			elt = 0;
			for (int j = 0; j < taille; j++) {
				// On récupère l'élément de coordonnées (i,j) de la matrice
				elt = visages.get(i, j);

				// On le soustrait à la moyenne de la ligne
				elt = elt - this.visageMoyen.get(i, 0);

				// On l'ajoute à la matrice centrée
				this.matriceCentree.set(i, j, elt);
			}
		}
	}


	// Getters

	public SimpleMatrix getVisageMoyen() {
		return visageMoyen;
	}

	public SimpleMatrix getMatriceCentree() {
		return matriceCentree;
	}

	public BaseDeDonnees getDonnees() {
		return donnees;
	}

	// Setters
	
	public void setVisageMoyen(SimpleMatrix visageMoyen) {
		this.visageMoyen = visageMoyen;
	}
	
	public void setMatriceCentree(SimpleMatrix matriceCentree) {
		this.matriceCentree = matriceCentree;
	}
	
	public void setDonnees(BaseDeDonnees donnees) {
		this.donnees = donnees;
	}
	
}

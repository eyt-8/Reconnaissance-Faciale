

import java.util.Vector;

import org.ejml.simple.SimpleMatrix;

/**
 * Classe mettant en place l'ACP
 * @author virgile
 * @version 0.1
 * */
public class Acp {
	/** 
	 * Représente le visage moyen, un vecteur
	 * Sur ejml un vecteur est défini par SimpleMatrix avec seulement une colonne
	 * */
	private SimpleMatrix visage_moyen;
	/** 
	 * Voir {@link BaseDeDonnees}
	 * */
	private BaseDeDonnees donnees;
	
	/** 
	 * Permet de calculer le visage moyen
	 * @author virgile
	 * Modifie {@link Acp#visage_moyen} 
	 * Utilise {@link BaseDeDonnees#matriceImage}
	 * */
	public void calcul_visage_moyen() {
		// Le nombre d'images correspond au nombre de lignes
		int taille = donnees.matriceImage.getNumCols();
		// Le nombre de colonnes correspond au nombre de colonnes
		int taille_pixels = donnees.matriceImage.getNumRows();
		
		// On calcule la moyenne sur chaque ligne et on l'ajoute au vecteur du visage moyen
		
		for (int i=0;i<taille;i++) {
			double somme = 0;
			for (int j=0;j<taille_pixels;j++) {
				// j,i car on fixe la ligne et on se déplace sur la colonne
				somme = somme + BaseDeDonnees.matriceImages.get(j,i);
			}
			visage_moyen.set(i,0,somme/taille);
		}
	}
}

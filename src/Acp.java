

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
	 * Matrice {@link BaseDeDonnees#matriceImages} centrée
	 * */
	private SimpleMatrix matrice_centree;
	/** 
	 * Voir {@link BaseDeDonnees}
	 * */
	private BaseDeDonnees donnees;
	
	public Acp(BaseDeDonnees donnees) {
		this.donnees=donnees;
		this.calcVisageMoyen();
		this.centrer();
	}
	
	/** 
	 * Permet de calculer le visage moyen à partir de la base de données
	 * Modifie {@link Acp#visage_moyen} 
	 * Utilise {@link BaseDeDonnees#matriceImage}
	 * */
	public void calcVisageMoyen() {
		// Le nombre d'images correspond au nombre de lignes
		int taille = this.donnees.matriceImages.getNumCols();
		// Le nombre de colonnes correspond au nombre de colonnes
		int taille_pixels = this.donnees.matriceImages.getNumRows();
		
		// On calcule la moyenne sur chaque ligne et on l'ajoute au vecteur du visage moyen
		
		for (int i=0;i<taille;i++) {
			double somme = 0;
			for (int j=0;j<taille_pixels;j++) {
				// j,i car on fixe la ligne et on se déplace sur la colonne
				somme = somme + this.donnees.matriceImages.get(j,i);
			}
			this.visage_moyen.set(i,0,somme/taille);
		}
	}
	
	/** 
	 * Centre la matrice {@link BaseDeDonnees#matriceImages}
	 * Modifie {@link Acp#matrice_centree}
	 * */
	public void centrer() {
		// Le nombre d'images correspond au nombre de lignes
		int taille = this.donnees.matriceImages.getNumCols();
		// Le nombre de colonnes correspond au nombre de colonnes
		int taille_pixels = this.donnees.matriceImages.getNumRows();
		double elt = 0;
		for (int i=0;i<taille;i++) {
			elt=0;
			for (int j=0;j<taille_pixels;j++) {
				// On récupère l'élément de coordonnées (i,j) de la matrice
				elt = this.donnees.matriceImages.get(i,j);
				
				// On le soustrait à la moyenne de la ligne
				elt = elt-this.visage_moyen.get(i,0);
				
				// On l'ajoute à la matrice centrée
				this.matrice_centree.set(i, j,elt);
			}
		}
	}
	
	public static void main(String[] args) {
		System.out.println("Classe ACP (TEST)");
		SimpleMatrix a = new SimpleMatrix(3,2);
		
		// Création d'une matrice de test
		/* ( 3 2 ) 
		 * ( 5 3 )
		 * ( 7 5 )
		 * */
		double[][] arr = {{3,2},{5,3},{7,5}};
		
		for (int i=0;i<3;i++) {
			
			// Matrice 1
			
			System.out.print("(");
			for (int j=0;j<2;j++) {
				System.out.print(" "+arr[i][j]+"");
				// On copie la matrice 1 en parallèle
				a.set(i, j, arr[i][j]);
			}
			System.out.println(" )");
		}
		
		// La suite requiert BaseDeDonnees
	}
}

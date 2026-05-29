

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
		this.calcVisageMoyen(this.donnees.getMatriceImages());
		this.centrer(this.donnees.getMatriceImages());
	}
	
	/** 
	 * Constructeur utilisant directement une matrice
	 * Utilisée principalement pour réaliser des tests*/
	public Acp(SimpleMatrix test) {
		this.donnees=null;
		this.calcVisageMoyen(test);
		this.centrer(test);
	}
	
	/** 
	 * Getters / Setters */
	public SimpleMatrix getVisage_moyen() {
		return visage_moyen;
	}

	public void setVisage_moyen(SimpleMatrix visage_moyen) {
		this.visage_moyen = visage_moyen;
	}

	public SimpleMatrix getMatrice_centree() {
		return matrice_centree;
	}

	public void setMatrice_centree(SimpleMatrix matrice_centree) {
		this.matrice_centree = matrice_centree;
	}

	public BaseDeDonnees getDonnees() {
		return donnees;
	}

	public void setDonnees(BaseDeDonnees donnees) {
		this.donnees = donnees;
	}
	
	/** 
	 * Permet de calculer le visage moyen à partir de la base de données
	 * Modifie {@link Acp#visage_moyen}
	 * Utilise {@link BaseDeDonnees#matriceImage} ou une matrice de test
	 * */
	public void calcVisageMoyen(SimpleMatrix visages) {
		// Le nombre d'images correspond au nombre de lignes
		int taille = visages.getNumCols();
		// Le nombre de colonnes correspond au nombre de colonnes
		int taille_pixels = visages.getNumRows();
		
		this.visage_moyen = new SimpleMatrix(taille_pixels,1);
		
		// On calcule la moyenne sur chaque ligne et on l'ajoute au vecteur du visage moyen
		
		double somme;
		
		for (int i=0;i<taille_pixels;i++) {
			somme = 0;
			for (int j=0;j<taille;j++) {
				somme = somme + visages.get(i,j);
			}
			this.visage_moyen.set(i,0,somme/taille);
		}
	}
	
	/** 
	 * Centre la matrice {@link BaseDeDonnees#matriceImages} ou une matrice de test
	 * Modifie {@link Acp#matrice_centree}
	 * */
	public void centrer(SimpleMatrix visages) {
		// Le nombre d'images correspond au nombre de lignes
		int taille = visages.getNumCols();
		// Le nombre de colonnes correspond au nombre de colonnes
		int taille_pixels = visages.getNumRows();
		
		this.matrice_centree = new SimpleMatrix(taille_pixels,taille);
		
		double elt = 0;
		for (int i=0;i<taille_pixels;i++) {
			elt=0;
			for (int j=0;j<taille;j++) {
				// On récupère l'élément de coordonnées (i,j) de la matrice
				elt = visages.get(i,j);
				
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
		
		System.out.println(a.toString());
		
		Acp acp = new Acp(a);
		// Affichage du visage moyen
		System.out.println(acp.getVisage_moyen().toString());
		// Affichage de la matrice centrée
		System.out.println(acp.getMatrice_centree());
	}
}

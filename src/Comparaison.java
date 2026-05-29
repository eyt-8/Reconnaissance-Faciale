
import org.ejml.simple.SimpleMatrix;

/**
 * Classe Comparaison
 * @author virgile
 * @version 0.1
*/
public class Comparaison {
	/**
	 * Matrice de l'image à l'origine 
	 * */
	private SimpleMatrix image;
	/** 
	 * Matrice déduite de la projection sur l'espace des eigenfaces (la plus proche)
	 * Pour plus d'informations, se référer à {@link Eigenfaces}
	 * */
	private SimpleMatrix projete;
	/** 
	 * Taille de la matrice (chaque matrice image est considérée comme carrée)
	 * */
	private int taille;
	
	/** 
	 * Constructeur de la classe
	 * @param image vectorielle correspondant à l'image
	 * @param projete matrice prédite
	 * Modifie {@link Comparaison#image}, {@link Comparaison#projete} et {@link Comparaison#taille}
	 * */
	public Comparaison(SimpleMatrix image, SimpleMatrix projete, int taille) {
		this.image = image;
		this.projete = projete;
		this.taille = taille;
	}
	
	/** 
	 * Erreur moyenne quadratique (EQM)
	 * @return  Erreur moyenne quadratique (EQM)
	 * Utilise {@link Comparaison#image} et {@link Comparaison#projete}
	 * */
	public double calcul_eqm() {
		double somme = 0;
		// On réalise la somme de la différence des pixels au carré (formule de l'EQM)
		for (int i=0;i<taille;i++) {
			for (int j=0;j<taille;j++) {
				somme = somme + Math.pow(this.image.get(i,j)-this.projete.get(i,j),2);
			}
		}
		return somme/taille;
	}
	
	/** 
	 * Racine de l'erreur moyenne quadratique (REQM)
	 * @return  Racine de l'erreur moyenne quadratique (REQM)
	 * Utilise {@link Comparaison#calcul_reqm()}
	 * */
	public double calcul_reqm() {
		return Math.sqrt(this.calcul_eqm());
	}
	
	/** 
	 * Biais (moyenne des écarts)
	 * @return biais
	 * Utilise {@link Comparaison#image} et {@link Comparaison#projete}
	 * */
	public double biais() {
		double somme = 0;
		// On réalise la somme de la différence des pixels au carré (formule de l'EQM)
		for (int i=0;i<taille;i++) {
			for (int j=0;j<taille;j++) {
				somme = somme + Math.abs(this.image.get(i,j)-this.projete.get(i,j));
			}
		}
		return somme/taille;
	}
	
	public static void main(String args[]) {
		System.out.println("Classe comparaison (TEST)");
		SimpleMatrix a = new SimpleMatrix(3,2);
		SimpleMatrix a2 = new SimpleMatrix(3,2);
		
		// Création d'une matrice de test
		/* ( 3 2 ) 
		 * ( 5 3 )
		 * */
		double[][] arr = {{3,2},{5,3}};
		// Création d'une deuxième matrice de test
		/* ( 1 5 )
		 * ( 7 6 )
		 * */
		double[][] arr2 = {{1,5},{7,6}};
		
		// Description des matrices
		System.out.println("Matrice gauche : matrice d'origine");
		System.out.println("Matrice droite : matrice des eigenfaces la plus proche");
		// Affichage des matrices en parallèle
		for (int i=0;i<2;i++) {
			
			// Matrice 1
			
			System.out.print("(");
			for (int j=0;j<2;j++) {
				System.out.print(" "+arr[i][j]+"");
				// On copie la matrice 1 en parallèle
				a.set(i, j, arr[i][j]);
			}
			
			System.out.print(" )");
			
			System.out.print(" (");
			// Matrice 2
			for (int j=0;j<2;j++) {
				System.out.print(" "+arr2[i][j]+"");
				// On copie la matrice en parallèle
				a2.set(i, j, arr2[i][j]);
			}
			
			System.out.println(" )");
			
		}
		
		//On crée la Comparaison et on teste la classe
		
		Comparaison c = new Comparaison(a,a2,2);
		
		// EQM
		System.out.println("EQM : "+c.calcul_eqm());
		// REQM
		System.out.println("REQM : "+c.calcul_reqm());
		// Biais
		System.out.println("Biais : "+c.biais());
	}
}

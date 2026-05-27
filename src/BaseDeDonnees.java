import org.ejml.simple.SimpleMatrix;
import javafx.scene.image.Image;
import java.io.file;

public class BaseDeDonnees {
	
	private String chemin; // Chemin des images d'apprentissage
	private Map<String,Image> images; // Map -> clé = nom, valeur = photo
	private SimpleMatrix matricesImages; // Matrice total

	public BaseDeDonnees(){
		this.chemin = "donnees/apprentissage/";
        this.images
	}

	public int getApprentissage(){
		File[] f = monRepertoire.listFiles();
		int x = 0;
		for (int i = 0 ; i < f.length ; i++) {
		  if (f[i].isFile()) {
			x++;
		  }
		}

		return x;
	}

	public static void main(String[] args){

	BaseDeDonnees bdd = new BaseDeDonnees();

		System.out.println(bdd.getApprentissage());
	}
	
}

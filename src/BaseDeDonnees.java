/**	Importation des classes nécessaires */
import org.ejml.simple.SimpleMatrix;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * La classe BaseDeDonnees gère le chargement et l'accès aux images d'apprentissage
 * depuis une hiérarchie de répertoires organisés par nom de personne.
 * Elle construit une matrice des images vectorisées pour l'analyse.
 *
 * @author Maël Lescoulié
 * @version 1.0
 */
public class BaseDeDonnees {
	/** Chemin racine vers le dossier contenant les images d'apprentissage */
	private String cheminRacine; 
	/** Liste des noms de personnes (ordre de chargement) */
	private List<String> listeNoms;
	/** Matrice contenant tous les vecteurs d'images en colonnes */
	private SimpleMatrix matriceImages;
	/** Liste des images de référence */
	private List<ImageVect> references;

	/**
	 * Constructeur initialisant la base de données avec les images du répertoire d'apprentissage
	 */
	public BaseDeDonnees(){
		this.cheminRacine = "donnees/apprentissage/";
        this.references = new ArrayList<>();
        this.listeNoms = new ArrayList<>();
        this.chargerChemin();
	}

	/**
	 * @return le nombre total d'images dans la base de données
	 */
	public int getNbImages() {
        return this.listeNoms.size();
    }

/**
	 * @return la matrice d'images vectorisées (colonnes = vecteurs d'images)
	 */
	public SimpleMatrix getMatriceImages() {
        return this.matriceImages;
    }

/**
	 * @param j indice de l'image
	 * @return le nom de la personne correspondant à cet indice
	 */
	public String getIdentite(int j) {
        return this.listeNoms.get(j);
    }

	/**
	 * Charge les images depuis la hiérarchie de répertoires et construit la matrice d'images
	 */
	private void chargerChemin() {
		File repertoirePrincipal = new File(this.cheminRacine);
		File[] sousRepertoires = repertoirePrincipal.listFiles();
		if (sousRepertoires == null) {
			System.err.println("Le répertoire est vide ou n'existe pas : " + this.cheminRacine);
			return;
		}

		for (File dossierPersonne : sousRepertoires) {
			if (dossierPersonne.isDirectory()) {
				String nomPersonne = dossierPersonne.getName();
				File[] fichiersImages = dossierPersonne.listFiles();
				if (fichiersImages != null) {
					for (File fichierImage : fichiersImages) {
						if (fichierImage.isFile() && fichierImage.getName().endsWith(".jpg")) {
							try {
								ImageVect img = new ImageVect(fichierImage.getAbsolutePath());
								img.vectoriser();
								this.references.add(img);
								this.listeNoms.add(nomPersonne);
							} catch (IOException e) {
								System.err.println("Erreur lors du chargement de " + fichierImage.getName() + " : " + e.getMessage());
							}
						}
					}
				}
			}
		}
		int totalImages = this.references.size();
        if (totalImages > 0) {
            int nbPixels = this.references.get(0).getVecteurCol().getNumRows();
            this.matriceImages = new SimpleMatrix(nbPixels, totalImages);            
            for (int j = 0; j < totalImages; j++) {
                this.matriceImages.insertIntoThis(0, j, this.references.get(j).getVecteurCol());
            }
        }
	}
	
	/**
	 * @return la liste de toutes les images d'apprentissage
	 */
	public List<ImageVect> getReferences() {
		return this.references;
	}

	/**
	 * Charge et retourne les images du dossier de test
	 * @return la liste des images de test
	 */
	public List<ImageVect> getTests() {
		List<ImageVect> listeTests = new ArrayList<>();
		File dossierTest = new File("donnees/test/"); 
		if (!dossierTest.exists() || !dossierTest.isDirectory()) {
			System.err.println("Le dossier de test n'existe pas ou est introuvable.");
			return listeTests;
		}
		File[] fichiers = dossierTest.listFiles();
		if (fichiers != null) {
			for (File f : fichiers) {
				if (f.isFile() && f.getName().endsWith(".jpg")) {
					try {
						listeTests.add(new ImageVect(f.getAbsolutePath()));
					} catch (IOException e) {
						System.err.println("Erreur lors du chargement de l'image de test" + f.getAbsolutePath() + " : " + e.getMessage());
					}
				}
			}
		}		
		return listeTests;
	}

	/**
	 * Créer la matrice final
	 * @param images listes des vecteurs images
	 * @return la matrice avec en colonnes les vecteurs colonnes
	 */
	public SimpleMatrix matriceTot(List<ImageVect> images){

            int nbColonnes = images.size();
            int nbLignes = images.get(0).getVecteurCol().getNumRows(); // Récupère le nombre de pixel d'un vecteur (le même pour tous)

            SimpleMatrix matrice = new SimpleMatrix(nbLignes, nbColonnes);

            for (int i=0;i<nbColonnes;i++){
                SimpleMatrix vecteur = images.get(i).getVecteurCol();
                matrice.insertIntoThis(0, i, vecteur);
			}
            return matrice;

        }
}
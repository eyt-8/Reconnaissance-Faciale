package application.Abstraction;
/**	Importation des classes nécessaires */
import org.ejml.simple.SimpleMatrix;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
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
	 * Constructeur avec chemin personnalisé vers le dossier d'apprentissage
	 * @param cheminRacine chemin du dossier (ex : "donnees/base/")
	 */
	public BaseDeDonnees(String cheminRacine){
		this.cheminRacine = cheminRacine;
        this.references = new ArrayList<>();
        this.listeNoms = new ArrayList<>();
        this.chargerChemin();
	}

	/**
	 * Constructeur par défaut utilisant le dossier "donnees/apprentissage/"
	 */
	public BaseDeDonnees(){
		this("donnees/apprentissage/");
	}

	/**
	 * @return le nombre total d'images dans la base de données
	 */
	public int getNbImages() {
        return this.listeNoms.size();
    }

	/** @return le chemin racine de la base de données */
	public String getCheminRacine() { return this.cheminRacine; }

	/** @return largeur (en pixels) des images de la base, lue depuis la première référence */
	public int getLargeurImage() { return this.references.get(0).getLargeur(); }

	/** @return hauteur (en pixels) des images de la base, lue depuis la première référence */
	public int getLongueurImage() { return this.references.get(0).getLongueur(); }

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
						if (fichierImage.isFile() && (fichierImage.getName().endsWith(".jpg") || fichierImage.getName().endsWith(".png") || fichierImage.getName().endsWith(".pgm"))) {
							try {
								ImageVect img = new ImageVect(fichierImage.getAbsolutePath());
								if (!this.references.isEmpty()) {
									int refW = this.references.get(0).getLargeur();
									int refH = this.references.get(0).getLongueur();
									if (img.getLargeur() != refW || img.getLongueur() != refH) {
										img = new ImageVect(this.redimensionner(img.getBufferedImage(), refW, refH), img.getNom());
									}
								}
								img.vectoriser();
								this.references.add(img);
								this.listeNoms.add(nomPersonne + "/" + fichierImage.getName());
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
			int refW = this.references.get(0).getLargeur();
			int refH = this.references.get(0).getLongueur();
			for (File f : fichiers) {
				if (f.isFile() && (f.getName().endsWith(".jpg") || f.getName().endsWith(".png") || f.getName().endsWith(".pgm"))) {
					try {
						ImageVect img = new ImageVect(f.getAbsolutePath());
						if (img.getLargeur() != refW || img.getLongueur() != refH) {
							img = new ImageVect(this.redimensionner(img.getBufferedImage(), refW, refH), img.getNom());
						}
						img.vectoriser();
						listeTests.add(img);
					} catch (IOException e) {
						System.err.println("Erreur lors du chargement de l'image de test " + f.getAbsolutePath() + " : " + e.getMessage());
					}
				}
			}
		}		
		return listeTests;
	}

	/**
	 * Redimensionne un BufferedImage aux dimensions cibles avec interpolation bilinéaire
	 */
	private BufferedImage redimensionner(BufferedImage src, int largeur, int longueur) {
		BufferedImage dest = new BufferedImage(largeur, longueur, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = dest.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(src, 0, 0, largeur, longueur, null);
		g.dispose();
		return dest;
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
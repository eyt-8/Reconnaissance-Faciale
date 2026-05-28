import org.ejml.simple.SimpleMatrix;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseDeDonnees {
	
	private String cheminRacine; 
	private Map<String, List<ImageVect>> images;
	private List<String> listeNoms;
	private SimpleMatrix matriceImages;

	public BaseDeDonnees(){
		this.cheminRacine = "donnees/apprentissage/";
        this.images = new HashMap<>();
        this.listeNoms = new ArrayList<>();
        this.chargerChemin();
	}

	public int getNbImages() {
        return this.listeNoms.size();
    }

    public SimpleMatrix getMatriceImages() {
        return this.matriceImages;
    }

    public String getIdentite(int j) {
        return this.listeNoms.get(j);
    }

	private void chargerChemin() {
		File repertoirePrincipal = new File(this.cheminRacine);
		File[] sousRepertoires = repertoirePrincipal.listFiles();
		if (sousRepertoires == null) {
			System.err.println("Le répertoire est vide ou n'existe pas : " + this.cheminRacine);
			return;
		}

		List<ImageVect> toutesImages = new ArrayList<>();
		for (File dossierPersonne : sousRepertoires) {
			if (dossierPersonne.isDirectory()) {
				String nomPersonne = dossierPersonne.getName();
				List<ImageVect> imagesPersonne = new ArrayList<>();

				File[] fichiersImages = dossierPersonne.listFiles();
				if (fichiersImages != null) {
					for (File fichierImage : fichiersImages) {
						if (fichierImage.isFile() && fichierImage.getName().endsWith(".jpg")) {
							ImageVect img = new ImageVect(fichierImage.getAbsolutePath());
							imagesPersonne.add(img);
							toutesImages.add(img);
							this.listeNoms.add(nomPersonne);
						}
					}
				}
				this.images.put(nomPersonne, imagesPersonne);
			}
		}
		int totalImages = toutesImages.size();
        if (totalImages > 0) {
            int nbPixels = toutesImages.get(0).getVecteur().numRows();
            this.matriceImages = new SimpleMatrix(nbPixels, totalImages);            
            for (int j = 0; j < totalImages; j++) {
                this.matriceImages.insertIntoThis(0, j, toutesImages.get(j).getVecteur());
            }
        }
	}
	
	public List<ImageVect> getReferences() {
		List<ImageVect> references = new ArrayList<>();
		for (List<ImageVect> listeImagesPersonne : this.images.values()) {
			references.addAll(listeImagesPersonne);
		}		
		return references;
	}

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
					listeTests.add(new ImageVect(f.getAbsolutePath()));
				}
			}
		}		
		return listeTests;
	}

	public Map<String, Integer> associerIdNom() {
		Map<String, Integer> association = new HashMap<>();
		int id = 0;
		for (String nomPersonne : this.images.keySet()) {
			association.put(nomPersonne, id);
			id++;
		}		
		return association;
	}
}
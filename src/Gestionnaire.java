/** Importation des classes nécessaires */
import java.io.File;

import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Gestionnaire central de l'interface utilisateur.
 * Il relie l'écran JavaFX aux actions de sélection de fichier et de traitement.
 *
 * @author Maël Lescoulié
 * @version 1.0
 */
public class Gestionnaire {

    /** L'interface graphique affichée dans la fenêtre principale */
    private Ecran ecran;
    /** Référence à la fenêtre principale de l'application */
    private Stage fenetrePrincipale;
    /** Base de données utilisée pour l'apprentissage et la recherche de visages */
    private BaseDeDonnees bdd;
    /** Moteur de reconnaissance qui identifie un visage à partir d'une image */
    private Reconnaissance reco;
    /** Projection des images sur les composantes principales (Eigenfaces) */
    private Projection proj;

    /**
     * Initialise le gestionnaire avec la fenêtre principale.
     * @param stage fenêtre JavaFX principale
     */
    public Gestionnaire(Stage stage) {
        this.fenetrePrincipale = stage;
        this.ecran = new Ecran();
        
        this.initialiserReco();

        this.enregistrerEcouteurs();
    }

    /**
     * Initialise les composants de reconnaissance faciale :
     * charge la base de données, calcule les composantes principales,
     * sélectionne le nombre de composantes utiles, crée la projection
     * et calibre le seuil de reconnaissance.
     */
    private void initialiserReco() {
        System.out.println("Démarrage de l'apprentissage...");
        try {
            this.bdd = new BaseDeDonnees();
            Acp acp = new Acp(this.bdd);
            SVD svd = new SVD(acp.getMatrice_centree());
            Eigenfaces faces = new Eigenfaces(svd, acp.getVisage_moyen());
            faces.construire();
            faces.selectionnerK(0.95);

            this.proj = new Projection(faces);
            this.reco = new Reconnaissance(this.bdd, this.proj, Double.MAX_VALUE);
            this.reco.calibrerSeuil();

            System.out.println("Apprentissage terminé — K = " + faces.getK());
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation de la reconnaissance : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Enregistre les actions utilisateur sur l'interface.
     */
    private void enregistrerEcouteurs() {
        this.ecran.getChoisirFichier().setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Sélectionner un visage à analyser");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.png", "*.pgm")
            );
            File fichierSelectionne = fileChooser.showOpenDialog(fenetrePrincipale);
            
            if (fichierSelectionne != null) {
                this.traiterReconnaissance(fichierSelectionne);
            }
        });
    }

    /**
     * Traite l'image sélectionnée et met à jour l'interface.
     * @param fichierImage image choisie par l'utilisateur
     */
    private void traiterReconnaissance(File fichierImage) {
        try {
            Image imgEntree = new Image(fichierImage.toURI().toString());
            ImageVect imageTest = new ImageVect(fichierImage.getAbsolutePath());
            String nomTrouve = this.reco.identifier(imageTest, "euclidienne");
            Image imgTrouvee = null;
            double tauxRessemblance = 0.0;
            if (!nomTrouve.equals("Inconnu")) {
                File dossierPersonne = new File("donnees/apprentissage/" + nomTrouve);                
                if (dossierPersonne.exists() && dossierPersonne.isDirectory()) {
                    File[] fichiersImages = dossierPersonne.listFiles((dir, name) -> name.endsWith(".jpg"));                    
                    if (fichiersImages != null && fichiersImages.length > 0) {
                        imgTrouvee = new Image(fichiersImages[0].toURI().toString());
                    }
                }
                double vraiDistance = this.reco.getDerniereDistance();
                double seuilMax = this.reco.getSeuil();
                double calculTaux = 100.0 * (1.0 - (vraiDistance / seuilMax));
                tauxRessemblance = Math.max(0.0, Math.min(100.0, calculTaux));
            } 
            else {
                tauxRessemblance = 0.0;
            }
            this.ecran.majInterface(imgEntree, imgTrouvee, nomTrouve, tauxRessemblance);
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la reconnaissance de l'image : " + e.getMessage());
            this.ecran.majInterface(null, null, "Erreur de lecture", 0.0);
        }
    }

    /**
     * @return l'écran principal géré par ce gestionnaire
     */
    public Ecran getEcran() {
        return this.ecran;
    }
}
package application.Controle;
/** Importation des classes nécessaires */
import java.io.File;

import org.ejml.simple.SimpleMatrix;

import application.Abstraction.Acp;
import application.Abstraction.BaseDeDonnees;
import application.Abstraction.Eigenfaces;
import application.Abstraction.ImageVect;
import application.Abstraction.Projection;
import application.Abstraction.Reconnaissance;
import application.Abstraction.SVD;
import application.Presentation.Ecran;
import application.Presentation.MenuLateral;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Gestionnaire central de l'application JavaFX.
 * Il relie l'écran aux actions utilisateur et gère la reconnaissance faciale.
 *
 * @author Maël Lescoulié
 * @version 1.0
 */
public class Gestionnaire {

    /** Écran principal géré par le gestionnaire */
    private Ecran ecran;
    /** Fenêtre JavaFX principale */
    private Stage fenetrePrincipale;
    /** Base de données d'apprentissage */
    private BaseDeDonnees bdd;
    /** Moteur de reconnaissance faciale */
    private Reconnaissance reco;
    private Eigenfaces faces;
    /** Projection des images dans l'espace des eigenfaces */
    private Projection proj;
    /** Distance choisie par l'utilisateur pour l'identification */
    private String distChoisie;
    /** Fichier sélectionné pour l'analyse */
    private File fichierSelectionne;
    private Image cacheImageMoyenne;


    /**
     * Initialise le gestionnaire avec la fenêtre principale.
     * @param stage fenêtre JavaFX principale
     */
    public Gestionnaire(Stage stage) {
        this.fenetrePrincipale = stage;
        this.ecran = new Ecran();
        this.distChoisie = "euclidienne";
        
        // Initialisation de l'état du menu
        this.ecran.getMenuLateral().getLancerReconnaissance().setDisable(true);
        
        this.initialiserReco();
        this.enregistrerEcouteurs();
    }

    /**
     * Initialise les composants de reconnaissance faciale :
     * charge la base de données, calcule les eigenfaces, crée la projection
     * et calibre le seuil de décision.
     */
    private void initialiserReco() {
        System.out.println("Démarrage de l'apprentissage...");
        try {
            this.bdd = new BaseDeDonnees();
            Acp acp = new Acp(this.bdd);
            SVD svd = new SVD(acp.getMatrice_centree());
            this.faces = new Eigenfaces(svd, acp.getVisage_moyen());
            faces.construire();
            faces.selectionnerK(0.95);

            this.proj = new Projection(faces);
            this.reco = new Reconnaissance(this.bdd, this.proj);
            this.reco.calibrerSeuil();

            System.out.println("Apprentissage terminé - K = " + faces.getK());
        } catch (Exception e) {
            System.err.println("Erreur d'initialisation : " + e.getMessage());
        }
    }

    /**
     * Enregistre les écouteurs pour les boutons et la navigation de l'interface.
     */
    private void enregistrerEcouteurs() {
        MenuLateral menu = this.ecran.getMenuLateral();

        // Bouton "Choisir une image"
        menu.getChoisirFichier().setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Sélectionner un visage à analyser");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.png", "*.pgm"));
            File file = fileChooser.showOpenDialog(fenetrePrincipale);
            
            if (file != null) {
                this.fichierSelectionne = file;
                // On active le lancement maintenant qu'un fichier est prêt
                menu.getLancerReconnaissance().setDisable(false);
                Image imgEntree = new Image(fichierSelectionne.toURI().toString());
                this.ecran.getConteneurPrincipal().getPanneauReco().getVisageEntre().setImage(imgEntree);
            }
        });

        // Bouton "Lancer la reconnaissance"
        menu.getLancerReconnaissance().setOnAction(e -> {
            if (this.fichierSelectionne != null) {
                // Récupération de la distance depuis la ComboBox
                this.distChoisie = menu.getComboDistance().getValue().toLowerCase();
                traiterReconnaissance(this.fichierSelectionne);
            }
        });

        // Boutons de navigation
        menu.getBtnNavReco().setOnAction(e -> {
            ecran.getConteneurPrincipal().afficherReconnaissance();
            menu.getBtnNavReco().setDisable(true);
            menu.getBtnNavVisu().setDisable(false);
        });

        menu.getBtnNavVisu().setOnAction(e -> {
            chargerImageMoyenne();
            // chargerEigenfaces();
            ecran.getConteneurPrincipal().afficherVisualisation();
            menu.getBtnNavReco().setDisable(false);
            menu.getBtnNavVisu().setDisable(true); 
        });

    }

    private void chargerImageMoyenne() {
        if (this.cacheImageMoyenne == null) {
            SimpleMatrix imgMoyenne = faces.getVisageMoyen();
            ImageVect img = new ImageVect(imgMoyenne);
            Image imgJavaFX = SwingFXUtils.toFXImage(img.getBufferedImage(), null);
            this.cacheImageMoyenne = imgJavaFX;
        }
        this.ecran.getConteneurPrincipal().getPanneauVisu().getImageMoyenne().setImage(this.cacheImageMoyenne);
    }

    /**
     * Traite l'image sélectionnée et met à jour l'interface en conséquence.
     * @param fichierImage image choisie par l'utilisateur
     */
    private void traiterReconnaissance(File fichierImage) {
        try {
            ImageVect imageTest = new ImageVect(fichierImage.getAbsolutePath());
            String nomTrouve = this.reco.identifier(imageTest, this.distChoisie);
            
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
                double calculTaux = 100.0 * (1.0 - (vraiDistance / this.reco.getDistanceMax()));
                tauxRessemblance = Math.max(0.0, Math.min(100.0, calculTaux));
            }

            // Mise à jour du PanneauReconnaissance situé dans le ConteneurPrincipal
            this.ecran.getConteneurPrincipal().getPanneauReco().majInterface(imgTrouvee, nomTrouve, tauxRessemblance);
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la reconnaissance : " + e.getMessage());
            this.ecran.getConteneurPrincipal().getPanneauReco().majInterface(null, "Erreur", 0.0);
        }
    }

    /**
     * @return l'écran principal géré par ce gestionnaire
     */
    public Ecran getEcran() {
        return this.ecran;
    }
}
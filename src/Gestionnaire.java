/** Importation des classes nécessaires */
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;

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
    // private BaseDeDonnees bdd;
    /** Référence à la fenêtre principale de l'application */
    private Stage fenetrePrincipale;

    /**
     * Initialise le gestionnaire avec la fenêtre principale.
     * @param stage fenêtre JavaFX principale
     */
    public Gestionnaire(Stage stage) {
        this.fenetrePrincipale = stage;
        this.ecran = new Ecran();
        // this.bdd = new BaseDeDonnees();
        this.enregistrerEcouteurs();
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
        Image imgEntree = new Image(fichierImage.toURI().toString());
        Image imgTrouveeSimulee = imgEntree; 
        String nomTrouveSimule = "Test"; 
        double tauxSimule = 94.5;
        this.ecran.majInterface(imgEntree, imgTrouveeSimulee, nomTrouveSimule, tauxSimule);
    }

    /**
     * @return l'écran principal géré par ce gestionnaire
     */
    public Ecran getEcran() {
        return this.ecran;
    }
}
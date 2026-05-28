import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;

public class Gestionnaire {

    private Ecran ecran;
    // private BaseDeDonnees bdd;
    private Stage fenetrePrincipale;

    public Gestionnaire(Stage stage) {
        this.fenetrePrincipale = stage;
        this.ecran = new Ecran();
        // this.bdd = new BaseDeDonnees();
        this.enregistrerEcouteurs();
    }

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

    private void traiterReconnaissance(File fichierImage) {
        Image imgEntree = new Image(fichierImage.toURI().toString());
        Image imgTrouveeSimulee = imgEntree; 
        String nomTrouveSimule = "Test"; 
        double tauxSimule = 94.5;
        this.ecran.majInterface(imgEntree, imgTrouveeSimulee, nomTrouveSimule, tauxSimule);
    }

    public Ecran getEcran() {
        return this.ecran;
    }
}
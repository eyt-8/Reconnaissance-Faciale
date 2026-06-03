/** Importation des classes nécessaires */
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;

/**
 * Menu latéral de l'application.
 * Il regroupe les boutons de contrôle et les choix de distance.
 *
 * @author Maël Lescoulié
 * @version 1.0
 */
public class MenuLateral extends VBox {
    /** Bouton pour sélectionner un fichier à analyser */
    private Button choisirFichier;
    /** Bouton pour lancer la reconnaissance */
    private Button lancer;
    /** ComboBox pour choisir la distance utilisée */
    private ComboBox<String> choixDistance;
    /** Bouton de navigation vers le mode reconnaissance */
    private Button btnNavReco;
    /** Bouton de navigation vers le mode visualisation */
    private Button btnNavVisu; 

    /**
     * Construit le menu latéral et initialise ses composants.
     */
    public MenuLateral() {
        this.setSpacing(25);
        this.setPrefWidth(250);
        this.setStyle("-fx-background-color: #f4f4f4; -fx-padding: 40 20;");
        this.setAlignment(Pos.TOP_CENTER);

        this.initialiserComposants();
    }

    /**
     * Initialise les composants du menu latéral et assemble l'interface.
     */
    private void initialiserComposants() {
        Label titreDistance = new Label("Type de distance :");
        titreDistance.setStyle("-fx-font-weight: bold;");

        // Bouton de sélection de fichier
        this.choisirFichier = new Button("Choisir une image");
        this.choisirFichier.getStyleClass().add("button");

        // ComboBox pour les distances
        this.choixDistance = new ComboBox<>();
        this.choixDistance.getItems().addAll("Euclidienne", "Mahalanobis", "Cosinus");
        this.choixDistance.setValue("Euclidienne"); // Valeur par défaut
        this.choixDistance.setPrefWidth(160);

        // Bouton de lancement
        this.lancer = new Button("Lancer la reconnaissance");
        this.lancer.getStyleClass().add("btn-action");

        // Boutons de navigation
        btnNavReco= new Button("Reconnaissance");
        btnNavReco.setDisable(true);
        btnNavVisu= new Button("Visualisation");
        this.btnNavReco.getStyleClass().add("button");
        this.btnNavVisu.getStyleClass().add("button");

        this.getChildren().addAll(
            choixDistance, 
            choisirFichier,
            new Separator(),
            lancer,
            new Separator(),
            btnNavReco,
            btnNavVisu
        );
    }

    // Getters
    /**
     * @return le bouton pour choisir un fichier
     */
    public Button getChoisirFichier() {
        return choisirFichier;
    }

    /**
     * @return le bouton pour lancer la reconnaissance
     */
    public Button getLancerReconnaissance() {
        return lancer;
    }

    /**
     * @return la ComboBox du choix de distance
     */
    public ComboBox<String> getComboDistance() {
        return choixDistance;
    }

    /**
     * @return le bouton pour afficher le panneau de reconnaissance
     */
    public Button getBtnNavReco() {
        return btnNavReco;
    }

    /**
     * @return le bouton pour afficher le panneau de visualisation
     */
    public Button getBtnNavVisu() {
        return btnNavVisu;
    }
}
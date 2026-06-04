package application.Presentation;
/** Importation des classes nécessaires */
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

/**
 * Panneau de visualisation des résultats.
 * Il affiche l'image moyenne et les eigenfaces calculées.
 *
 * @author Maël Lescoulié
 * @version 1.0
 */
public class PanneauVisualisation extends VBox {
    /** Vue de l'image moyenne calculée */
    private ImageView imageMoyenne = new ImageView();
    /** Conteneur des images eigenfaces */
    private FlowPane imagesEigenfaces;

    /**
     * Construit le panneau de visualisation et prépare son contenu.
     */
    public PanneauVisualisation() {
        this.setPadding(new Insets(20));
        this.setSpacing(20);
        this.setStyle("-fx-background-color: white;");

        this.imageMoyenne = new ImageView();
        this.imageMoyenne.setFitWidth(200);
        this.imageMoyenne.setPreserveRatio(true);

        this.imagesEigenfaces = new FlowPane();
        this.imagesEigenfaces.setHgap(10);
        this.imagesEigenfaces.setVgap(10);

        ScrollPane scrollPane = new ScrollPane(imagesEigenfaces);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(600);

        this.getChildren().addAll(
            new Label("Image Moyenne :"), imageMoyenne,
            new Label("Eigenfaces :"), scrollPane
        );
    }

    // Getter
    /**
     * @return l'image moyenne affichée
     */
    public ImageView getImageMoyenne() {
        return imageMoyenne;
    }

    /**
     * @return le conteneur des eigenfaces
     */
    public FlowPane getImagesEigenfaces() {
        return imagesEigenfaces;
    }
}
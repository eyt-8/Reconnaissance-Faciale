package application.Presentation;
import java.util.List;

/** Importation des classes nécessaires */
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import org.ejml.simple.SimpleMatrix;

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
    /** Graphique pour visualiser la variance expliquée */
    private LineChart<Number,Number> graphique;

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

        NumberAxis x = new NumberAxis();
        x.setLabel("Nombre d'eigenfaces (K)");
        NumberAxis y = new NumberAxis();
        y.setLabel("Variance expliquée cumulée");
        this.graphique = new LineChart<>(x, y);
        this.graphique.setTitle("Évolution de la variance");
        this.graphique.setCreateSymbols(false);
        this.graphique.setLegendVisible(false);
        this.graphique.setPrefHeight(450);
        this.graphique.setMinHeight(350);

        this.getChildren().addAll(
            new Label("Image Moyenne :"), imageMoyenne,
            new Label("Eigenfaces :"), scrollPane,
            new Label("Graphique variance :"), graphique
        );
    }

    public void afficherEigenfaces(List<Image> listeImages, List<Double> valeursPropres) {
        this.imagesEigenfaces.getChildren().clear();        
        for (int i = 0; i < listeImages.size(); i++) {
            ImageView iv = new ImageView(listeImages.get(i));
            iv.setFitWidth(150);
            iv.setPreserveRatio(true);
            Label lblVP = new Label(String.format("Valeur propre : %.2f", valeursPropres.get(i)));
            VBox vBoxImage = new VBox(new Label("Eigenface " + (i + 1)), iv, lblVP);
            this.imagesEigenfaces.getChildren().add(vBoxImage);
        }
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

    /**
     * Trace la courbe de variance cumulée
     * @param varCumulee
     */
    public void tracerCourbe(SimpleMatrix varCumulee){
        double valeur;
        this.graphique.getData().clear();
        XYChart.Series<Number,Number> serie = new XYChart.Series<>();

        int nbComposantes = varCumulee.getNumRows();

        for (int i =0;i< nbComposantes;i++){
            valeur = varCumulee.get(i,0);
            serie.getData().add(new XYChart.Data<>(i+1,valeur));
        }

        this.graphique.getData().add(serie);

    }
}
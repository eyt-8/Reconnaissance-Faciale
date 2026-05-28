import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class Ecran extends BorderPane {
    private Button choisirFichier;
    private ImageView visageEntre;
    private ImageView visageTrouve;
    private Label nomPrenom;
    private ProgressBar barreRessemblance;
    private Label pourcentage;

    public Ecran() {
        this.initialiserComposants();
        this.assemblerLayout();
    }

    private void initialiserComposants() {
        this.choisirFichier = new Button("Choisir un fichier");
        this.choisirFichier.setStyle("-fx-background-color: #dcdcdc; -fx-text-fill: black; -fx-padding: 8 15;");

        this.visageEntre = new ImageView();
        this.configurerImageView(this.visageEntre);

        this.visageTrouve = new ImageView();
        this.configurerImageView(this.visageTrouve);

        this.nomPrenom = new Label("La personne est ...");
        this.nomPrenom.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-font-style: italic;");

        this.barreRessemblance = new ProgressBar(0.0);
        this.barreRessemblance.setPrefHeight(20);
        this.barreRessemblance.setPrefWidth(350);
        this.barreRessemblance.setStyle("-fx-accent: #7bc07b;");

        this.pourcentage = new Label("Taux de ressemblance :   %");
        this.pourcentage.setStyle("-fx-font-size: 14px;");
    }

    private void assemblerLayout() {
        HBox bandeauHaut = new HBox(new Label("Reconnaissance Faciale"));
        bandeauHaut.setStyle("-fx-background-color: #7bc07b; -fx-padding: 10;");
        bandeauHaut.getChildren().get(0).setStyle("-fx-text-fill: black; -fx-font-size: 18px; -fx-font-weight: bold;");
        this.setTop(bandeauHaut);

        VBox barreLaterale = new VBox(this.choisirFichier);
        barreLaterale.setStyle("-fx-background-color: #f4f4f4; -fx-padding: 40 20;");
        barreLaterale.setAlignment(Pos.TOP_CENTER);
        barreLaterale.setPrefWidth(200);
        this.setRight(barreLaterale);

        VBox blocImage1 = new VBox(10, this.visageEntre, new Label("Visage entré"));
        blocImage1.setAlignment(Pos.CENTER);
        VBox blocImage2 = new VBox(10, this.visageTrouve, new Label("Personne trouvée"));
        blocImage2.setAlignment(Pos.CENTER);
        HBox ligneImages = new HBox(50, blocImage1, blocImage2);
        ligneImages.setAlignment(Pos.CENTER);

        VBox contenuCentral = new VBox(30, ligneImages, this.nomPrenom, this.barreRessemblance, this.pourcentage);
        contenuCentral.setAlignment(Pos.CENTER);
        contenuCentral.setPadding(new Insets(20));
        this.setCenter(contenuCentral);
    }

    private void configurerImageView(ImageView iv) {
        iv.setFitWidth(180);
        iv.setFitHeight(180);
        iv.setPreserveRatio(true);
        try {
            java.io.File fichierSilhouette = new java.io.File("donnees/silhouette.png");
            if (fichierSilhouette.exists()) {
                iv.setImage(new Image(fichierSilhouette.toURI().toString()));
            } else {
                System.err.println("Le fichier donnees/silhouette.png est introuvable.");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de la silhouette : " + e.getMessage());
        }
    }

    public Button getChoisirFichier() {
        return this.choisirFichier;
    }

    public void majInterface(Image imgEntre, Image imgTrouvee, String nom, double taux) {
        if (imgEntre != null) this.visageEntre.setImage(imgEntre);
        if (imgTrouvee != null) this.visageTrouve.setImage(imgTrouvee);
        this.nomPrenom.setText("La personne est " + nom);
        this.barreRessemblance.setProgress(taux / 100.0);
        this.pourcentage.setText("Taux de ressemblance : " + String.format("%.1f", taux) + " %");
    }

}

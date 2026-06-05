package application.Presentation;
/** Importation des classes nécessaires */
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Panneau principal de la reconnaissance faciale.
 * Il affiche l'image d'entrée, l'image trouvée et le taux de ressemblance.
 *
 * @author Maël Lescoulié
 * @version 1.0
 */
public class PanneauReconnaissance extends VBox {
    /** Image de l'entrée utilisateur */
    private ImageView visageEntre = new ImageView();
    /** Image de la personne identifiée */
    private ImageView visageTrouve = new ImageView();
    /** Étiquette affichant le nom reconnu */
    private Label nomPrenom = new Label("La personne est ...");
    /** Barre de progression du taux de ressemblance */
    private ProgressBar barreRessemblance = new ProgressBar(0.0);
    /** Libellé du pourcentage de ressemblance */
    private Label pourcentage = new Label("Taux de ressemblance : 0%");

    /**
     * Construit le panneau de reconnaissance et met en place la présentation.
     */
    public PanneauReconnaissance() {
        this.setSpacing(50);
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(40));
        this.setStyle("-fx-background-color: white;");

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

        // Assemblage
        VBox blocImage1 = new VBox(20, this.visageEntre, new Label("Visage entré"));
        blocImage1.setAlignment(Pos.CENTER);
        
        VBox blocImage2 = new VBox(20, this.visageTrouve, new Label("Personne trouvée"));
        blocImage2.setAlignment(Pos.CENTER);
        
        HBox ligneImages = new HBox(50, blocImage1, blocImage2);
        ligneImages.setAlignment(Pos.CENTER);

        this.getChildren().addAll(ligneImages, this.nomPrenom, this.barreRessemblance, this.pourcentage);
    }

    /**
     * Configure les paramètres d'affichage d'un ImageView.
     * @param iv composant ImageView à configurer
     */
    private void configurerImageView(ImageView iv) {
        iv.setFitWidth(300);
        iv.setFitHeight(300);
        iv.setPreserveRatio(true);
        try {
            java.io.File fichierSilhouette = new java.io.File("donnees/silhouette.png");
            if (fichierSilhouette.exists()) {
                iv.setImage(new Image(fichierSilhouette.toURI().toString()));
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement silhouette : " + e.getMessage());
        }
    }

    /**
     * Met à jour l'interface en affichant le visage d'entrée, le visage trouvé,
     * le nom reconnu et le taux de ressemblance.
     * @param imgEntre image d'entrée
     * @param imgTrouvee image reconnue
     * @param nom nom de la personne trouvée
     * @param taux taux de ressemblance en pourcentage
     */
    public void majInterface(Image imgTrouvee, String nom, double taux) {        
        if (imgTrouvee != null) {
            this.visageTrouve.setImage(imgTrouvee);
        } 
        else {
            // Rechargement silhouette si pas de résultat
            try {
                java.io.File fichierSilhouette = new java.io.File("donnees/silhouette.png");
                if (fichierSilhouette.exists()) {
                    this.visageTrouve.setImage(new Image(fichierSilhouette.toURI().toString()));
                }
            } catch (Exception e) {
                System.err.println("Erreur rechargement silhouette.");
            }
        }
        this.nomPrenom.setText("La personne est " + nom);
        this.barreRessemblance.setProgress(taux / 100.0);
        this.pourcentage.setText("Taux de ressemblance : " + String.format("%.1f", taux) + " %");
    }

    public ImageView getVisageEntre() {
        return this.visageEntre;
    } 
}
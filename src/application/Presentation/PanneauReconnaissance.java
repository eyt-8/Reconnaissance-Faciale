package application.Presentation;
/** Importation des classes nécessaires */
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Panneau principal de la reconnaissance faciale.
 * Il affiche l'image d'entrée, l'image trouvée et le taux de ressemblance.
 *
 * @author Maël Lescoulié Nylan Paillassa
 * @version 1.0
 */
public class PanneauReconnaissance extends VBox {
    /** Image de l'entrée utilisateur */
    private ImageView visageEntre = new ImageView();
    /** Image de la personne identifiée */
    private ImageView visageTrouve = new ImageView();
    /** Étiquette affichant le nom reconnu */
    private Label nomPrenom = new Label("La personne est ...");
    /** Conteneur pour les informations supplémentaires (plus proches images) */
    private VBox infosSupplementaires = new VBox(5);


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
        this.infosSupplementaires.setAlignment(Pos.CENTER);

        // Assemblage
        VBox blocImage1 = new VBox(20, this.visageEntre, new Label("Visage entré"));
        blocImage1.setAlignment(Pos.CENTER);
        
        VBox blocImage2 = new VBox(20, this.visageTrouve, new Label("Personne trouvée"));
        blocImage2.setAlignment(Pos.CENTER);
        
        HBox ligneImages = new HBox(50, blocImage1, blocImage2);
        ligneImages.setAlignment(Pos.CENTER);

        this.getChildren().addAll(ligneImages, this.nomPrenom, this.infosSupplementaires);
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
     * Met à jour l'interface en affichant le visage trouvé, le nom reconnu
     * et les 5 candidats les plus proches.
     * @param imgTrouvee image reconnue (null si inconnu)
     * @param nom nom de la personne trouvée
     * @param details liste des candidats avec leur distance T²
     */
    public void majInterface(Image imgTrouvee, String nom, java.util.List<String> details) {
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
        
        this.infosSupplementaires.getChildren().clear();
        if (details != null && !details.isEmpty()) {
            Label titreInfos = new Label("Images les plus proches :");
            titreInfos.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-underline: true;");
            this.infosSupplementaires.getChildren().add(titreInfos);
            for (String detail : details) {
                Label lbl = new Label(detail);
                lbl.setStyle("-fx-font-size: 12px;");
                this.infosSupplementaires.getChildren().add(lbl);
            }
        }
    }

    public ImageView getVisageEntre() {
        return this.visageEntre;
    } 
}
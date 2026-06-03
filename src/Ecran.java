/** Importation des classes nécessaires */
import java.util.ArrayList;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Ecran représente l'interface principale de l'application.
 * Il assemble les composants JavaFX et met à jour les images et résultats
 * de reconnaissance faciale.
 *
 * @author Maël Lescoulié
 * @version 1.0
 */
public class Ecran extends BorderPane {
    /** Bouton permettant de sélectionner une image à analyser */
    private Button choisirFichier;
    /** Liste des distances possibles sous forme de boutons Radio */
    private ArrayList<RadioButton> distances;
    private ToggleGroup groupeDistances;
    /** Zone d'affichage de l'image entrée par l'utilisateur */
    private ImageView visageEntre;
    /** Zone d'affichage de l'image correspondant à la personne trouvée */
    private ImageView visageTrouve;
    /** Libellé du nom de la personne reconnue */
    private Label nomPrenom;
    /** Barre de progression du taux de ressemblance */
    private ProgressBar barreRessemblance;
    /** Affichage textuel du pourcentage de ressemblance */
    private Label pourcentage;

    /**
     * Initialise l'écran et construit le layout.
     */
    public Ecran() {
        this.initialiserComposants();
        this.assemblerLayout();
    }

    /**
     * Crée les composants graphiques de base utilisés dans l'écran.
     */
    private void initialiserComposants() {
        this.choisirFichier = new Button("Choisir un fichier");
        this.choisirFichier.setStyle("-fx-background-color: #dcdcdc; -fx-text-fill: black; -fx-padding: 8 15;");
        
        // On crée les boutons Radio liés à la distance
        this.distances = new ArrayList<RadioButton>();
        this.distances.add(new RadioButton("Euclidienne"));
        this.distances.add(new RadioButton("Mahalanobis"));
        this.distances.add(new RadioButton("Cosinus"));
        
        this.groupeDistances = new ToggleGroup();
        // Ajoute toutes les distances dans un ToggleGroup pour n'en sélectionner qu'une
        for (int i=0;i<distances.size();i++) {
        	distances.get(i).setToggleGroup(groupeDistances);
        }
        // La distance euclidienne est sélectionnée
        this.distances.get(0).setSelected(true);

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

    /**
     * Assemble le layout principal de l'écran et positionne les composants.
     */
    private void assemblerLayout() {
        HBox bandeauHaut = new HBox(new Label("Reconnaissance Faciale"));
        bandeauHaut.setStyle("-fx-background-color: #7bc07b; -fx-padding: 10;");
        bandeauHaut.getChildren().get(0).setStyle("-fx-text-fill: black; -fx-font-size: 18px; -fx-font-weight: bold;");
        this.setTop(bandeauHaut);

        VBox barreLaterale = new VBox();
        // On ajoute les distances dans les blocs une par une
        for (int i=0;i<distances.size();i++) {
        	barreLaterale.getChildren().add(distances.get(i));
        }
        barreLaterale.getChildren().add(this.choisirFichier);
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

    /**
     * Configure un ImageView avec une taille fixe et une image par défaut.
     * @param iv ImageView à configurer
     */
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

    /**
     * @return le bouton utilisé pour sélectionner un fichier d'image
     */
    public Button getChoisirFichier() {
        return this.choisirFichier;
    }
    
    /**
     * @return le groupe de distances utilisé pour sélectionner un fichier d'image
     */
    public ToggleGroup getGroupeDistances() {
        return this.groupeDistances;
    }

    /**
     * Met à jour l'affichage avec les images et le score de reconnaissance.
     * @param imgEntre image d'entrée sélectionnée
     * @param imgTrouvee image de la personne trouvée
     * @param nom nom reconnu de la personne
     * @param taux pourcentage de ressemblance
     */
    public void majInterface(Image imgEntre, Image imgTrouvee, String nom, double taux) {
        if (imgEntre != null) this.visageEntre.setImage(imgEntre);
        if (imgTrouvee != null) {
            this.visageTrouve.setImage(imgTrouvee);
        }
        else {
            try {
                java.io.File fichierSilhouette = new java.io.File("donnees/silhouette.png");
                if (fichierSilhouette.exists()) {
                    this.visageTrouve.setImage(new Image(fichierSilhouette.toURI().toString()));
                }
            } catch (Exception e) {
                System.err.println("Erreur lors du rechargement de la silhouette.");
            }
        }
        this.nomPrenom.setText("La personne est " + nom);
        this.barreRessemblance.setProgress(taux / 100.0);
        this.pourcentage.setText("Taux de ressemblance : " + String.format("%.1f", taux) + " %");
    }

}

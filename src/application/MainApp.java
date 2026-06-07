package application;
/** Importation des classes nécessaires */
import application.Controle.Gestionnaire;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Point d'entrée JavaFX de l'application de reconnaissance faciale.
 * Cette classe initialise la fenêtre principale et affiche l'interface.
 *
 * @author Maël Lescoulié Nylan Paillassa
 * @version 1.0
 */
public class MainApp extends Application {
    /**
     * Démarre l'application JavaFX et affiche la scène principale.
     * @param primaryStage fenêtre principale fournie par JavaFX
     */
    @Override
    public void start(Stage primaryStage) {
        Gestionnaire gestionnaire = new Gestionnaire(primaryStage);
        Scene scene = new Scene(gestionnaire.getEcran(), 1200, 1080);

        // Chargement du CSS
        try {
            String cssPath = getClass().getResource("/application/Presentation/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.err.println("Erreur : Impossible de charger le fichier style.css");
            e.printStackTrace();
        }

        primaryStage.setTitle("Système de Reconnaissance Faciale");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true); // Empêche le redimensionnement de la fenêtre
        primaryStage.show();
    }

    /**
     * Lance l'application JavaFX.
     * @param args arguments de la ligne de commande
     */
    public static void main(String[] args) {
        launch(args);
    }
}
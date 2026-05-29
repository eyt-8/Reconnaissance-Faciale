/** Importation des classes nécessaires */
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Point d'entrée JavaFX de l'application de reconnaissance faciale.
 * Cette classe initialise la fenêtre principale et affiche l'interface.
 *
 * @author Maël Lescoulié
 * @version 1.0
 */
public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        Gestionnaire gestionnaire = new Gestionnaire(primaryStage);
        Scene scene = new Scene(gestionnaire.getEcran(), 750, 500);
        primaryStage.setTitle("Système de Reconnaissance Faciale");
        primaryStage.setScene(scene);
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
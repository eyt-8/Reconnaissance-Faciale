import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        Gestionnaire gestionnaire = new Gestionnaire(primaryStage);
        Scene scene = new Scene(gestionnaire.getEcran(), 750, 500);
        primaryStage.setTitle("Système de Reconnaissance Faciale");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
/** Importation des classes nécessaires */
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

/**
 * Interface principale de l'application.
 * Elle organise le conteneur central et le menu latéral.
 *
 * @author Maël Lescoulié
 * @version 1.0
 */
public class Ecran extends BorderPane {
    /** Conteneur central contenant les panneaux de l'application */
    private ConteneurPrincipal conteneurPrincipal;
    /** Menu latéral permettant de piloter les actions */
    private MenuLateral menuLateral;

    /**
     * Construit l'écran principal et positionne les éléments de l'interface.
     */
    public Ecran() {
        this.conteneurPrincipal = new ConteneurPrincipal();
        this.menuLateral = new MenuLateral();

        // Titre
        HBox bandeauHaut = new HBox(new Label("Système de Reconnaissance Faciale"));
        bandeauHaut.setStyle("-fx-background-color: #7bc07b; -fx-padding: 15;");
        bandeauHaut.getChildren().get(0).setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        // Layout final
        this.setTop(bandeauHaut);
        this.setCenter(conteneurPrincipal);
        this.setRight(menuLateral);
    }

    // Getters
    /**
     * @return le conteneur principal de l'interface
     */
    public ConteneurPrincipal getConteneurPrincipal() { 
        return conteneurPrincipal; 
    }
    
    /**
     * @return le menu latéral de l'interface
     */
    public MenuLateral getMenuLateral() { 
        return menuLateral; 
    }
}
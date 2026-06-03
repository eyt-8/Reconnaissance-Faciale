/** Importation des classes nécessaires */
import javafx.scene.layout.StackPane;

/**
 * Conteneur principal de l'interface graphique.
 * Il regroupe le panneau de reconnaissance et le panneau de visualisation.
 * Il permet de basculer entre les deux vues.
 *
 * @author Maël Lescoulié
 * @version 1.0
 */
public class ConteneurPrincipal extends StackPane {
    /** Panneau affichant les résultats de reconnaissance faciale */
    private PanneauReconnaissance panneauReco;
    /** Panneau affichant l'image moyenne et les eigenfaces */
    private PanneauVisualisation panneauVisu;

    /**
     * Initialise le conteneur principal et affiche par défaut la vue de reconnaissance.
     */
    public ConteneurPrincipal() {
        this.panneauReco = new PanneauReconnaissance();
        this.panneauVisu = new PanneauVisualisation();

        // Ajout des deux panneaux (le dernier ajouté est au-dessus)
        this.getChildren().addAll(panneauVisu, panneauReco);
        
        // Par défaut, on affiche la reconnaissance
        afficherReconnaissance();
    }

    /**
     * Affiche le panneau de reconnaissance faciale.
     */
    public void afficherReconnaissance() {
        this.panneauReco.toFront();
    }

    /**
     * Affiche le panneau de visualisation des eigenfaces.
     */
    public void afficherVisualisation() {
        this.panneauVisu.toFront();
    }

    // Getters
    public PanneauReconnaissance getPanneauReco() {
        return panneauReco;
    }

    public PanneauVisualisation getPanneauVisu() {
        return panneauVisu;
    }
}
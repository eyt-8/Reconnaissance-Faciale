// Import des objets SimpleMatrix et SimpleEVD
import org.ejml.simple.SimpleMatrix;
import org.ejml.simple.SimpleEVD;

/**
 * Classe pour la décomposition en valeurs propres
 * @author Nylan
 * @version 1.0
 */

public class Propre {
    /**
     * matrice des VECTEURS PROPRES, rangés en COLONNES
     */
    private SimpleMatrix matP;
    /**
     * matrice DIAGONALE des VALEURS PROPRES
     */
    private SimpleMatrix matD;
    /**
     * Matrice à décomposer arrivant par le constructeur ou le setter
     */
    private SimpleMatrix matrice;

    /**
     * Constructeur avec le param
     */
    public Propre(SimpleMatrix matrice) {
        this.matrice = matrice;
    }
 
    public Propre() {
    }
    /**
     * Setter de la matrice à décomposer
     * 
     * @param matrice matrice à décomposer
     */
    public void setMatrice(SimpleMatrix matrice) {
        this.matrice = matrice;
    }

    /**
     * Méthode permettant de fiare la décomposition de matrice à l'aide de la méthode eig de EJML
     * Cette méthode instancie matP et matD permattant ensuite de les réustiliser
     */
    public void decomposer() {
        // Dimension de la matrice (carrée) : n lignes = n colonnes = n valeurs propres.
        int n = matrice.getNumRows();

        // L'objet evd contient les valeurs propres et les vecteurs propres.
        SimpleEVD<SimpleMatrix> evd = matrice.eig();

        // D : matrice diagonale qui recevra les valeurs propres (zéros ailleurs).
        SimpleMatrix D = new SimpleMatrix(n, n);
        // P : matrice qui recevra les vecteurs propres, rangés en colonnes.
        SimpleMatrix P = new SimpleMatrix(n, n);

        // On parcourt chaque couple (valeur propre, vecteur propre), indexé par j.
        for (int j = 0; j < n; j++) {

            // Récupère la j-ème valeur propre. getReal() suffit car la matrice est
            // symétrique : ses valeurs propres sont garanties réelles (partie
            // imaginaire nulle), donc on ignore la composante complexe.
            double lambda = evd.getEigenvalue(j).getReal();
            // On place cette valeur propre sur la diagonale, en position (j, j).
            D.set(j, j, lambda);

            // Récupère le vecteur propre associé à cette valeur propre (colonne n x 1).
            // EJML renvoie null si la valeur propre est complexe : on s'en protège.
            SimpleMatrix v = evd.getEigenVector(j);
            if (v != null) {

                // Norme euclidienne du vecteur. On la calcule pour pouvoir normaliser :
                // on veut une base ORTHONORMÉE (vecteurs de longueur 1), nécessaire
                // pour que la reconstruction A = P * D * P^T fonctionne.
                double norme = v.normF();
                // On ne divise que si la norme n'est pas quasi nulle, pour éviter
                // une division par zéro (1e-12 = seuil de sécurité numérique).
                if (norme > 1e-12) v = v.divide(norme);

                // Recopie composante par composante le vecteur v dans la colonne j de P.
                // i parcourt les lignes : P[i][j] reçoit la i-ème composante de v.
                for (int i = 0; i < n; i++) {
                    P.set(i, j, v.get(i, 0));
                }
            }
        }

        this.matP = P;
        this.matD = D;
    }
    
    /**
     * Accesseur de matP (Matrice Vecteurs Propres)
     */
    public SimpleMatrix getMatP() {
        return matP;
    }
    
    /**
     * Accesseur matD (Matrice Diagonale des Valeurs Propres)
     */
    public SimpleMatrix getMatD() {
        return matD;
    }
}

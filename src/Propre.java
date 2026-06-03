import org.ejml.simple.SimpleMatrix;
import org.ejml.simple.SimpleEVD;

/**
 * Classe pour la décomposition en valeurs propres
 * @author Nylan PAILLASSA
 * @version 1.0
 */

public class Propre {
    /**
     * matrice des VECTEURS PROPRES, rangés en COLONNES
     */
    private SimpleMatrix matP;
    /**
     * vecteur de taille nx1 des VALEURS PROPRES
     */
    private SimpleMatrix vectD;
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
     * Méthode permettant de faire la décomposition de matrice à l'aide de la méthode eig de EJML
     * Cette méthode instancie matP et vectD permattant ensuite de les réutiliser
     */
    public void decomposer() {
        // Dimension de la matrice (carrée) : n lignes = n colonnes = n valeurs propres.
        int n = matrice.getNumRows();

        // L'objet evd contient les valeurs propres et les vecteurs propres.
        SimpleEVD<SimpleMatrix> evd = matrice.eig();

        // D : vecteur qui recevra les valeurs propres (en colonne).
        SimpleMatrix D = new SimpleMatrix(n, 1);
        // P : matrice qui recevra les vecteurs propres, rangés en colonnes.
        SimpleMatrix P = new SimpleMatrix(n, n);

        // On parcourt chaque couple (valeur propre, vecteur propre), indexé par j.
        for (int j = 0; j < n; j++) {
            double lambda = evd.getEigenvalue(j).getReal();
            D.set(j, 0, lambda);

            // Récupère le vecteur propre associé à cette valeur propre (colonne n x 1).
            // EJML renvoie null si la valeur propre est complexe : on s'en protège.
            SimpleMatrix v = evd.getEigenVector(j);
            if (v != null) {
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
        this.vectD = D;
    }
    
    /**
     * Accesseur de matP (Matrice Vecteurs Propres)
     */
    public SimpleMatrix getMatP() {
        return matP;
    }
    
    /**
     * Accesseur vectD (vecteur des Valeurs Propres)
     */
    public SimpleMatrix getVectD() {
        return vectD;
    }
}

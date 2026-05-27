import org.ejml.simple.SimpleMatrix;


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
     */
    public void decomposer(){
        int n = matrice.numRows();
        SimpleEVD<SimpleMatrix> evd = matrice.eig();
        matP = valParFacto(matrice);
    }

    /**
     * Méthode qui renvoie la matrice des VALEURS PROPRES (diagonale) et construit
     * la matrice des vecteurs propres dans matP.
     * @param matrice matrice symetrique carree a decomposer
     * @return matrice DIAGONALE contenant les valeurs propres
     */
    public SimpleMatrix valParFacto(SimpleMatrix matrice){
    int n = matrice.numRows();
 
    SimpleEVD<SimpleMatrix> evd = matrice.eig();
 
    SimpleMatrix D = new SimpleMatrix(n, n);   // diagonale des valeurs propres
    SimpleMatrix P = new SimpleMatrix(n, n);   // vecteurs propres en colonnes
 
    for (int j = 0; j < n; j++) {
        // Valeur propre j (reelle pour une matrice symetrique).
        double lambda = evd.getEigenvalue(j).getReal();
        D.set(j, j, lambda);

        // Vecteur propre associe (colonne n x 1). null si valeur propre complexe.
        SimpleMatrix v = evd.getEigenVector(j);
        if (v != null) {
            // Normalisation (norme 1) pour une base orthonormee propre.
            double norme = v.normF();
            if (norme > 1e-12) {
                v = v.divide(norme);
            }
            for (int i = 0; i < n; i++) {
                P.set(i, j, v.get(i, 0));
            }
        }
    }

    this.matP = P;
    this.matD = D;
    return D;
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

    /**
     * Test sans utilisation du reste du projet
     */
    // public static void main(String[] args) {
 
    //     // --- MATRICE DE TEST (factice) ---
    //     // Symetrique 2x2. trace = 7, det = 11 => valeurs propres ~ 5.30 et 1.70.
    //     double[][] data = {
    //             {4.0, 1.0},
    //             {1.0, 3.0}
    //     };
    //     SimpleMatrix test = new SimpleMatrix(data);
 
    //     System.out.println("=== Matrice de test ===");
    //     test.print();
 
    //     Propre propre = new Propre(test);
    //     propre.decomposer();
 
    //     System.out.println("=== matD (valeurs propres) ===");
    //     propre.getMatD().print();
 
    //     System.out.println("=== matP (vecteurs propres en colonnes) ===");
    //     propre.getMatP().print();
 
    //     // --- Verification : P * D * P^T doit redonner la matrice de depart ---
    //     SimpleMatrix reconstruite =
    //             propre.getMatP().mult(propre.getMatD()).mult(propre.getMatP().transpose());
    //     System.out.println("=== Reconstruction P*D*P^T (doit ~= test) ===");
    //     reconstruite.print();
    // }
}

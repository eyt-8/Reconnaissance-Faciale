package application.Abstraction;
import org.ejml.simple.SimpleMatrix;

/**
 * @author Nylan PAILLASSA
 * @version 1.0
 */

/**
 * Classe Eigenfaces : représente la base réduite des axes principaux (eigenfaces)
 * obtenue à partir de l'ACP des visages de référence.
 */
public class Eigenfaces {
    /**
     * Vecteur du visageMoyen
     */
    private SimpleMatrix visageMoyen;
    /**
     * SVD permettant de récupérer les informations pour le calcul de la base des eigenfaces
     */
    private SVD svd;
    /**
     * eigenfaces sélectionnées (colonnes) => Matrice A centrée
     */
    private SimpleMatrix baseReduite;
    /**
     * valeurs propres associées
     */
    private SimpleMatrix valPropres;
    /**
     * nombre d'eigenfaces retenues
     */
    private int k;

    /**
     * Constructeur de eigenfaces
     * @param svd
     * @param visageMoyen
     */
    public Eigenfaces(SVD svd, SimpleMatrix visageMoyen) {
        this.k = 0;
        this.svd = svd;
        this.visageMoyen = visageMoyen;
    }

    /**
     * Constructeur chaîné en prenant en compte que l'on peut mettre plus tard
     * le visageMoyen avec un setter
     * @param svd
     */
    public Eigenfaces(SVD svd) {
        this.k = 0;
        this.svd = svd;
    }
    
    /**
     * Construit la base d'eigenfaces à partir des valeurs propres et vecteurs propres.
     */
    public void construire() {
        // Tri décroissant des valeurs propres (et réorganisation des vecteurs)
    	
        // vp  vecteur colonne des valeurs propres (taille m x 1)
    	// vec matrice des vecteurs propres en colonnes (taille n x m)
    	SimpleMatrix vp = svd.getbValSinguliere();
    	SimpleMatrix vec = svd.getU();
    	
        int m = vp.getNumRows();
        Integer[] indices = new Integer[m];
        for (int i = 0; i < m; i++) indices[i] = i;

        // Tri des indices selon la valeur propre décroissante
        java.util.Arrays.sort(indices, (a, b) ->
            Double.compare(vp.get(b, 0), vp.get(a, 0)));

        SimpleMatrix vpTrie = new SimpleMatrix(m, 1);
        SimpleMatrix vecTrie = new SimpleMatrix(vec.getNumRows(), m);
        
        for (int j = 0; j < m; j++) {
            int src = indices[j];
            vpTrie.set(j, 0, vp.get(src, 0));
            for (int i = 0; i < vec.getNumRows(); i++) {
                vecTrie.set(i, j, vec.get(i, src));
            }
        }

        this.valPropres = vpTrie;
        this.baseReduite = vecTrie;     // par défaut, on garde toutes les composantes
        this.k = vecTrie.getNumCols();
    }

    /**
     * Sélectionne le nombre K d'eigenfaces nécessaires pour atteindre
     * un certain seuil de variance expliquée cumulée (ex : 0.9 = 90%).
     * Met à jour 'base' pour ne garder que les K premières colonnes.
     *
     * @param seuil proportion de variance à conserver (entre 0 et 1)
     */
    public void selectionnerK(double seuil) {
        SimpleMatrix varCum = varianceExpliquee(); // variance expliquée cumulée
        int nbComposantes = varCum.getNumRows();

        int kChoisi = nbComposantes; // par défaut on garde tout
        for (int i = 0; i < nbComposantes; i++) {
            if (varCum.get(i, 0) >= seuil) {
                kChoisi = i + 1; // i+1 composantes suffisent
                break;
            }
        }

        this.k = kChoisi;
        // On tronque la base aux K premières colonnes : base[:, 0..k-1]
        this.baseReduite = this.baseReduite.extractMatrix(0, this.baseReduite.getNumRows(), 0, this.k);
    }

    /**
     * Calcule la variance expliquée CUMULÉE par les composantes.
     * variance expliquée d'une composante i = valPropre_i / somme(valPropres)
     *
     * @return vecteur colonne où l'élément i = variance cumulée des i+1 premières composantes
     */
    public SimpleMatrix varianceExpliquee() {
        int m = valPropres.getNumRows();

        // Somme totale des valeurs propres
        double total = 0.0;
        for (int i = 0; i < m; i++) {
            total += valPropres.get(i, 0);
        }

        // Variance cumulée
        SimpleMatrix varCum = new SimpleMatrix(m, 1);
        double cumul = 0.0;
        for (int i = 0; i < m; i++) {
            cumul += valPropres.get(i, 0);
            varCum.set(i, 0, cumul / total);
        }
        return varCum;
    }


    // Getters
    public SimpleMatrix getValPropresK() {
        return this.valPropres.extractMatrix(0, this.k, 0, 1);
    }

    public SimpleMatrix getBase() {
        return baseReduite;
    }

    public SimpleMatrix getValPropres() {
        return valPropres;
    }

    public int getK() {
        return k;
    }

    public SimpleMatrix getVisageMoyen() {
        return this.visageMoyen;
    }
    
    public SVD getSvd() {
        return this.svd;
    }

    public SimpleMatrix getEigenface(int i) {
        return this.baseReduite.extractVector(false, i);
    }

    // Setters
    public void setVisageMoyen(SimpleMatrix visageMoyen) {
        this.visageMoyen = visageMoyen;
    }
}



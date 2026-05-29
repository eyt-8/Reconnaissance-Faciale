import org.ejml.simple.SimpleMatrix;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.image.BufferedImage;

public class ImageVect {

    private int longueur;
    private int largeur;
    private String nom; // nom du fichier
    private SimpleMatrix vecteurCol; // vecteur de l'image
    private BufferedImage image;
    private File fichier;

    /**
     * Constructeur d'ImageVect
     * @param chemin nom du chemin du fichier
     * @throws IOException si on trouve pas le fichier
     */
    public ImageVect(String chemin) throws IOException{
        this.fichier = new File(chemin);
        this.image = ImageIO.read(this.fichier);

        if (this.image == null){
            throw new IOException("Le chemin ne marche pas\n");
        }

        this.nom = this.fichier.getName();
        this.nom = this.nom.substring(0, this.nom.length() - 4);
        this.longueur = this.image.getHeight();
        this.largeur = this.image.getWidth();
        this.vecteurCol = new SimpleMatrix(this.largeur*this.longueur,1);
    }

    /**
     * Accesseur de nom
     * @return le nom de l'image
     */
    public String getNom(){
        return this.nom;
    }

    /**
     * Accesseur de longueur
     * @return la longueur de l'image
     */
    public int getLongueur(){
        return this.longueur;
    }

    /**
     * Accesseur de largeur
     * @return la largeur de l'image
     */
    public int getLargeur(){
        return this.largeur;
    }

    public BufferedImage getBufferedImage(){
        return this.image;
    }

    public SimpleMatrix getVecteurCol(){
        return this.vecteurCol;
    }

    /**
     * Transforme l'image en vecteur colonne
     */
    public void vectoriser(){
        int index = 0;
        for (int i=0;i<this.getLargeur();i++){
            for (int j=0;j<this.getLongueur();j++){
                int pixel = this.image.getRGB(i,j); // Récupère pixel
                double pixelAjoute = pixel & 0xFF; // Récupère la valeur du pixel (0 à 255)
                this.vecteurCol.set(index,0,pixelAjoute);
                index++;
            }
        }
    }

}


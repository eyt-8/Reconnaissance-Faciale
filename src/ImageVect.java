import org.ejml.simple.SimpleMatrix;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.image.BufferedImage;

public class ImageVect {

    private int longueur;
    private int largeur;
    private String nom; // nom du fichier
    private SimpleMatrix vecteurCol;
    private BufferedImage image;
    private File fichier;

    public ImageVect(String chemin) throws IOException{
        this.fichier = new File(chemin);
        this.image = ImageIO.read(this.fichier);

        if (this.image == null){
            throw new IOException("Le chemin ne marche pas!");
        }

        this.nom = this.fichier.getName();
        this.nom = this.nom.substring(0, this.nom.length() - 4);
        this.longueur = this.image.getHeight();
        this.largeur = this.image.getWidth();
        this.vecteurCol = new SimpleMatrix(1,this.largeur*this.longueur);
    }

    public String getNom(){
        return this.nom;
    }

    public int getLongueur(){
        return this.longueur;
    }

    public int getLargeur(){
        return this.largeur;
    }

    public void vectoriser(){
        int index = 0;
        for (int i=0;i<this.getLargeur();i++){
            for (int j=0;j<this.getLongueur();j++){
                int pixel = this.image.getRGB(i,j); // Récupère pixel
                double pixelAjoute = pixel & 0xFF;
                this.vecteurCol.set(index,0,pixelAjoute);
                index++;
            }
        }
    }

    public static void main(String[] args){

        try {
            ImageVect img = new ImageVect("donnees/apprentissage/Alysa Liu/alysa1.jpg");
            System.out.println("Image chargée avec succès !");
            System.out.println("Nom du fichier : " + img.getNom());
            System.out.println("Dimensions : " + img.getLargeur() + " x " + img.getLongueur() + " pixels");
            System.out.println(img.vecteurCol);
        } catch (IOException e) {
            System.out.println(e);
        }
        


    }



}

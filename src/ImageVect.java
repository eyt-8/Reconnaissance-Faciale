import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.simple.SimpleMatrix;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.Color;
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
    
    public ImageVect(SimpleMatrix vec_eigenface, int chiffre) throws IOException {
    	this.fichier = null;
    	this.vecteurCol = vec_eigenface;
    	// On convertit le vecteur en image
    	
    	int taille = (int)Math.floor(Math.sqrt(this.vecteurCol.getNumRows()));
    	
    	this.vecteurCol.reshape(taille, taille);
    	
	    BufferedImage image_dest = new BufferedImage(taille,taille,BufferedImage.TYPE_INT_RGB);
	    for(int i=0; i<taille; i++) {
	        for(int j=0; j<taille; j++) {
	        	double decentrer = this.vecteurCol.get(j, i)+this.vecteurCol.elementMinAbs();
	        	decentrer = decentrer /(this.vecteurCol.elementMinAbs()+this.vecteurCol.elementMaxAbs());
	            int a = (int)Math.floor(Math.abs(decentrer*255));
	            System.out.println(a);
	            Color newColor = new Color(a,a,a);
	            image_dest.setRGB(j,i,newColor.getRGB());
	        }
	    }
	    File output = new File("eigenface"+chiffre+".jpg");
	    ImageIO.write(image_dest, "jpg", output);
	
    	
    }

    /**
     * Deuxième constructeur d'ImageVect
     * @param vecteur vecteut colonne
     * @param largeur largeur de l'image
     * @param longueur longueur de l'image
     */
    public ImageVect(SimpleMatrix vecteur, int largeur, int longueur){
        this.nom = "ImageReconstruite";
        this.longueur = longueur;
        this.largeur = largeur;
        this.vecteurCol = vecteur;
        this.fichier = null;
        this.image = this.devectoriser(vecteur, largeur, longueur);
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

    /**
     * Accesseur de l'image
     * @return l'image original
     */
    public BufferedImage getBufferedImage(){
        return this.image;
    }


    /**
     * Accesseur du vecteur
     * @return le vecteur colonne
     */
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

    /**
     * Transforme le vecteur colonne en son apparence original
     * @param vecteur vecteur colonne
     * @param largeur largeur original
     * @param longueur longueur original
     * @return l'image original
     */
    public BufferedImage devectoriser(SimpleMatrix vecteur, int largeur, int longueur){

        BufferedImage img = new BufferedImage(largeur, longueur, BufferedImage.TYPE_INT_RGB);

        int index = 0;

        for (int i=0;i<largeur;i++){
            for (int j=0;j<longueur;j++){

                double valeur = vecteur.get(index,0);
                // Transforme en valeur de gris
                int pixelGris = (int) Math.max(0,Math.min(255, valeur));
                // Remet en valeur RGBx 
                int pixel = (255<<24) | (pixelGris<<16) | (pixelGris<<8) | pixelGris;
                img.setRGB(i, j, pixel);
                index++;

            }
        }

        return img;
    }

}


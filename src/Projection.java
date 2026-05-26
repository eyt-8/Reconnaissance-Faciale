import org.ejml.simple.SimpleMatrix;

// TODO : A confirmer en fonction des méthodes des autres classes

public class Projection {
    private Eigenfaces eigenfaces;
    private SimpleMatrix coords;

    public Projection(Eigenfaces eigenfaces) {
        this.eigenfaces=eigenfaces;
    }

    public SimpleMatrix projeter(Image img) {
        SimpleMatrix vImage = img.vectoriser();
        SimpleMatrix visageMoyen = this.eigenfaces.getVisageMoyen();
        SimpleMatrix vCentre = vImage.minus(visageMoyen);
        SimpleMatrix baseEigenfaces = this.eigenfaces.getBase();
        this.coords = vCentre.mult(baseEigenfaces);
        return this.coords;
    }

    public Image reconstruire(SimpleMatrix coords) {
        SimpleMatrix baseEigenfaces = this.eigenfaces.getBase();
        SimpleMatrix vCentreReconstruit = coords.mult(baseEigenfaces.transpose());
        SimpleMatrix visageMoyen = this.eigenfaces.getVisageMoyen();
        SimpleMatrix vImagePixels = vCentreReconstruit.plus(visageMoyen);
        Image imgReconstruite = new Image(); // TODO
        return imgReconstruite;
    }

    public double erreurReconstruction(Image j, Image jp) {
        SimpleMatrix vOriginal = j.vectoriser();
        SimpleMatrix vReconstruit = jp.vectoriser();
        SimpleMatrix difference = vOriginal.minus(vReconstruit);
        double erreur = difference.normF();
        return erreur;
    }

    public SimpleMatrix varianceCumulee() {
        SimpleMatrix valPropres = this.eigenfaces.getValPropres();
        int taille = valPropres.getNumElements();
        SimpleMatrix varianceCumulee = new SimpleMatrix(taille,1); // Inverse ?
        double total = valPropres.elementSum();
        double cumul = 0;
        for (int i=0 ; i<taille ; i++) {
            cumul += valPropres.get(i);
            varianceCumulee.set(i, cumul/total);
        }
        return varianceCumulee;
    }
}

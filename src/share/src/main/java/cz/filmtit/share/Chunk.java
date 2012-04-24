package cz.filmtit.share;


/**
 * @author Joachim Daiber
 */
public class Chunk {

    private String surfaceform;

    public Chunk(String surfaceform) {
        this.surfaceform = surfaceform;
    }

    public String getSurfaceform() {
        return surfaceform;
    }

    public void setSurfaceform(String surfaceform) throws IllegalAccessException {
        if (this.surfaceform == null) { this.surfaceform = surfaceform; }
        else {
            throw new IllegalAccessException("The chunk surface form can be set just once.");
        }
    }
}

package cz.filmtit.share;


/**
 * @author Joachim Daiber
 */
public class Chunk {

    private String surfaceform;

    public Chunk() {
        // nothing
    }

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

    @Override
    public String toString() {
        return "Chunk[" + surfaceform + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Chunk)) return false;

        Chunk chunk = (Chunk) o;

        return surfaceform.equals(chunk.surfaceform);
    }

    @Override
    public int hashCode() {
        return surfaceform.hashCode();
    }
}

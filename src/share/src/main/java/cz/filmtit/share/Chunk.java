package cz.filmtit.share;

import cz.filmtit.share.annotations.Annotation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Joachim Daiber
 */
public class Chunk implements com.google.gwt.user.client.rpc.IsSerializable, Serializable {

    private String surfaceform="";
    
    //I do NOT want to create a constructor with list so scala doesn't add 
    //its own implementation of java lists
    private List<Annotation> annotations;

    public Chunk() {
    	// nothing
    }

    
    public Chunk(String surfaceform) {
        this.surfaceform = surfaceform;
    }

    public String getSurfaceForm() {
        return surfaceform;
    }

    public void setSurfaceForm(String surfaceform) throws /*IllegalAccessException*/ IllegalArgumentException {
        if (this.surfaceform == null) { this.surfaceform = surfaceform; }
        else {
            //throw new IllegalAccessException("The chunk surface form can be set just once.");
        	// GWT does not know IllegalAccessException - rewritten:
        	throw new IllegalArgumentException("The chunk surface form can be set just once.");
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

    public List<Annotation> getAnnotations() {
        return annotations == null ? Collections.<Annotation>emptyList() : annotations;
    }

    public void clearAnnotations() {
        this.annotations.clear();
    }

    public void addAnnotation(Annotation annotation) {
        if (this.annotations == null)
            this.annotations = new ArrayList<Annotation>();

        this.annotations.add(annotation);
    }

    public void addAnnotations(Collection<Annotation> annotations) {
        if (this.annotations == null)
            this.annotations = new ArrayList<Annotation>();

        this.annotations.addAll(annotations);
    }

    public void removeAnnotation(int index) {
        if (this.annotations != null)
            this.annotations.remove(index);
    }
}

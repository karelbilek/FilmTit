package cz.filmtit.share;

import cz.filmtit.share.annotations.AnnotationType;
import cz.filmtit.share.annotations.Annotation;

import com.google.gwt.user.client.rpc.GwtTransient;
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
    
    private List<Annotation> annotations;

    @GwtTransient
    private String[] tokens = null;

    public boolean isTokenized() {
        return (tokens != null);
    }

    public void setTokens(String[] tokens) throws Exception {
        if (tokens== null) {
            throw new Exception("Cannot unset tokens");
        }
        if (isTokenized()) {
            throw new Exception("Cannot reset tokens.");
        }
        this.tokens = tokens;
    }

    public String[] getTokens() throws Exception {
        if (isTokenized()) {
            return tokens;
        } else {
            throw new Exception("cannot get no tokens");
            
//            return new String[]{};
        }
    }

    public Chunk() {
    	// nothing
    }

    public Chunk(String surfaceform, List<Annotation> annotations) {
        this.surfaceform = surfaceform.replace('\u0000',' ');
        this.annotations=annotations;
    }

    
    public Chunk(String surfaceform) {
        this.surfaceform = surfaceform.replace('\u0000',' ');
    }

    public String getSurfaceForm() {
        return surfaceform;
    }

    public void setSurfaceForm(String surfaceform) {
        this.surfaceform = surfaceform.replace('\u0000', ' ');
        /*if (this.surfaceform.equals(surfaceform)) { return; }
        if (this.surfaceform == null || this.surfaceform.equals("")) {
            this.surfaceform = surfaceform.replace('\u0000', ' ');
        }
        else {
            //throw new IllegalAccessException("The chunk surface form can be set just once.");
        	throw new IllegalArgumentException("The chunk surface form can be set just once.");
        }*/
    }

    /**
     * Displays the surface form of the Chunk.
     */
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
 
    public String getGUIForm(){
        String result = this.getSurfaceForm();
        for (Annotation an:this.getAnnotations()) {
            if (an.getType().equals(AnnotationType.LINEBREAK)){
                int index = an.getBegin();
                result = result.substring(0, index)+"<br />"+result.substring(index, result.length());
            }
        }
        for (Annotation an:this.getAnnotations()) {
            if (an.getType().equals(AnnotationType.DIALOGUE)){
                result = "- "+result;
            }
        }
        return result;     
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

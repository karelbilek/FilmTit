package cz.filmtit.share;

import cz.filmtit.share.annotations.AnnotationType;
import cz.filmtit.share.annotations.Annotation;
import cz.filmtit.share.parsing.Parser;

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

	public boolean isActive = true;
	
    private volatile String surfaceForm = "";
    
    protected List<Annotation> annotations;

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

    public Chunk(String surfaceForm, List<Annotation> annotations) {
        this.surfaceForm = surfaceForm.replace('\u0000',' ');
        this.annotations=annotations;
    }

    
    public Chunk(String surfaceForm) {
        this.surfaceForm = surfaceForm.replace('\u0000',' ');
    }

    /**
     * Only the string, with no annotations (newlines are turned into spaces).
     * @return
     */
    public String getSurfaceForm() {
        return surfaceForm;
    }

    /**
     * The string with annotations, as in an SRT file (newlines are turned into |).
     * @return
     */
    public String getDatabaseForm() {
        return getFormatedForm("- "," | ");
    }

    /**
     * The string with annotations, as in an SRT file (newlines are turned into |).
     * @return
     */
    public void setDatabaseForm(String form) {
        copyFromOther(Parser.getChunkFromText(form));
    }

    /**
     * The string with annotations, as in an SRT file (newlines are turned into |).
     * Deletes previously existing format annotations.
     * @return
     */
    public void setDatabaseFormForce(String form) {
        //how the heck is this different from the previous one?
        copyFromOther(Parser.getChunkFromText(form));
    }

    /**
     * Copies data from other chunk.
     * @param other chunk to copy data from
     */
    public void copyFromOther(Chunk other) {
        setSurfaceForm(other.getSurfaceForm());
        setFormattingAnnotations(other.getAnnotations());
    }

    /**
     * The string with annotations, newlines turned into newlineString, dialogue dashes into dashString.
     * @return
     */
    public String getFormatedForm(String dashString, String newlineString) {
        String displayForm = getSurfaceForm();
        
        //we are doing annots from left to right
        //and if we move the string, we have to move the positions too
        int movedAlready=0;

        if (annotations != null) {
            Collections.sort(annotations);
            for (Annotation annotation : annotations)  {
                int pos = annotation.getBegin() + movedAlready;
                switch (annotation.getType()) {
                    case DIALOGUE:
                        displayForm = displayForm.substring(0, pos) + dashString
                                + displayForm.substring(pos);
                        movedAlready += dashString.length();
                        break;
                    case LINEBREAK:
                        displayForm = displayForm.substring(0, pos) + newlineString
                                + displayForm.substring(pos + 1);
                        //+1 / -1, because in surfaceform, there is space after linebreak
                        //but we don't want the space here
                        movedAlready += newlineString.length() - 1;
                        break;
                }
            }
        }
        

        return displayForm;
    }


    public boolean isDialogue() {
        for (Annotation annotation: annotations) {
            if (annotation.getType() == AnnotationType.DIALOGUE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Only the string, with no annotations (newlines are turned into spaces).
     * @return
     */
    public void setSurfaceForm(String surfaceform) {
        this.surfaceForm = surfaceform.replace('\u0000', ' ');
        /*if (this.surfaceForm.equals(surfaceForm)) { return; }
        if (this.surfaceForm == null || this.surfaceForm.equals("")) {
            this.surfaceForm = surfaceForm.replace('\u0000', ' ');
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
        return "Chunk[" + surfaceForm + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Chunk)) return false;

        Chunk chunk = (Chunk) o;

        return surfaceForm.equals(chunk.surfaceForm);
    }

    @Override
    public int hashCode() {
        return surfaceForm.hashCode();
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
 
    /**
     * The string with annotations in HTML form (newlines are turned into <br />).
     * @return
     */
    public String getGUIForm(){
        return getFormatedForm("- ", "<br />");
    }

//    intended to be used where the text is displayed in a textarea but not used at the moment
//
//    /**
//     * The string with annotations in text form (newlines are turned into \n).
//     * @return
//     */
//    public String getTextForm(){
//        return getFormatedForm("- ", "\n");
//    }
//    
//    /**
//     * The string with annotations in text form (newlines are turned into \n).
//     * @return
//     */
//    public void setTextForm(String form){
//    	RegExp newline = RegExp.compile("[\n\r]+", "g");
//    	String dbForm = newline.replace(form, " | ");
//        setDatabaseForm(dbForm);
//    }
    
    /**
     * Add new annotations, keeping the original ones.
     */
    public void addAnnotations(Collection<Annotation> annotations) {
        if (this.annotations == null)
            this.annotations = new ArrayList<Annotation>();

        this.annotations.addAll(annotations);
    }

    /**
     * Set new formatting annotations, deleting the previous ones.
     * @param annotations
     */
    public void setFormattingAnnotations(Collection<Annotation> annotations) {
    	// remove old formatting annotations = readd non-formatting annotations
    	List<Annotation> newAnnotations = new ArrayList<Annotation>();
    	
        if (this.annotations!=null) {
            for (Annotation annotation : this.annotations) {
                if (
                        annotation.getType() == AnnotationType.ORGANIZATION ||
                        annotation.getType() == AnnotationType.PERSON ||
                        annotation.getType() == AnnotationType.PLACE
                        ) {
                    
                    annotations.add(annotation);
                }
            }
        }
        // add new format annotations
        newAnnotations.addAll(annotations);
        // set
        this.annotations = newAnnotations;
    }

    public void removeAnnotation(int index) {
        if (this.annotations != null)
            this.annotations.remove(index);
    }
}

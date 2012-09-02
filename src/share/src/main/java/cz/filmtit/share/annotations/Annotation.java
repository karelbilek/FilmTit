package cz.filmtit.share.annotations;

import java.io.Serializable;

/**
 *  Class represents named entities. String, which are not send to
 *  TM for quering.
 */

public class Annotation implements com.google.gwt.user.client.rpc.IsSerializable, Serializable, Comparable<Annotation> {

    /**
     * Type of name entity
     */
    private AnnotationType type;
    // (zero-based) position of the first character of the annotated substring
    private int begin;
    // (zero-based) position of the first character AFTER the annotated substring
    private int end;
    // i.e. "Peter lives at home" would have substring Peter annotated as (AnnotationType.PERSON, 0, 5)

    /**
     * Create empty instance
     */
    public Annotation() {
    	// do nothing;
    }

    /**
     * Create new instance with
     * @param type  type of annotation
     * @param begin  start position
     * @param end    end position
     */
    public Annotation(AnnotationType type, int begin, int end) {
        
        this.type = type;
        this.begin = begin;
        this.end = end;

    }

    /**
     * Returns type of annotation
     * @return  annotation type
     */
    public AnnotationType getType() {
        return type;
    }

    /**
     *  Gets start position
     * @return   start position
     */
    public int getBegin(){
        return begin;
    }

    /**
     * Gets end position
     * @return  end position
     */
    public int getEnd() {
        return end;
    }

    /**
     * Compares start position of annotation
     * @param other annotation
     * @return  0 - same start position
     *          0 > - other starts earlier
     *          0 < - other starts later
     */
    public int compareTo(Annotation other) {
        return (begin - other.begin);
    }

}

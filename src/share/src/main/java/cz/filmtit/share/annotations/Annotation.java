package cz.filmtit.share.annotations;

import java.io.Serializable;


public class Annotation implements com.google.gwt.user.client.rpc.IsSerializable, Serializable {
    private AnnotationType type;
    // (zero-based) position of the first character of the annotated substring
    private int begin;
    // (zero-based) position of the first character AFTER the annotated substring
    private int end;
    // i.e. "Peter lives at home" would have substring Peter annotated as (AnnotationType.PERSON, 0, 5)

    public Annotation() {
    	// do nothing;
    }
    
    public Annotation(AnnotationType type, int begin, int end) {
        
        this.type = type;
        this.begin = begin;
        this.end = end;

    }

    public AnnotationType getType() {
        return type;
    }

    public int getBegin(){
        return begin;
    }

    public int getEnd() {
        return end;
    }

}

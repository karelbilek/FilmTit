package cz.filmtit.share.annotations;

import java.io.Serializable;

public class Annotation implements com.google.gwt.user.client.rpc.IsSerializable, Serializable {
    private AnnotationType type;
    private int begin;
    private int end;

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

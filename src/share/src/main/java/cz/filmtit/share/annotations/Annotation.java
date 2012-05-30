package cz.filmtit.share.annotations;

public class Annotation {
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

package cz.filmtit.share;

import java.util.List;
import java.util.ArrayList;

public class Document {
    public int year;
    public long workStartTime;
    public long spentOnThisTime;
    public String language;
    public long translationGenerationTime;
    
    public List<Chunk> chunks;
    
    public Document() {
    	this.chunks = new ArrayList<Chunk>();
    }
}

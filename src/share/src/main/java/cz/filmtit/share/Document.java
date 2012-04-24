package cz.filmtit.share;

import java.util.List;
import java.util.ArrayList;

public class Document {
    public String movieTitle;
    public int year;
    public long spentOnThisTime;
    public String language;

    public List<TranslationResult> chunks;
    
    public Document() {
    	this.chunks = new ArrayList<TranslationResult>();
    }
}

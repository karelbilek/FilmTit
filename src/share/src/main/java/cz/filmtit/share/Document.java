package cz.filmtit.share;

import java.util.List;
import java.util.ArrayList;

public class Document {
    public String movieTitle;
    public int year;
    public long spentOnThisTime;
    public String language;

    public List<TranslationResult> translationResults = new ArrayList<TranslationResult>();
    
    public Document() {
    	// nothing
    }

}

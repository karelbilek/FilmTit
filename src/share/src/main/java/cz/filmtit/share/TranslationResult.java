package cz.filmtit.share;

import java.util.LinkedList;
import java.util.List;

public class TranslationResult {

    private TimedChunk sourceChunk;
    private String userTranslation;
    private List<TranslationPair> translationPairs = new LinkedList<TranslationPair>();
    private int feedback;
	
    public TimedChunk getSourceChunk() {
        return sourceChunk;
    }

    public String getUserTranslation() {
        return userTranslation;
    }

    public void setUserTranslation(String userTranslation) {
        this.userTranslation = userTranslation;
    }
    
    public void setTmSuggestions(Object o) {
    	
    }
}	


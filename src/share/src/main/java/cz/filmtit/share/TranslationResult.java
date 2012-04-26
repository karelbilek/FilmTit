package cz.filmtit.share;

import java.util.List;

/**

*/

public class TranslationResult {
    private long id;
    private TimedChunk sourceChunk;
    private List<TranslationPair> tmSuggestions;
    private String userTranslation;
    private long selectedTranslationPairID;

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<TranslationPair> getTmSuggestions() {
        return tmSuggestions;
    }

    public void setTmSuggestions(List<TranslationPair> tmSuggestions) {
        this.tmSuggestions = tmSuggestions;
    }

    public String getUserTranslation() {
        return userTranslation;
    }

    public void setUserTranslation(String userTranslation) {
        this.userTranslation = userTranslation;
    }

    public long getSelectedTranslationPairID() {
        return selectedTranslationPairID;
    }

    public void setSelectedTranslationPairID(int selectedTranslationPairID) {
        this.selectedTranslationPairID = selectedTranslationPairID;
    }

    public TimedChunk getSourceChunk() {
        return sourceChunk;
    }

    public void setSourceChunk(TimedChunk sourceChunk) {
        this.sourceChunk = sourceChunk;
    }
}

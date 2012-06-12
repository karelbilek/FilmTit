package cz.filmtit.share;

import java.io.Serializable;
import java.util.List;
import java.util.LinkedList;

/**
 * Class representing the result sent from the core corresponding to the request by a source-chunk,
 * packaging the given source-chunk and the translation-suggestions from the core.
 * Additionaly, the translation from the user is present, for its storage and feedback.
 */
public class TranslationResult implements com.google.gwt.user.client.rpc.IsSerializable, Comparable<TranslationResult>, Serializable {
    private TimedChunk sourceChunk;
    private List<TranslationPair> tmSuggestions=new LinkedList<TranslationPair>();
    private String userTranslation;
    private long selectedTranslationPairID;

    public int getChunkId() {
        return sourceChunk.getId();
    }

    public void setChunkId(int id) {
        sourceChunk.setId(id);
    }

    public long getDocumentId() {
    	return sourceChunk.getDocumentId();
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

    public void setSelectedTranslationPairID(long selectedTranslationPairID) {
        this.selectedTranslationPairID = selectedTranslationPairID;
    }

    public TimedChunk getSourceChunk() {
        return sourceChunk;
    }

    public void setSourceChunk(TimedChunk sourceChunk) {
        this.sourceChunk = sourceChunk;
    }

    /**
     * Compares the two TranslationResults according to their source chunks.
     */
	public int compareTo(TranslationResult that) {		
		return this.sourceChunk.compareTo(that.sourceChunk);
	}
	
	/**
	 * If comparing two TranslationResults, they are equal iff their proper compareTo returns 0.
	 */
	@Override
	public boolean equals(Object that) {
		if (that instanceof TranslationResult) {
			return (this.compareTo((TranslationResult) that) == 0) ? true : false;
		}
		else return super.equals(that);
	}
}

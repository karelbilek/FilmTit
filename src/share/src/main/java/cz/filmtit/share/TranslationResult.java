package cz.filmtit.share;

import cz.filmtit.share.annotations.Annotation;
import java.io.Serializable;
import java.util.List;
import java.util.LinkedList;

/**
 * Class representing the result sent from the core corresponding to the request by a source-chunk,
 * packaging the given source-chunk and the translation-suggestions from the core.
 * Additionally, the translation from the user is present, for its storage and feedback.
 */
public class TranslationResult implements com.google.gwt.user.client.rpc.IsSerializable, Comparable<TranslationResult>, Serializable {
    private volatile TimedChunk sourceChunk;
    private volatile List<TranslationPair> tmSuggestions=new LinkedList<TranslationPair>();
    private volatile String userTranslation;
    private volatile long selectedTranslationPairID;

    public TranslationResult() {
        sourceChunk = new TimedChunk();
    }

    public TranslationResult(TimedChunk sourceChunk) {
        this.sourceChunk = sourceChunk;
    }

    public int getChunkId() {
        return sourceChunk.getId();
    }

    public void setChunkId(int id) {
        sourceChunk.setId(id);
    }

    public long getDocumentId() {
    	return sourceChunk.getDocumentId();
    }

    public void setDocumentId(long documentId) {
        sourceChunk.setDocumentId(documentId);
    }
    
    public List<TranslationPair> getTmSuggestions() {
        return tmSuggestions;
    }

    public void setTmSuggestions(List<TranslationPair> tmSuggestions) {
        this.tmSuggestions = tmSuggestions;
    }


    //TODO: arbitrary newlines
    //TODO: dialogues
    public TimedChunk getUserTranslationAsChunk() {
        TimedChunk source = this.getSourceChunk();
        TimedChunk tc = new TimedChunk(this.getUserTranslation(), source.getStartTime(), source.getEndTime(), new LinkedList<Annotation>());
        return tc;
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
	
	/**
	 * Prints out the source chunk surface form.
	 */
	@Override
	public String toString() {
		return "TranslationResult[" + sourceChunk.toString() + "->" + userTranslation + "(" + selectedTranslationPairID + ")]";
	}

    public TranslationResult resultWithoutSuggestions() {
         if (tmSuggestions == null || tmSuggestions.size() == 0) {
             return this;
         }

        TranslationResult clone = new TranslationResult();
        clone.selectedTranslationPairID = selectedTranslationPairID;
        clone.sourceChunk = sourceChunk;
        clone.userTranslation = userTranslation;
        return clone;
    }
}

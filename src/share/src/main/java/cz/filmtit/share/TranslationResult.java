package cz.filmtit.share;

import java.io.Serializable;
import java.util.List;

/**

*/

public class TranslationResult implements Serializable, Comparable<TranslationResult> {
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

    public void setSelectedTranslationPairID(long selectedTranslationPairID) {
        this.selectedTranslationPairID = selectedTranslationPairID;
    }

    public TimedChunk getSourceChunk() {
        return sourceChunk;
    }

    public void setSourceChunk(TimedChunk sourceChunk) {
        this.sourceChunk = sourceChunk;
    }

	public int compareTo(TranslationResult that) {
		// TODO: compare differently for various subtitle formats, i.e.
		// - lexicographically for srt
		// - numerically for sub
		// - ???
		
		// this.startTime < that.startTime ?
		int result = this.getSourceChunk().getStartTime().compareTo(that.getSourceChunk().getStartTime());
		
		if (result == 0) {
			// this.startTime == that.startTime
			// this.endTime < that.endTime ?
			result = this.getSourceChunk().getEndTime().compareTo(that.getSourceChunk().getEndTime());			
			
			if (result == 0) {
				// this.endTime == that.endTime
				// this.partNumber < that.partNumber ?
				result = (int) Math.signum(that.getSourceChunk().getPartNumber() - this.sourceChunk.getPartNumber());
			}
		}
		
		return result;
	}
}

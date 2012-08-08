package cz.filmtit.share;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;
import java.util.*;
public class Document implements IsSerializable, Serializable, Comparable<Document> {
    public long spentOnThisTime;

    // Generated by Userspace (i.e. probably by the database)
    private long id = Long.MIN_VALUE;
    private String title;
    private MediaSource movie;
    private Language language;
    private long lastChange;
    private int totalChunksCount;
    private int translatedChunksCount;

    private long userId = Long.MIN_VALUE;

    public TreeMap<ChunkIndex, TranslationResult> translationResults = new TreeMap<ChunkIndex, TranslationResult>();
    
	public Document() {
    	// nothing
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Document(String title, String langCode) {
        this.title = title;
		this.language = Language.fromCode(langCode);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
        if (this.id == id) { return; }
        if (this.id != Long.MIN_VALUE) {
            throw new UnsupportedOperationException("Once the document ID is set, it cannot be changed.");
        }
        this.id = id;
	}

    public MediaSource getMovie() {
        return movie;
    }

    public void setMovie(MediaSource movie) {
        this.movie = movie;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguageCode(String languageCode) {
        language = Language.fromCode(languageCode);
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        if (this.userId == userId) { return; }
        if (this.userId != Long.MIN_VALUE) {
            throw  new UnsupportedOperationException("Once the owner ID is set, it cannot be changed.");
        }
        this.userId = userId;
    }

    public long getLastChange() {
        return lastChange;
    }

    public void setLastChange(long lastChange) {
        this.lastChange = lastChange;
    }

    public int getTotalChunksCount() {
        return totalChunksCount;
    }

    public void setTotalChunksCount(int totalChunksCount) {
        this.totalChunksCount = totalChunksCount;
    }

    public int getTranslatedChunksCount() {
        return translatedChunksCount;
    }

    public void setTranslatedChunksCount(int translatedChunksCount) {
        this.translatedChunksCount = translatedChunksCount;
    }

    public List<TranslationResult> getSortedTranslationResults() {
       List<TranslationResult> res = new ArrayList<TranslationResult>(translationResults.size());
       //sorted because treeset
       for (ChunkIndex i:translationResults.keySet()) {
          res.add(translationResults.get(i));
       }
       return res;
    }

    public Map<ChunkIndex, TranslationResult> getTranslationResults() {
        return translationResults;
    }

    /**
     * Return the document without translation results. If the translation results are loaded in the document,
     * a clone of the document not containing them is created.
     * @return Document without translation results.
     */
    public Document documentWithoutResults() {
        if (translationResults == null || translationResults.size() == 0) {
            return this;
        }

        Document clone = new Document();

        clone.id = id;
        clone.language = language;
        clone.movie = movie;
        clone.title = title;
        clone.userId = userId;
        clone.lastChange = lastChange;
        clone.totalChunksCount = totalChunksCount;
        clone.translatedChunksCount = translatedChunksCount;
        
        return clone;
    }

    
    
    @Override
    public int compareTo(Document other) {
        if (this.lastChange > other.lastChange) { return -1; }
        if (this.lastChange < other.lastChange) { return 1; }
        return 0;
    }
}

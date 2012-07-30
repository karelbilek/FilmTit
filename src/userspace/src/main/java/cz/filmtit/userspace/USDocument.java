package cz.filmtit.userspace;

import cz.filmtit.share.ChunkIndex;
import cz.filmtit.share.Document;
import cz.filmtit.share.Language;
import cz.filmtit.share.MediaSource;
import org.hibernate.Session;

import java.util.*;

/**
 * Represents a subtitle file the user work with.
 * @author Jindřich Libovický
 */
public class USDocument extends DatabaseObject {
    private static final int MINIMUM_MOVIE_YEAR = 1850;
    private static final int ALLOWED_FUTURE_FOR_YEARS = 5;

    private long ownerDatabaseId=0;
    private Document document;
    private Map<ChunkIndex, USTranslationResult> translationResults;
    private long workStartTime;
    private long translationGenerationTime;
    private boolean finished;
    
    public USDocument(Document document, USUser user) {
        this.document = document;
        workStartTime = new Date().getTime();
        translationResults = Collections.synchronizedMap(new HashMap<ChunkIndex, USTranslationResult>());
        
        //it should not be null, but I am lazy to rewrite the tests
        if (user != null) {
            this.ownerDatabaseId = user.getDatabaseId();
        }

        Session dbSession = HibernateUtil.getSessionWithActiveTransaction();
        saveToDatabase(dbSession);
        HibernateUtil.closeAndCommitSession(dbSession);
    }

    /**
     * Default constructor (for Hibernate).
     */
    public USDocument() {
        document = new Document();
        translationResults = Collections.synchronizedMap(new HashMap<ChunkIndex, USTranslationResult>());
        ownerDatabaseId = 0; 
    }

    public long getOwnerDatabaseId() {
        return ownerDatabaseId;
    }


    //this should not be run anywhere in regular code!
    //it is here only for hibernate
    public void setOwnerDatabaseId(long ownerDatabaseId) throws Exception {
        if (this.ownerDatabaseId!=0) {
            throw new Exception("you should not reset the owner. It is " + this.ownerDatabaseId);
        }
        this.ownerDatabaseId = ownerDatabaseId;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public Document getDocument() {
		return document;
	}

     public String getMovieTitle() {
        return document.getMovie().getTitle();
    }



    public String getYear() {
        return document.getMovie().getYear();
    }


    /**
     * Gets the time spent on translating this subtitles valid right now.
     * @return The time spent on this subtitles in milliseconds.
     */
    public long getSpentOnThisTime() {
        return document.spentOnThisTime + (new Date()).getTime() - workStartTime;
    }

    /**
     * Sets the new time which was spent on translating this subtitles.
     * If this is set, the work start time reset to current time.
     * @param spentOnThisTime New value of time spent on this document.
     */
    public void setSpentOnThisTime(long spentOnThisTime) {
        document.spentOnThisTime = spentOnThisTime;
        workStartTime = new Date().getTime();
    }

    public Language getLanguage() {
        return document.getLanguage();
    }

    public String getLanguageCode() {
        return document.getLanguage().getCode();
    }
    
    public void setLanguageCode(String languageCode) {
        document.setLanguageCode(languageCode);
    }

    public MediaSource getMediaSource() {
        return document.getMovie();
    }

    private void setMediaSource(MediaSource movie) {
        document.setMovie(movie);
    }

    public long getTranslationGenerationTime() {
        return translationGenerationTime;
    }

    public void setTranslationGenerationTime(long translationGenerationTime) {
        this.translationGenerationTime = translationGenerationTime;
    }

    protected long getSharedClassDatabaseId() {
        return document.getId();
    }

    protected void setSharedClassDatabaseId(long databaseId) {
        document.setId(databaseId);
    }

    protected void setMovie(MediaSource movie) {
        document.setMovie(movie);
    }

    protected MediaSource getMovie() {
        return document.getMovie();
    }

    public Map<ChunkIndex, USTranslationResult> getTranslationResults() {
        return translationResults;
    }


    /**
     * Loads the translationResults from User Space database if there are some
     */
    public void loadChunksFromDb() {
        org.hibernate.Session dbSession = HibernateUtil.getSessionWithActiveTransaction();
    
        // query the database for the translationResults
        List foundChunks = dbSession.createQuery("select c from USTranslationResult c where c.documentDatabaseId = :d")
                .setParameter("d", databaseId).list();

        translationResults = Collections.synchronizedMap(new HashMap<ChunkIndex, USTranslationResult>());
        for (Object o : foundChunks) {
            USTranslationResult result = (USTranslationResult)o;
            result.setDocument(this);
            translationResults.put(result.getTranslationResult().getSourceChunk().getChunkIndex(), result);
        }
    
        HibernateUtil.closeAndCommitSession(dbSession);

        //Collections.sort(translationResults);
        // add the translation results to the inner document
        for (Map.Entry<ChunkIndex, USTranslationResult> usResult : translationResults.entrySet()) {
            document.getTranslationResults().put(usResult.getKey(), usResult.getValue().getTranslationResult());
        }

    }

    /**
     * Saves the document to the database including the Translation Results in the case they were loaded
     * or created.
     * @param dbSession  Opened database session.
     */
    public void saveToDatabase(Session dbSession) {
        saveJustObject(dbSession);

        if (translationResults != null) {
            for (USTranslationResult translationResult : translationResults.values()) {
                translationResult.saveToDatabase(dbSession);
            }
        }
    }

    public void deleteFromDatabase(Session dbSession) {
        deleteJustObject(dbSession);
    }

    public void addTranslationResult(USTranslationResult translationResult) {
        addOrReplaceTranslationResult(translationResult);   
    }

    public void addOrReplaceTranslationResult(USTranslationResult usTranslationResult) {
        ChunkIndex chunkIndex = usTranslationResult.getTranslationResult().getSourceChunk().getChunkIndex();
        translationResults.put(chunkIndex, usTranslationResult);
        document.getTranslationResults().put(chunkIndex, usTranslationResult.getTranslationResult());
    }

    public void replaceTranslationResult(USTranslationResult usTranslationResult) {
        addOrReplaceTranslationResult(usTranslationResult);
  }

}

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
    private static final int ALLOWED_FUTURE_FOR_YEARS = 10;

    private volatile long ownerDatabaseId = 0;
    private volatile USUser owner = null;
    private volatile Document document;
    private SortedMap<ChunkIndex, USTranslationResult> translationResults; // make sure it's a synchronized map
    private volatile long workStartTime;
    private volatile long translationGenerationTime;
    private volatile boolean toBeDeleted = false;
    private volatile boolean finished;

    public USDocument(Document document, USUser user) {
        this.document = document;
        workStartTime = new Date().getTime();
        document.setLastChange(new Date().getTime());
        translationResults = Collections.synchronizedSortedMap(new TreeMap<ChunkIndex, USTranslationResult>());
        
        //it should not be null, but I am lazy to rewrite the tests
        if (user != null) {
            this.ownerDatabaseId = user.getDatabaseId();
        }

        Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();
        saveToDatabase(dbSession);
        usHibernateUtil.closeAndCommitSession(dbSession);
    }

    /**
     * Default constructor (for Hibernate).
     */
    public USDocument() {
        document = new Document();
        translationResults = Collections.synchronizedSortedMap(new TreeMap<ChunkIndex, USTranslationResult>());
        ownerDatabaseId = 0;
    }

    public long getOwnerDatabaseId() {
        return ownerDatabaseId;
    }

    private void setOwnerDatabaseId(long ownerDatabaseId) throws Exception {
        if (this.ownerDatabaseId !=0 ) {
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

    /**
     * Gets the time spent on translating this subtitles valid right now.
     * @return The time spent on this subtitles in milliseconds.
     */
    private long getSpentOnThisTime() {
        return document.getSpentOnThisTime() + (new Date()).getTime() - workStartTime;
    }

    /**
     * Sets the new time which was spent on translating this subtitles.
     * If this is set, the work start time reset to current time.
     * @param spentOnThisTime New value of time spent on this document.
     */
    public void setSpentOnThisTime(long spentOnThisTime) {
        document.setSpentOnThisTime(spentOnThisTime);
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

    private long getTranslationGenerationTime() {
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

    public String getTitle() {
        return document.getTitle();
    }

    public void setTitle(String title) {
        document.setTitle(title);
    }
    
    public Set<ChunkIndex> getTranslationResultKeys() {
        if (translationResults == null) { return null; }
        return translationResults.keySet();
    }

    public void removeTranslationResult(ChunkIndex index) {
        translationResults.remove(index); 
    }

    public Collection<USTranslationResult> getTranslationResultValues() {
        if (translationResults == null) { return null; }
        return translationResults.values();
    }
    
    public USTranslationResult getTranslationResultForIndex(ChunkIndex i) {
        return translationResults.get(i);
    }

    public void setTranslationResultForIndex(ChunkIndex i, USTranslationResult tr) {
        //Collections.sort(translationResults);
        if (tr == null) {
            throw new RuntimeException("Someone is trying to put null translationResult!");
        }
        translationResults.put(i, tr);
    }

    private long getLastChange() {
        return document.getLastChange();
    }

    public void setLastChange(long lastChange) {
        document.setLastChange(lastChange);
    }

    private int getTotalChunksCount() {
        return document.getTotalChunksCount();
    }

    private void setTotalChunksCount(int totalChunksCount) {
        document.setTotalChunksCount(totalChunksCount);
    }

    public int getTranslatedChunksCount() {
        return document.getTranslatedChunksCount();
    }

    public void setTranslatedChunksCount(int translatedChunksCount) {
        document.setTranslatedChunksCount(translatedChunksCount);
    }

    public boolean isToBeDeleted() {
        return toBeDeleted;
    }

    public void setToBeDeleted(boolean toBeDeleted) {
        this.toBeDeleted = toBeDeleted;
    }

    public USUser getOwner() {
        return owner;
    }

    public void setOwner(USUser owner) {
        this.owner = owner;
    }

    /**
     * Loads the translationResults from User Space database if there are some
     */
    public synchronized void loadChunksFromDb() {
        org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();
    
        // query the database for the translationResults
        List foundChunks = dbSession.createQuery("select c from USTranslationResult c where c.documentDatabaseId = :d")
                .setParameter("d", databaseId).list();

        translationResults = Collections.synchronizedSortedMap(new TreeMap<ChunkIndex, USTranslationResult>());
        for (Object o : foundChunks) {
            USTranslationResult result = (USTranslationResult)o;
            result.setDocument(this);
            if (result==null) {
                throw new RuntimeException("Someone is trying to put null translationResult!");
            }
            translationResults.put(result.getTranslationResult().getSourceChunk().getChunkIndex(), result);
        }
    
        usHibernateUtil.closeAndCommitSession(dbSession);

        //Collections.sort(translationResults);
        // add the translation results to the inner document
        for (Map.Entry<ChunkIndex, USTranslationResult> usResult : translationResults.entrySet()) {
            document.getTranslationResults().put(usResult.getKey(), usResult.getValue().getTranslationResult());
        }
        document.setTotalChunksCount(translationResults.size());
    }

    /**
     * Saves the document to the database including the Translation Results in the case they were loaded
     * or created.
     * @param dbSession  Opened database session.
     */
    public synchronized void saveToDatabase(Session dbSession) {
        if (document.getMovie() != null) {
            dbSession.saveOrUpdate(document.getMovie());
        }
        saveJustObject(dbSession);

        if (translationResults != null) {
            for (USTranslationResult translationResult : translationResults.values()) {
                translationResult.saveToDatabase(dbSession);
            }
        }
    }

    public void saveToDatabaseJustDocument(Session dbSession) {
        if (document.getMovie() != null) {
            dbSession.saveOrUpdate(document.getMovie());
        }
        saveJustObject(dbSession);
    }

    public synchronized void deleteFromDatabase(Session dbSession) {
        deleteJustObject(dbSession);
    }

    /**
     * Adds the translation result to the document,
     * replacing a possible translation result with the same ChunkIndex.
     * @param translationResult
     */
    public void addTranslationResult(USTranslationResult translationResult) {
        addOrReplaceTranslationResult(translationResult);
        document.setTotalChunksCount(translationResults.size());
    }

    /**
     * Puts the translation result into the list of translation results of the document
     * (both the USDocument and the Document).
     * The translation result is identified by the inner ChunkIndex;
     * if a translation result with the same ChunkIndex already exists,
     * it is replaced by this new one.
     * @param usTranslationResult
     */
    public synchronized void addOrReplaceTranslationResult(USTranslationResult usTranslationResult) {
        ChunkIndex chunkIndex = usTranslationResult.getTranslationResult().getSourceChunk().getChunkIndex();
        translationResults.put(chunkIndex, usTranslationResult);
        document.getTranslationResults().put(chunkIndex, usTranslationResult.getTranslationResult());
        document.setTotalChunksCount(translationResults.size());
    }

    public void replaceTranslationResult(USTranslationResult usTranslationResult) {
        addOrReplaceTranslationResult(usTranslationResult);
    }
}

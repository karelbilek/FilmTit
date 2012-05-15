package cz.filmtit.userspace;

import cz.filmtit.core.Factory;
import cz.filmtit.core.Configuration;
import cz.filmtit.core.model.TranslationMemory;
import cz.filmtit.share.Chunk;
import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.TranslationPair;
import cz.filmtit.share.TranslationResult;
import org.hibernate.Session;

import java.io.File;
import java.util.*;

/**
 * Represents a subtitle chunk together with its timing, translation suggestions from the translation memory
 * and also the user translation.
 * @author Jindřich Libovický
 */
public class USTranslationResult extends DatabaseObject {
    private TranslationResult translationResult;
    private long documentDatabaseId;
    private USDocument parent; // is set if and only if it's created from the docoument side

    public USTranslationResult(TimedChunk chunk) {
        translationResult = new TranslationResult();
        translationResult.setSourceChunk(chunk);
        //translationResult.setId(chunk.getId());

        Session dbSession = HibernateUtil.getSessionFactory().getCurrentSession();
        dbSession.beginTransaction();
        saveToDatabase(dbSession);
        dbSession.getTransaction().commit();
    }

    /**
     * Default constructor for Hibernate.
     */
    public USTranslationResult() {
        translationResult = new TranslationResult();
    }

    /**
     * Creates an instance of User Space Chunk from the shared Match.
     *
     * It just assigns it to the inner variable, User Space objects
     * wrapping the contained translations are created when necessary.
     * @param c
     */
    public USTranslationResult(TranslationResult c) {
        translationResult = c;
    }
    
    public TranslationResult getTranslationResult() {
	    return translationResult;
	}

    public int hashCode() {
        return translationResult.hashCode();
    }    
    
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass()) { return false; }
        return  translationResult.equals(((USTranslationResult)obj).translationResult);
    }

    public long getDocumentDatabaseId() {
        return documentDatabaseId;
    }

    public void setDocumentDatabaseId(long documentDatabaseId) {
        this.documentDatabaseId = documentDatabaseId;
    }

    public String getStartTime() {
        return translationResult.getSourceChunk().getStartTime();
    }

    public void setStartTime(String startTime) {
        translationResult.getSourceChunk().setStartTime(startTime);
    }

    public String getEndTime() {
        return translationResult.getSourceChunk().getEndTime();
    }

    public void setEndTime(String endTime) {
        translationResult.getSourceChunk().setEndTime(endTime);
    }

    public String getText() {
        return translationResult.getSourceChunk().getSurfaceForm();
    }

    public void setText(String text) throws IllegalAccessException {
        translationResult.getSourceChunk().setSurfaceForm(text);
    }

    public String getUserTranslation() {
        return translationResult.getUserTranslation();
    }

    public void setUserTranslation(String userTranslation) {
        translationResult.setUserTranslation(userTranslation);
    }

    public int getPartNumber() {
        return translationResult.getSourceChunk().getPartNumber();
    }

    public void setPartNumber(int partNumber) {
        translationResult.getSourceChunk().setPartNumber(partNumber);
    }

    public long getSelectedTranslationPairID() {
        return translationResult.getSelectedTranslationPairID();
    }

    public void setSelectedTranslationPairID(long selectedTranslationPairID) {
        translationResult.setSelectedTranslationPairID(selectedTranslationPairID);
    }

    public int getSharedId() {
        return translationResult.getChunkId();
    }

    public void setSharedId(int sharedId) {
        // TODO: Make the property immutable
        translationResult.setChunkId(sharedId);
    }

    private List<TranslationPair> getTmSuggestions() {
        return translationResult.getTmSuggestions();
    }

    private void setTmSuggestions(List<TranslationPair> tmSuggestions) {
        translationResult.setTmSuggestions(tmSuggestions);
    }

    protected long getSharedDatabaseId() { return databaseId; }
    protected void setSharedDatabaseId(long setSharedDatabaseId) { }

    public void generateMTSuggestions(TranslationMemory TM) {
        if (TM == null) { return; } // TODO: remove this when the it will possible to create the TM in tests

        // TODO: ensure none of the potential previous suggestions is in the server cache collection
        // dereference of current suggestion will force hibernate to remove them from the db as well
        translationResult.setTmSuggestions(null);

        scala.collection.immutable.List<TranslationPair> TMResults =
                TM.nBest(translationResult.getSourceChunk(), parent.getLanguage(), parent.getMediaSource(), 10, false);

        // the retrieved Scala collection must be transformed to a Java collection
        // otherwise it cannot be iterated by the for loop
        Collection<TranslationPair> javaList =
                scala.collection.JavaConverters.asJavaCollectionConverter(TMResults).asJavaCollection();

        // the list of suggestions will be stored as a synchronized list
        translationResult.setTmSuggestions(Collections.synchronizedList(new ArrayList<TranslationPair>(javaList)));
    }

    public void saveToDatabase(Session dbSession) {
        saveJustObject(dbSession);
    }

    public void deleteFromDatabase(Session dbSession) {
        deleteJustObject(dbSession);
    }
}

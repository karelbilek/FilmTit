package cz.filmtit.userspace;

import cz.filmtit.core.model.TranslationMemory;
import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.TranslationPair;
import cz.filmtit.share.TranslationResult;
import org.hibernate.Session;
import org.hibernate.annotations.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a subtitle chunk together with its timing, translation suggestions from the translation memory
 * and also the user translation in the User Space. It is a wrapper of the TranslationResult class from the
 * share namespace. Unlike the other User Space objects, the Translations Results stays in the database even
 * in cases the the document the Translation Result belongs to is deleted.
 *
 * @author Jindřich Libovický
 */
public class USTranslationResult extends DatabaseObject implements Comparable<USTranslationResult> {
    /**
     * The shared object which is wrapped by the USTranslationResult.
     */
    private TranslationResult translationResult;
    /**
     * A sign if the feedback to the core has been already provided.
     */
    private boolean feedbackSent = false;
    /**
     * The document this translation result is part of.
     */
    private USDocument parent;

    /**
     * Sets the document the Translation Result belongs to. It is called either when a new translation
     * result is created or when the loadChunksFromDb() on a document is called.
      * @param parent A document the Translation Result is part of.
     */
    public void setParent(USDocument parent) {
        this.parent = parent;
    }

    /**
     * Creates the Translation Result object from the Timed Chunk. It is typically called when the User Space
     * receives a TimedChunk from the client. The object is immediately stored in the database, but the
     * TM core is not queried for the translation suggestions in the constructor. It happens latter in
     * a separate method.
     * @param chunk
     */
    public USTranslationResult(TimedChunk chunk) {
        translationResult = new TranslationResult();
        translationResult.setSourceChunk(chunk);

        Session dbSession = HibernateUtil.getSessionWithActiveTransaction();
        saveToDatabase(dbSession);
        HibernateUtil.closeAndCommitSession(dbSession);
    }

    /**
     * A default constructor used by Hibernate.
     */
    private USTranslationResult() {
        translationResult = new TranslationResult();
    }

    public TranslationResult getTranslationResult() {
	    return translationResult;
	}

    public long getDocumentDatabaseId() {
        return translationResult.getDocumentId();
    }

    public void setDocumentDatabaseId(long documentDatabaseId) {
        translationResult.setDocumentId(documentDatabaseId);
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

    @Type(type="text")
    public String getText() {
        return translationResult.getSourceChunk().getSurfaceForm();
    }

    @Type(type="text")
    public void setText(String text) throws IllegalAccessException {
        translationResult.getSourceChunk().setSurfaceForm(text);
    }

    @Type(type="text")
    public String getUserTranslation() {
        return translationResult.getUserTranslation();
    }

    @Type(type="text")
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
        if (TM == null) { return; }

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
        translationResult.setTmSuggestions(new ArrayList<TranslationPair>(javaList));
    }

    public void saveToDatabase(Session dbSession) {
        saveJustObject(dbSession);
    }

    public void deleteFromDatabase(Session dbSession) {
        deleteJustObject(dbSession);
    }

    private boolean isFeedbackSent() {
        return feedbackSent;
    }

    private void setFeedbackSent(boolean feedbackSent) {
        this.feedbackSent = feedbackSent;
    }

    /**
     * Queries the database for a list of translation results which were not marked as checked
     * and mark them as checked. This is then interpreted as that a feedback for has been provided
     * and the chunks are ready to be deleted from the database.
     * @return  A list of unchecked translation results.
     */
    public static List<TranslationResult> getUncheckedResults() {
        Session dbSession = HibernateUtil.getSessionWithActiveTransaction();


        List queryResult = dbSession.createQuery("select t from USTranslationResult t " +
                "where t.feedbackSent = false and t.userTranslation != null").list();

        List<TranslationResult> results = new ArrayList<TranslationResult>();

        for (Object tr : queryResult) {
            USTranslationResult usResult = (USTranslationResult)tr;
            usResult.setFeedbackSent(true);
            usResult.saveToDatabase(dbSession);
            results.add(usResult.getTranslationResult());
        }
        HibernateUtil.closeAndCommitSession(dbSession);
        return results;
    }

    /**
     * Compares the object with a different one based on the start time of the chunks. In fact
     * only the compareTo method of the wrapped TranslationResult object is called.
     * @param other A Translation Result which is compared to this one
     * @return Result of comparison of the translation result with other chunk.
     */
    @Override
    public int compareTo(USTranslationResult other) {
        return translationResult.compareTo(other.getTranslationResult());
    }
}

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
 * and also the user translation.
 * @author Jindřich Libovický
 */
public class USTranslationResult extends DatabaseObject implements Comparable<USTranslationResult> {
    private TranslationResult translationResult;
    private boolean feedbackSent = false;
    private USDocument parent; // is set if and only if it's created from the document side
    
    public void setParent(USDocument parent) {
        this.parent = parent;

    }

    public USTranslationResult(TimedChunk chunk) {
        translationResult = new TranslationResult();
        translationResult.setSourceChunk(chunk);
        //translationResult.setId(chunk.getId());

        //TODO: solve this!
        /*
        Session dbSession = HibernateUtil.getSessionFactory().getCurrentSession();
        dbSession.beginTransaction();
        saveToDatabase(dbSession);
        dbSession.getTransaction().commit();*/
    }

    /**
     * Default constructor for Hibernate.
     */
    private USTranslationResult() {
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
     * and mark them as checked. This is then interpreted as that a feedback has been provided
     *
     * @return  A list of unchecked translation results.
     */
    public static List<TranslationResult> getUncheckedResults() {
        Session dbSession = HibernateUtil.getSessionFactory().getCurrentSession();

        dbSession.beginTransaction();

        List queryResult = dbSession.createQuery("select t from USTranslationResult t " +
                "where t.feedbackSent = false and t.userTranslation != null").list();

        List<TranslationResult> results = new ArrayList<TranslationResult>();

        for (Object tr : queryResult) {
            USTranslationResult usResult = (USTranslationResult)tr;
            usResult.setFeedbackSent(true);
            usResult.saveToDatabase(dbSession);
            results.add(usResult.getTranslationResult());
        }

        dbSession.getTransaction().commit();
        return results;
    }

    @Override
    public int compareTo(USTranslationResult other) {
        return translationResult.compareTo(other.getTranslationResult());
    }
}

package cz.filmtit.userspace;

import cz.filmtit.core.model.TranslationMemory;
import cz.filmtit.share.ChunkIndex;
import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.TranslationPair;
import cz.filmtit.share.TranslationResult;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private USDocument document;

    private static USHibernateUtil usHibernateUtil = new USHibernateUtil();
    /**
     * Sets the document the Translation Result belongs to. It is called either when a new translation
     * result is created or when the loadChunksFromDb() on a document is called.
      * @param document A document the Translation Result is part of.
     */
    public void setDocument(USDocument document) {
        this.document = document;
        translationResult.setDocumentId(document.getDatabaseId());
    }

    /**
     * A private setter that sets the document ID. It is used by Hibernate only. Due the immutability of
     * the documentId in TimedChunk, this property is also immutable.
     * @param documentDatabaseId
     */
    private void setDocumentDatabaseId(long documentDatabaseId) {
        translationResult.setDocumentId(documentDatabaseId);
    }

    /**
     * Gets the ID of documents this translation result belongs. The value originates in the TimedChunk object.
     * @return A ID of the document this translation result is part of.
     */
    public long getDocumentDatabaseId() {
        return  translationResult.getDocumentId();
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

        Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();
        saveToDatabase(dbSession);
        usHibernateUtil.closeAndCommitSession(dbSession);
    }

    /**
     * A default constructor used by Hibernate.
     */
    private USTranslationResult() {
        translationResult = new TranslationResult();
    }

    /**
     * Gets the wrapped shared object.
     * @return Wrapped shared object.
     */
    public TranslationResult getTranslationResult() {
	    return translationResult;
	}

    /**
     * Gets the starting time of the chunk in the srt format.
     * @return Starting time of the chunk.
     */
    public String getStartTime() {
        return translationResult.getSourceChunk().getStartTime();
    }

    /**
     * Sets the starting time of the chunk in the srt format.
     * @param startTime Starting time of the chunk.
     */
    public void setStartTime(String startTime) {
        translationResult.getSourceChunk().setStartTime(startTime);
    }

    public String getEndTime() {
        return translationResult.getSourceChunk().getEndTime();
    }

    public void setEndTime(String endTime) {
        translationResult.getSourceChunk().setEndTime(endTime);
    }

    /**
     * Gets the original text of the chunk.
     * @return Original text of the chunk.
     */
    public String getText() {
        return translationResult.getSourceChunk().getSurfaceForm();
    }

    /**
     * Sets the original text of the chunk. It is used by Hibenrate only.
     * @param text Original text of the chunk.
     * @throws IllegalAccessException
     */
    public void setText(String text) {
        if (text == null)
            text = "";
        translationResult.getSourceChunk().setSurfaceForm(text);
    }

    /**
     * Gets the translation provided by the user.
     * @return The user tranlsation.
     */
    public String getUserTranslation() {
        return translationResult.getUserTranslation();
    }

    /**
     * Sets the user translation. It is used both at the runtime when a user changes the translation and
     * by Hibernate at the time the TranslationsResult is loaded from the database.
     * @param userTranslation
     */
    public void setUserTranslation(String userTranslation) {
        translationResult.setUserTranslation(userTranslation);
        feedbackSent = false;
        /*
         *  THIS CAN'T BE HERE- we can't save it to database when it is use by hibernate setter
         *  Hibernate will try to save it to database while we are loading it from database
         *  
         *  leading into needles saving to DB and more importantly - bugs, because it is in weird state
         *
        Session dbSession = HibernateUtil.getSessionWithActiveTransaction();
        saveToDatabase(dbSession);
        HibernateUtil.closeAndCommitSession(dbSession);
        */
    }

    /**
     * Gets the order of the part of the original subtitle chunk which is this translation result part of.
     * (The original subtitle chunks -- the amount of text which is displayed on the screen at one moment
     * is split on the sentences boundaries.)
     * @return The order of the part of the original subtitle chunk
     */
    public int getPartNumber() {
        return translationResult.getSourceChunk().getPartNumber();
    }

    /**
     * the order of the part of the original subtitle chunk which is this translation result part of.
     * It is used by Hibernate only.
     * @param partNumber The order of the part of the original subtitle chunk
     */
    private void setPartNumber(int partNumber) {
        translationResult.getSourceChunk().setPartNumber(partNumber);
    }

    /**
     * Gets the index of translation pair user selected int the client in the wrapped object.
     * @return The index of selected translation pair.
     */
    public long getSelectedTranslationPairID() {
        return translationResult.getSelectedTranslationPairID();
    }

    /**
     * Sets the index of translation pair the user selected in the client in the wrapped object.
     * @param selectedTranslationPairID The index of the selected translation pair.
     */
    public void setSelectedTranslationPairID(long selectedTranslationPairID) {
        translationResult.setSelectedTranslationPairID(selectedTranslationPairID);

        /*
         *  THIS CAN'T BE HERE- we can't save it to database when it is use by hibernate setter
         *  Hibernate will try to save it to database while we are loading it from database
         *  
         *  leading into needles saving to DB and more importantly - bugs, because it is in weird state
         *
Session dbSession = HibernateUtil.getSessionWithActiveTransaction();
        saveToDatabase(dbSession);
        HibernateUtil.closeAndCommitSession(dbSession);
        */
    }

    /**
     * Gets the chunk identifier which is unique within a document and is used for identifying the
     * chunks during the communication between the GUI and User Space. The getter of the wrapped
     * object is called.
     * @return The chunk identifier.
     */
    public int getSharedId() {
        return translationResult.getChunkId();
    }

    /**
     * Sets the chunk identifier which is unique within a document. The property is immutable.
     * Once the value is set, later attempts to reset the value throw an exception. The setter
     * of the wrapped object is called in this method.
     * @param sharedId A new value of the chunk identifier.
     * @exception UnsupportedOperationException The exception is thrown if the resetting the identifier
     *   is attempted.
     * */
    public void setSharedId(int sharedId) {
        if (sharedId < 0) {
                RuntimeException e = new RuntimeException("ShareID lesser than zero!");
                
                System.out.println("----error stacktrace---");

                StackTraceElement[] st = e.getStackTrace();
                for (StackTraceElement stackTraceElement : st) {
                    System.out.println(stackTraceElement.toString());
                }



            throw e;
        }
        translationResult.setChunkId(sharedId);
    }


    protected long getSharedClassDatabaseId() { return databaseId; }
    protected void setSharedClassDatabaseId(long setSharedDatabaseId) { }

    public ChunkIndex getChunkIndex() {
        return translationResult.getSourceChunk().getChunkIndex();
    }

    /**
     * Queries the Translation Memory for the suggestions. If there are some previous
     * suggestions they are discarded.
     * @param TM An instance of Tranlsation Memory from the core.
     */
    public void generateMTSuggestions(TranslationMemory TM) {
        if (TM == null) { return; }

        // TODO: ensure none of the potential previous suggestions is in the server cache collection
        // dereference of current suggestion will force hibernate to remove them from the db as well
        translationResult.setTmSuggestions(null);

        scala.collection.immutable.List<TranslationPair> TMResults =
                TM.nBest(translationResult.getSourceChunk(), document.getLanguage(), document.getMediaSource(), 25, false);
        // the retrieved Scala collection must be transformed to a Java collection
        // otherwise it cannot be iterated by the for loop
        List<TranslationPair> javaList = new ArrayList<TranslationPair>(
                scala.collection.JavaConverters.asJavaListConverter(TMResults).asJava());


        // store the collections as synchronized (to have a better feeling from this)
        translationResult.setTmSuggestions(javaList);
    }

    public void saveToDatabase(Session dbSession) {
        System.out.println("us: Chci ulozit s indexem "+getSharedId());
        saveJustObject(dbSession);
    }

    public void deleteFromDatabase(Session dbSession) {
        deleteJustObject(dbSession);
    }

    /**
     * A private getter of the sign if the Translation Result already provided
     * a feedback to the core. Used by Hibernate.
     * @return The sign value
     */
    private boolean isFeedbackSent() {
        return feedbackSent;
    }

    /**
     * A private setter for the sign if the Translation Result already provided
     * a feedback to the core. Used by Hibernate.
     * @param feedbackSent The sign value
     */
    private void setFeedbackSent(boolean feedbackSent) {
        this.feedbackSent = feedbackSent;
    }

    /**
     * Queries the database for a list of translation results which were not marked as checked
     * and mark them as checked. This is then interpreted as that a feedback for has been provided
     * and the chunks are ready to be deleted from the database.
     * @return  A list of unchecked translation results.
     */
    public static List<USTranslationResult> getUncheckedResults() {
         Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();


         List queryResult = dbSession.createQuery("select t from USTranslationResult t " +
                 "where t.feedbackSent = false").list();

         Map<Long, USDocument> involvedDocuments = new HashMap<Long, USDocument>();
         List<USTranslationResult> results = new ArrayList<USTranslationResult>();

         for (Object tr : queryResult) {
             USTranslationResult usResult = (USTranslationResult)tr;
             usResult.setFeedbackSent(true);

             if (!involvedDocuments.containsKey(usResult.getDocumentDatabaseId()))
             {
                 List documentResult = dbSession.createQuery("select d from USDocument d " +
                         "where d.databaseId = " +
                         usResult.getDocumentDatabaseId()).list();

                 if (documentResult.size() == 1) {
                     involvedDocuments.put(usResult.getDocumentDatabaseId(),
                             (USDocument)documentResult.get(0));
                 }
                 else {
                     throw new RuntimeException("Referencing to not-existing document.");
                 }

             }
             usResult.setDocument(involvedDocuments.get(usResult.getDocumentDatabaseId()));

             usResult.saveToDatabase(dbSession);
             results.add(usResult);
         }
         usHibernateUtil.closeAndCommitSession(dbSession);
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

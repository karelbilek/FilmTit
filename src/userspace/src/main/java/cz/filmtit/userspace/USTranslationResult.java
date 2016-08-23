/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/
package cz.filmtit.userspace;

import cz.filmtit.core.model.TranslationMemory;
import cz.filmtit.share.*;
import org.hibernate.Session;

import java.util.*;

/**
 * Represents a subtitle chunk together with its timing, selected translation
 * suggestions from the translation memory and also the user translation in the
 * User Space. It is a wrapper of the TranslationResult class from the share
 * namespace. Unlike the other User Space objects, the Translations Results
 * stays in the database even in cases the the document the Translation Result
 * belongs to is deleted. Unlike the client side copy of the wrapped object it
 * does not contain the suggestions itfself which are thrown away as soon they
 * are sent to the client.
 *
 * @author Jindřich Libovický
 */
public class USTranslationResult extends DatabaseObject implements Comparable<USTranslationResult> {

    /**
     * The shared object which is wrapped by the USTranslationResult.
     */
    private volatile TranslationResult translationResult;
    /**
     * A sign if the feedback to the core has been already provided.
     */
    private volatile boolean feedbackSent = false;
    /**
     * A reference to the document this translation result is part of.
     */
    private USDocument document;

    private volatile Long lockedByUser;

    /**
     * Sets the document the Translation Result belongs to. It is called either
     * when a new translation result is created or when the loadChunksFromDb()
     * on a document is called.
     *
     * @param document A document the Translation Result is part of.
     */
    public void setDocument(USDocument document) {
        this.document = document;
        translationResult.setDocumentId(document.getDatabaseId());
    }

    /**
     * A private setter that sets the document ID. It is used by Hibernate only.
     * Due the immutability of the documentId in TimedChunk, this property is
     * also immutable.
     *
     * @param documentDatabaseId
     */
    private void setDocumentDatabaseId(long documentDatabaseId) {
        translationResult.setDocumentId(documentDatabaseId);
    }

    /**
     * Gets the ID of documents this translation result belongs. The value
     * originates in the TimedChunk object.
     *
     * @return A ID of the document this translation result is part of.
     */
    public long getDocumentDatabaseId() {
        return translationResult.getDocumentId();
    }

    /**
     * Creates the Translation Result object from the Timed Chunk. It is
     * typically called when the User Space receives a TimedChunk from the
     * client. The object is immediately stored in the database, but the TM core
     * is not queried for the translation suggestions in the constructor. It
     * happens latter in a separate method.
     *
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
     * A public default constructor used by Hibernate.
     */
    public USTranslationResult() {
        translationResult = new TranslationResult();
    }

    /**
     * Gets the wrapped shared object.
     *
     * @return Wrapped shared object.
     */
    public TranslationResult getTranslationResult() {
        return translationResult;
    }

    /**
     * Gets the starting time of the chunk in the srt format.
     *
     * @return Starting time of the chunk.
     */
    public String getStartTime() {
        return translationResult.getSourceChunk().getStartTime();
    }

    /**
     * Sets the starting time of the chunk in the srt format. The format check
     * is not done here, but in the methods in class Session that can handle the
     * timing.
     *
     * @param startTime Starting time of the chunk.
     */
    public void setStartTime(String startTime) {
        translationResult.getSourceChunk().setStartTime(startTime);
    }

    /**
     * Gets the ending time of the chunk in the srt format.
     *
     * @return End time of the chunk.
     */
    public String getEndTime() {
        return translationResult.getSourceChunk().getEndTime();
    }

    /**
     * Sets the end time of the chunk in srt format. The format check is not
     * done here, but in the methods in class Session that can handle the
     * timing.
     *
     * @param endTime End tie of the chunk in srt format.
     */
    public void setEndTime(String endTime) {
        translationResult.getSourceChunk().setEndTime(endTime);
    }

    /**
     * Gets the original text of the chunk.
     *
     * @return Original text of the chunk.
     */
    public String getText() {
        return translationResult.getSourceChunk().getDatabaseForm();
    }

    /**
     * Sets the original text of the chunk. It is used by Hibernate only.
     *
     * @param text Original text of the chunk.
     * @throws IllegalAccessException
     */
    public void setText(String text) {
        if (text == null) {
            text = "";
        }
        translationResult.getSourceChunk().setDatabaseForm(text);
    }

    /**
     * Gets the translation provided by the user. It Accesses the wrapped
     * object.
     *
     * @return The user translation.
     */
    public String getUserTranslation() {
        return translationResult.getUserTranslation();
    }

    /**
     * Sets the user translation. It is used both at the runtime when a user
     * changes the translation and by Hibernate at the time the
     * TranslationsResult is loaded from the database. It access the wrapped
     * object.
     *
     * @param userTranslation
     */
    public void setUserTranslation(String userTranslation) {
        translationResult.setUserTranslation(userTranslation);
        feedbackSent = false;
    }

    /**
     * Gets the order of the part of the original subtitle chunk which is this
     * translation result part of. (The original subtitle chunks -- the amount
     * of text which is displayed on the screen at one moment is split on the
     * sentences boundaries.)
     *
     * @return The order of the part of the original subtitle chunk
     */
    public int getPartNumber() {
        return translationResult.getSourceChunk().getPartNumber();
    }

    /**
     * the order of the part of the original subtitle chunk which is this
     * translation result part of. It is used by Hibernate only.
     *
     * @param partNumber The order of the part of the original subtitle chunk
     */
    private void setPartNumber(int partNumber) {
        translationResult.getSourceChunk().setPartNumber(partNumber);
    }

    /**
     * Gets the index of translation pair user selected int the client in the
     * wrapped object.
     *
     * @return The index of selected translation pair.
     */
    public long getSelectedTranslationPairID() {
        return translationResult.getSelectedTranslationPairID();
    }

    /**
     * Sets the index of translation pair the user selected in the client in the
     * wrapped object.
     *
     * @param selectedTranslationPairID The index of the selected translation
     * pair.
     */
    public void setSelectedTranslationPairID(long selectedTranslationPairID) {
        translationResult.setSelectedTranslationPairID(selectedTranslationPairID);
    }

    /**
     * Gets the subtitle item identifier which is unique within a document and
     * is used for identifying the chunks during the communication between the
     * GUI and User Space. The getter of the wrapped object is called.
     *
     * @return The chunk identifier.
     */
    public int getSharedId() {
        return translationResult.getChunkId();
    }

    /**
     * Sets the subtitle item identifier which is unique within a document. The
     * property is immutable. Once the value is set, later attempts to reset the
     * value throw an exception. The setter of the wrapped object is called in
     * this method.
     *
     * @param sharedId A new value of the chunk identifier.
     * @exception UnsupportedOperationException The exception is thrown if the
     * resetting the identifier is attempted.
     *
     */
    public void setSharedId(int sharedId) {
        if (sharedId < 0) {
            RuntimeException e = new RuntimeException("ShareID lesser than zero!");
            logger.error("SharedID is lesser than zero!");
            throw e;
        }
        translationResult.setChunkId(sharedId);
    }

    /**
     * The database ID is not used in the wrapped object, so it gets just the
     * database ID. (Called by the getDatabaseId getter in DatabaseObject.)
     *
     * @return The database ID.
     */
    protected long getSharedClassDatabaseId() {
        return databaseId;
    }

    /**
     * The database ID is not used in the wrapped object, it does nothing.
     * (Called by the setDatabaseId setter in DatabaseObject.)
     *
     * @param setSharedDatabaseId Subtitle item ID
     */
    @Override
    protected void setSharedClassDatabaseId(long setSharedDatabaseId) {
        //logger.error("Setting translationResult.Id to " + setSharedDatabaseId);
        translationResult.setId(setSharedDatabaseId);
    }

    public ChunkIndex getChunkIndex() {
        return translationResult.getSourceChunk().getChunkIndex();
    }

    /**
     * Queries the Translation Memory for the suggestions. If there are some
     * previous suggestions they are discarded. The suggestion are stored in the
     * structure wrapped object the way as it is the client. Anyway, they are
     * discarded as soon as they are sent to the client.
     *
     * @param TM An instance of Tranlsation Memory from the core.
     */
    public synchronized void generateMTSuggestions(TranslationMemory TM) {
        if (TM == null) {
            return;
        }

        // TODO: ensure none of the potential previous suggestions is in the server cache collection
        // dereference of current suggestion will force hibernate to remove them from the db as well
        translationResult.setTmSuggestions(null);

        Set<TranslationSource> disabledSources = new HashSet<TranslationSource>();
        if (!document.getOwner().getUseMoses()) {
            disabledSources.add(TranslationSource.EXTERNAL_MT);
        }

        scala.collection.immutable.List<TranslationPair> TMResults
                = TM.nBest(translationResult.getSourceChunk(), document.getLanguage(), document.getMediaSource(),
                        document.getOwner().getMaximumNumberOfSuggestions(), false, disabledSources);
        // the retrieved Scala collection must be transformed to a Java collection
        // otherwise it cannot be iterated by the for loop
        List<TranslationPair> javaList = new ArrayList<TranslationPair>(
                scala.collection.JavaConverters.asJavaListConverter(TMResults).asJava());

        // store the collections as synchronized (to have a better feeling from this)
        translationResult.setTmSuggestions(javaList);
    }

    /**
     * Saves the object to database.
     *
     * @param dbSession An opened database session.
     */
    public void saveToDatabase(Session dbSession) {
        saveJustObject(dbSession);
    }

    /**
     * Deletes the object from database.
     *
     * @param dbSession An opened database session.
     */
    public void deleteFromDatabase(Session dbSession) {
        deleteJustObject(dbSession);
    }

    /**
     * A private getter of the sign if the Translation Result already provided a
     * feedback to the core. Used by Hibernate.
     *
     * @return The sign value
     */
    public boolean isFeedbackSent() {
        return feedbackSent;
    }

    /**
     * A private setter for the sign if the Translation Result already provided
     * a feedback to the core. Used by Hibernate.
     *
     * @param feedbackSent The sign value
     */
    private void setFeedbackSent(boolean feedbackSent) {
        this.feedbackSent = feedbackSent;
    }

    /**
     * Queries the database for a list of translation results which were not
     * marked as checked and mark them as checked. This is then interpreted as
     * that a feedback for has been provided and the chunks are ready to be
     * deleted from the database. If the unchecked translation results are part
     * a document that has been flagged as to be deleted, teh translation
     * results are deleted as well.
     *
     * @return A list of unchecked translation results.
     */
    public synchronized static List<USTranslationResult> getUncheckedResults() {
        Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();

        List queryResult = dbSession.createQuery("select t from USTranslationResult t "
                + "where t.feedbackSent = false").list();

        Map<Long, USDocument> involvedDocuments = new HashMap<Long, USDocument>();
        List<USTranslationResult> results = new ArrayList<USTranslationResult>();

        for (Object tr : queryResult) {
            USTranslationResult usResult = (USTranslationResult) tr;
            usResult.setFeedbackSent(true);

            // if we haven't met the document before load it form the db
            if (!involvedDocuments.containsKey(usResult.getDocumentDatabaseId())) {
                List documentResult = dbSession.createQuery("select d from USDocument d "
                        + "where d.databaseId = :did").setParameter("did", usResult.getDocumentDatabaseId()).list();

                if (documentResult.size() == 1) {
                    involvedDocuments.put(usResult.getDocumentDatabaseId(),
                            (USDocument) documentResult.get(0));
                } else {
                    throw new RuntimeException("Referencing to not-existing document.");
                }
            }
            USDocument resultsDocument = involvedDocuments.get(usResult.getDocumentDatabaseId());
            usResult.setDocument(resultsDocument);

            if (resultsDocument.isToBeDeleted()) { // delete the result if it's from document to be deleted
                usResult.deleteFromDatabase(dbSession);
            } else { // otherwise just save the sign of having provided feedback
                usResult.saveToDatabase(dbSession);
            }

            results.add(usResult);
        }
        usHibernateUtil.closeAndCommitSession(dbSession);
        return results;
    }

    /**
     * Compares the object with a different one based on the start time of the
     * chunks. In fact only the compareTo method of the wrapped
     * TranslationResult object is called.
     *
     * @param other A Translation Result which is compared to this one
     * @return Result of comparison of the translation result with other chunk.
     */
    @Override
    public int compareTo(USTranslationResult other) {
        return translationResult.compareTo(other.getTranslationResult());
    }

    public synchronized TranslationResult getResultCloneAndRemoveSuggestions() {
        TranslationResult withSuggestions = translationResult;
        translationResult = translationResult.resultWithoutSuggestions();
        return withSuggestions;
    }

    @Override
    public String toString() {
        return getDatabaseId() + "#" + getTranslationResult().toString();
    }

    /**
     * Sets the chunk to be active or non-active, it means if it is currently
     * displayed in a translation workspace and translation suggestion should
     * generated for it. If it is set to false, the translation memory core
     * stops to generate the suggestion.
     *
     * @param active Flag if the translation results is worth of generating
     * suggestions.
     */
    public void setChunkActive(boolean active) {
        translationResult.getSourceChunk().isActive = active;
    }

    /**
     * @return the lockedByUser
     */
    public Long getLockedByUser() {
        return lockedByUser;
    }

    /**
     * @param lockedByUser the lockedByUser to set
     */
    public void setLockedByUser(Long lockedByUser) {
        this.lockedByUser = lockedByUser;
    }
}

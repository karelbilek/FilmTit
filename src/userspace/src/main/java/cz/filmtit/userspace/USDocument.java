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

import cz.filmtit.share.ChunkIndex;
import cz.filmtit.share.Document;
import cz.filmtit.share.Language;
import cz.filmtit.share.MediaSource;
import org.hibernate.Session;

import java.util.*;

/**
 * Represents a subtitle file the user work with. It is a wrapper of the shared
 * Document class. Some of the properties of the class are used keep the wrapped
 * document properties persistent by the database mapping. There are also
 * further properties which are used exclusively by the User Space.
 *
 * @author Jindřich Libovický
 */
public class USDocument extends DatabaseObject {

    /**
     * ID of the owner of the document. (It is only here, not in the shared
     * object.)
     */
    private volatile long ownerDatabaseId = 0;
    /**
     * Reference to the actual instance of the owner of the document.
     */
    private volatile USUser owner = null;
    /**
     * The wrapped shared document object.
     */
    private volatile Document document;

    /**
     * A map of translation results this documents consists of. It is insured in
     * the code it is always a synchronized map. The collection is mirrored in
     * the wrapped documents by the objects of shared translation results
     * classes.
     */
    private SortedMap<ChunkIndex, USTranslationResult> translationResults;

    /**
     * Flag is the the user deleted the document. It is kept in the database
     * until the feedback is provided to the core, but is not displayed to the
     * user.
     */
    private volatile boolean toBeDeleted = false;

    /**
     * List of users who can access this docunment
     */
    private volatile List<DocumentUsers> documentUsers;
    
    private volatile Long shareId;

    /**
     * A constructor used when a new document is created. It wraps the shared
     * document sent by the client (at that time the media source is not set)
     * and saves the new document immediately to the database
     *
     * @param document Shared document object.
     * @param user Owner of the document.
     */
    public USDocument(Document document, USUser user, List<DocumentUsers> documentUsers) {
        this.document = document;
        document.setLastChange(new Date().getTime()); // current time, not be 1970 by default
        translationResults = Collections.synchronizedSortedMap(new TreeMap<ChunkIndex, USTranslationResult>());
        owner = user;

        this.ownerDatabaseId = user.getDatabaseId();
        this.documentUsers = documentUsers;

        // save it to the database right away
        Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();
        saveToDatabase(dbSession);
        usHibernateUtil.closeAndCommitSession(dbSession);
    }

    /**
     * A private default constructor (for Hibernate).
     */
    public USDocument() {
        document = new Document();
        translationResults = Collections.synchronizedSortedMap(new TreeMap<ChunkIndex, USTranslationResult>());
        ownerDatabaseId = 0;
    }

    /**
     * Gets the path of the video on the user's local machine which was used
     * last time the client run. It is private because it is used by Hibernate
     * only.
     *
     * @return
     */
    private String getLocalMoviePath() {
        return document.getMoviePath();
    }

    /**
     * Sets the path to the video on the user's local machine. It is private
     * beacause it is use by Hibernate only.
     *
     * @param moviePath Local path to a video file.
     */
    private void setLocalMoviePath(String moviePath) {
        document.setMoviePath(moviePath);
    }

    /**
     * Gets the database id of the owner of the document. It is private and used
     * by Hibernate only.
     *
     * @return
     */
    private long getOwnerDatabaseId() {
        return ownerDatabaseId;
    }

    /**
     * Sets the database id of user owning the document. It is private and used
     * by Hibernate only.
     *
     * @param ownerDatabaseId
     * @throws Exception
     */
    private void setOwnerDatabaseId(long ownerDatabaseId) {
        if (this.ownerDatabaseId == ownerDatabaseId) {
            return;
        }
        if (this.ownerDatabaseId != 0) {
            throw new RuntimeException("Owner database ID should not be reset. User " + this.ownerDatabaseId);
        }
        this.ownerDatabaseId = ownerDatabaseId;
    }

    /**
     * Gets the wrapped shared document.
     *
     * @return A shared document object.
     */
    public Document getDocument() {
        return document;
    }

    /**
     * Gets the source language of the document as the language object.
     *
     * @return Language of the document.
     */
    public Language getLanguage() {
        return document.getLanguage();
    }

    /**
     * Gets the source language code (string) of the document.
     *
     * @return Language code of the document.
     */
    public String getLanguageCode() {
        return document.getLanguage().getCode();
    }

    /**
     * Sets the source language of the document. Only the language code is
     * passed to the method, corresponding language object is created in the
     * wrapped document object.
     *
     * @param languageCode Language code of the source language of the document.
     */
    public void setLanguageCode(String languageCode) {
        document.setLanguageCode(languageCode);
    }

    /**
     * Gets the media source object representing the movie or TV show of the
     * document.
     *
     * @return Media source of the document.
     */
    public MediaSource getMediaSource() {
        return document.getMovie();
    }

    /**
     * Sets the media source of the document. It is private because it is used
     * by Hibernate only. There is many-to-one mapping used, so there is only a
     * foreign key in the database.
     *
     * @param movie
     */
    public void setMediaSource(MediaSource movie) {
        document.setMovie(movie);
    }

    /**
     * Supplies the id value from the wrapped object to the getDatabaseId getter
     * of the parent DatabaseObject.
     *
     * @return
     */
    protected long getSharedClassDatabaseId() {
        return document.getId();
    }

    /**
     * Propagates setting the database ID from the setDatabaseId setter to the
     * wrapped object.
     *
     * @param databaseId Database ID.
     */
    protected void setSharedClassDatabaseId(long databaseId) {
        document.setId(databaseId);
    }

    /**
     * Gets the title of the document (originates in the wrapped shared object).
     *
     * @return The title of the document.
     */
    public String getTitle() {
        return document.getTitle();
    }

    /**
     * Sets the title of the document (calls the wrapped shared object).
     *
     * @param title
     */
    public void setTitle(String title) {
        document.setTitle(title);
    }

    /**
     * Gets the set of indexes (unique within a document) of the translation
     * results.
     *
     * @return Set of indexes of translation results.
     */
    public Set<ChunkIndex> getTranslationResultKeys() {
        if (translationResults == null) {
            return null;
        }
        return translationResults.keySet();
    }

    /**
     * Gets the collection of translation results the document consists of.
     *
     * @return Documents translation results.
     */
    public Collection<USTranslationResult> getTranslationResultValues() {
        if (translationResults == null) {
            return null;
        }
        return translationResults.values();
    }

    /**
     * Removes translation result of given index.
     *
     * @param index Index of translation result to be deleted.
     */
    public void removeTranslationResult(ChunkIndex index) {
        translationResults.remove(index);
        document.setTranslatedChunksCount(translationResults.size());
    }

    /**
     * Gets translation result of given index.
     *
     * @param i Index of required translation result.
     * @return Translation result of index i.
     */
    public USTranslationResult getTranslationResultForIndex(ChunkIndex i) {
        return translationResults.get(i);
    }

    /**
     * Gets the time of the last change of the document (from the wrapped
     * document).
     *
     * @return Time of the document last change.
     */
    private long getLastChange() {
        return document.getLastChange();
    }

    /**
     * Sets the time of change of the document.
     *
     * @param lastChange Time of last change of the document.
     */
    public void setLastChange(long lastChange) {
        document.setLastChange(lastChange);
    }

    /**
     * Gets the total number of chunks in the document. (It is in a separate
     * variable in order to be get the number even if the translation results
     * are not loaded from the database.)
     *
     * @return
     */
    private int getTotalChunksCount() {
        return document.getTotalChunksCount();
    }

    /**
     * Sets the total number of chunks (calls the wrapped shared object). It is
     * private and used by Hibernate only.
     *
     * @param totalChunksCount
     */
    private void setTotalChunksCount(int totalChunksCount) {
        document.setTotalChunksCount(totalChunksCount);
    }

    /**
     * Gets the number of translation results where the user translation has
     * been already set. (Calls the wrapped share object.)
     *
     * @return Number of already translated chunks.
     */
    public int getTranslatedChunksCount() {
        return document.getTranslatedChunksCount();
    }

    /**
     * Sets the count of already translated chunks. It is called both from
     * Hibernate and from the setUserTranslation method in the Session class
     * which is called when the user sets or changes the translation.
     *
     * @param translatedChunksCount
     */
    public void setTranslatedChunksCount(int translatedChunksCount) {
        document.setTranslatedChunksCount(translatedChunksCount);
    }

    /**
     * Gets the flag if a user marked the document to be deleted from the
     * database
     *
     * @return Flag is the document should be deleted.
     */
    public boolean isToBeDeleted() {
        return toBeDeleted;
    }

    /**
     * Sets the flag if the document should be deleted.
     *
     * @param toBeDeleted Flag if the document can be deleted from the database.
     */
    public void setToBeDeleted(boolean toBeDeleted) {
        this.toBeDeleted = toBeDeleted;
    }

    /**
     * Gets the owner of the document.
     *
     * @return User that owns the docuemtn.
     */
    public USUser getOwner() {
        return owner;
    }

    /**
     * Sets the owner of the document. Used while loading the user.
     *
     * @param owner User who is owner of the document.
     */
    public void setOwner(USUser owner) {
        // it sets the database id first, because it throws an exception if the owner is changed.
        setOwnerDatabaseId(owner.getDatabaseId());
        this.owner = owner;
    }

    /**
     * Loads the translation results from the database.
     */
    public synchronized void loadChunksFromDb() {
        org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();

        // query the database for the translationResults
        List foundChunks = dbSession.createQuery("select c from USTranslationResult c where c.documentDatabaseId = :d")
                .setParameter("d", databaseId).list();

        translationResults = Collections.synchronizedSortedMap(new TreeMap<ChunkIndex, USTranslationResult>());
        for (Object o : foundChunks) {
            USTranslationResult result = (USTranslationResult) o;
            result.setDocument(this);
            if (result == null) {
                throw new RuntimeException("Someone is trying to put null translationResult!");
            }
            translationResults.put(result.getTranslationResult().getSourceChunk().getChunkIndex(), result);
        }

        usHibernateUtil.closeAndCommitSession(dbSession);

        // add the translation results to the inner document
        for (Map.Entry<ChunkIndex, USTranslationResult> usResult : translationResults.entrySet()) {
            document.getTranslationResults().put(usResult.getKey(), usResult.getValue().getTranslationResult());
        }
        document.setTotalChunksCount(translationResults.size());
    }

    /**
     * Saves the document to the database including the Translation Results in
     * the case they were loaded or created.
     *
     * @param dbSession Opened database session.
     */
    public synchronized void saveToDatabase(Session dbSession) {
        saveJustObject(dbSession);

        if (translationResults != null) {
            for (USTranslationResult translationResult : translationResults.values()) {
                translationResult.saveToDatabase(dbSession);
            }
        }
    }

    /**
     * Saves the document to the databse without the the translation results.
     *
     * @param dbSession An opened database session.
     */
    public void saveToDatabaseJustDocument(Session dbSession) {
        if (document.getMovie() != null) {
            dbSession.saveOrUpdate(document.getMovie());
        }
        saveJustObject(dbSession);
    }

    /**
     * Deletes the document from database.
     *
     * @param dbSession
     */
    public synchronized void deleteFromDatabase(Session dbSession) {
        deleteJustObject(dbSession);
    }

    /**
     * Adds the translation result to the document, replacing a possible
     * translation result with the same ChunkIndex.
     *
     * @param translationResult
     */
    public void addTranslationResult(USTranslationResult translationResult) {
        addOrReplaceTranslationResult(translationResult);
        document.setTotalChunksCount(translationResults.size());
    }

    /**
     * Puts the translation result into the list of translation results of the
     * document (both the USDocument and the Document). The translation result
     * is identified by the inner ChunkIndex; if a translation result with the
     * same ChunkIndex already exists, it is replaced by this new one.
     *
     * @param usTranslationResult
     */
    public synchronized void addOrReplaceTranslationResult(USTranslationResult usTranslationResult) {
        ChunkIndex chunkIndex = usTranslationResult.getTranslationResult().getSourceChunk().getChunkIndex();
        translationResults.put(chunkIndex, usTranslationResult);
        document.getTranslationResults().put(chunkIndex, usTranslationResult.getTranslationResult());
        document.setTotalChunksCount(translationResults.size());
    }

    /**
     * @return the documentUsers
     */
    public List<DocumentUsers> getDocumentUsers() {
        return documentUsers;
    }

    /**
     * @param documentUsers the documentUsers to set
     */
    public void setDocumentUsers(List<DocumentUsers> documentUsers) {
        this.documentUsers = documentUsers;
    }

    /**
     * @return the shareId
     */
    public Long getShareId() {
        return shareId;
    }

    /**
     * @param shareId the shareId to set
     */
    public void setShareId(Long shareId) {
        if (this.shareId == shareId) {
            return;
        }
        this.shareId = shareId;
    }


}

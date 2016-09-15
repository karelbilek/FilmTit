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

import cz.filmtit.core.model.MediaSourceFactory;
import cz.filmtit.core.model.TranslationMemory;
import cz.filmtit.share.*;
import cz.filmtit.share.exceptions.AlreadyLockedException;
import cz.filmtit.share.exceptions.InvalidChunkIdException;
import cz.filmtit.share.exceptions.InvalidDocumentIdException;
import cz.filmtit.share.exceptions.InvalidValueException;
import cz.filmtit.userspace.servlets.FilmTitBackendServer;
import cz.filmtit.userspace.servlets.FilmTitBackendServer.CheckUserEnum;

import org.jboss.logging.Logger;

import java.util.*;
import java.util.regex.Pattern;
import org.hibernate.Query;

/**
 * Represents a running session.
 *
 * @author Jindřich Libovický
 */
public class Session {

    /**
     * Database ID of the session. It is set when the log about the session is
     * saved to database. It means that all the time the session exists has
     * value of Long.MIN_VALUE.
     */
    private long databaseId = Long.MIN_VALUE;
    /**
     * Reference to the user who owns the session.
     */
    private USUser user;

    /**
     * Time when the session was created. It is stored in the log.
     */
    private volatile long sessionStart;
    /**
     * Time of last operation with the session. It is stored in the log.
     */
    private volatile long lastOperationTime;
    /**
     * Current state of the session. It is stored in the log.
     */
    private volatile SessionState state;

    /**
     * JBoss logger.
     */
    Logger logger = Logger.getLogger("Session");

    /**
     * Regular expression used for checking if the timing has the right format.
     */
    private static final Pattern timingRegexp = Pattern.compile("^[0-9]{2}:[0-9]{2}:[0-9]{2},[0-9]{3}$");
    /**
     * Instance of the singleton class for managing database sessions.
     */
    private static USHibernateUtil usHibernateUtil = USHibernateUtil.getInstance();

    public synchronized Void lockTranslationResult(TranslationResult tResult) throws AlreadyLockedException {
        org.hibernate.Session session = usHibernateUtil.getSessionWithActiveTransaction();

        Query query = session.createQuery("FROM USTranslationResult t WHERE t.documentDatabaseId = :did AND t.sharedId = :sid AND t.partNumber = :pid");
        query.setParameter("did", tResult.getDocumentId());
        query.setParameter("sid", tResult.getChunkId());
        query.setParameter("pid", tResult.getSourceChunk().getChunkIndex().getPartNumber());

        USTranslationResult translationResult = (USTranslationResult) query.list().get(0);

        if ((translationResult.getLockedByUser() != null) && (translationResult.getLockedByUser() != this.getUserDatabaseId()) && (translationResult.getLockedByUser() != 0)) {
            throw new AlreadyLockedException(String.valueOf(translationResult.getLockedByUser() + " " + this.getUserDatabaseId()));
        } else {
            translationResult.setLockedByUser(this.getUserDatabaseId());
        }

        session.saveOrUpdate(translationResult);
        usHibernateUtil.closeAndCommitSession(session);

        return null;

    }

    public synchronized Void unlockTranslationResult(ChunkIndex chunkIndex, Long documentId) {
        org.hibernate.Session session = usHibernateUtil.getSessionWithActiveTransaction();

        Query query = session.createQuery("FROM USTranslationResult t WHERE t.documentDatabaseId = :did AND t.sharedId = :sid AND t.partNumber = :pid");
        query.setParameter("did", documentId);
        query.setParameter("sid", chunkIndex.getId());
        query.setParameter("pid", chunkIndex.getPartNumber());

        List list = query.list();

        USTranslationResult translationResult = (USTranslationResult) list.get(0);

        if (translationResult.getLockedByUser() == this.getUserDatabaseId()) {
            translationResult.setLockedByUser(null);
        }

        session.saveOrUpdate(translationResult);
        usHibernateUtil.closeAndCommitSession(session);

        return null;
    }

    /**
     * Type for describing the stae of the session. <b>active</b> means the
     * session is running, <b>loggedOut</b>
     * means the user deliberately logged out from the application,
     * <b>terminated</b> means the session was ended when the user logged again
     * to the application despite he has a previously opened session.
     */
    enum SessionState {
        active, loggedOut, terminated, killed
    }

    /**
     * Cache hash table for active documents owned by current user, to make them
     * quickly available using their IDs. Being active means that there are
     * translation results loaded from the database. The rest of the users
     * document is available via user.getOwnedDocuments(). The preferable way
     * for making a document active is to call getActiveDocument(databaseId)
     * method in the session.
     */
    private Map<Long, USDocument> activeDocuments;

    /**
     * Creates a session owned by given user.
     *
     * @param user Owner of the session.
     */
    public Session(USUser user) {
        activeDocuments = Collections.synchronizedMap(new HashMap<Long, USDocument>());
        //lockedTranslationResults = new ArrayList<TranslationResult>();

        sessionStart = new Date().getTime();
        updateLastOperationTime();
        state = SessionState.active;
        this.user = user;
    }

    /**
     * Gets the time of the last operation with this session. It is usud
     *
     * @return Time of last operation of the session.
     */
    public long getLastOperationTime() {
        return lastOperationTime;
    }

    /**
     * Setter of last operation time, method required by Hibernate. Anyway, it
     * is never used because the session is never loaded from the databse.
     *
     * @param time Arbitrary number.
     */
    private void setLastOperationTime(long time) {
    }

    /**
     * Gets the time when the session started. Used by Hibernate only.
     *
     * @return The time when the session started.
     */
    private long getSessionStart() {
        return sessionStart;
    }

    /**
     * Setter of starting time of the session, method required by Hibernate.
     * Anyway, it is never used because the session is never loaded from the
     * databse.
     *
     * @param time Arbitrary number.
     */
    private void setSessionStart(long time) {
    }

    /**
     * Gets the user owning the session.
     *
     * @return User owning the session.
     */
    public USUser getUser() {
        return user;
    }

    /**
     * Sets the user owning the session.
     *
     * @return User owning the session.
     *
     */
    public void setUser(USUser user) {
        this.user = user;
    }

    /**
     * Gets the database ID of user owning the session.
     *
     * @return Database ID of user owning the session.
     */
    public long getUserDatabaseId() {
        return user.getDatabaseId();
    }

    /**
     * Setter of user database ID, method required by Hibernate. Anyway, it is
     * never used because the session is never loaded from the database.
     *
     * @param id Arbitrary number.
     */
    private void setUserDatabaseId(long id) {
    }

    /**
     * Gets the database if of the session. It used by Hibernate only at the
     * time the log about the session is saved.
     *
     * @return Database ID of the session.
     */
    private long getDatabaseId() {
        return databaseId;
    }

    /**
     * Sets the database ID fo the session if it has not been set before. Used
     * by Hibernate only at the time the log about the session is saved.
     *
     * @param databaseId
     */
    private void setDatabaseId(long databaseId) {
        if (this.databaseId == databaseId) {
            return;
        }
        if (this.databaseId == Long.MIN_VALUE) {
            this.databaseId = databaseId;
            return;
        }
        throw new UnsupportedOperationException("Once the database ID is set, it can't be changed.");
    }

    /**
     * Gets the state of the session as a string. It is used by Hibernate only
     * when the log about the session is saved.
     *
     * @return The sate of the session as a string.
     */
    private String getStateString() {
        return state.toString();
    }

    /**
     * Setter of the state of the session, method required by Hibernate. Anyway,
     * it is never used because the session is never loaded from the database.
     *
     * @param stateString Arbitrary string.
     */
    private void setStateString(String stateString) {
    }

    /**
     * Gets the flag if the session is permanent. (Calls the user owning the
     * session for his settings.)
     *
     * @return The flag is the session is permanent.
     */
    public boolean isPermanent() {
        return user.isPermanentlyLoggedId();
    }

    /**
     * Logs out the user and terminates the session.
     */
    public void logout() {
        state = SessionState.loggedOut;
        terminate();
    }

    /**
     * Terminates the session. It is called when the same user logs in for the
     * second time.
     */
    public void terminateOnNewLogin() {
        state = SessionState.terminated;
        logger.info("Previous session of " + user.getUserName() + "was terminated before creating a new one.");
        terminate();
    }

    /**
     * Terminates the session. Used in all session terminating situations (i.e.
     * logout, time-out, re-login). Saves the log about the session to the
     * database.
     */
    private synchronized void terminate() {
        // the session was already terminated and is in database => skip this method
        if (databaseId != Long.MIN_VALUE) {
            return;
        }

        org.hibernate.Session session = usHibernateUtil.getSessionWithActiveTransaction();

        Query query = session.createQuery("FROM USTranslationResult t WHERE t.lockedByUser = :user");
        query.setParameter("user", this.getUserDatabaseId());

        List list = query.list();

        for (Object o : list) {
            USTranslationResult translationResult = (USTranslationResult) o;
            translationResult.setLockedByUser(null);
            session.save(translationResult);
        }

        session.save(this);
        usHibernateUtil.closeAndCommitSession(session);
    }

    /**
     * Kills the session when it times out.
     */
    public void kill() {
        state = SessionState.killed;
        terminate();
    }

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    // HANDLING USERS
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    // permanently logged in, email, maximumNumberOfSuggestions, useMoses
    /**
     * Sets the flag if the user owning the session is permanently logged in or
     * not. (Called directly from an RPC call.)
     *
     * @param permanentlyLoggedIn Flag if the user owning the session is
     * permanently logged in
     * @return Void
     */
    public Void setPermanentlyLoggedIn(boolean permanentlyLoggedIn) {
        user.setPermanentlyLoggedId(permanentlyLoggedIn);
        return null;
    }

    /**
     * Changes the email of the owner of the session. (Called directly from an
     * RPC call.)
     *
     * @param email New user's email.
     * @return Void
     * @throws InvalidValueException Throws an exception if an invalid email
     * address is provided.
     */
    public Void setEmail(String email) throws InvalidValueException {

        Emailer.validateEmail(email);

        user.setEmail(email);
        saveUser();
        return null;
    }

    /**
     * Changes the maximum number of translation suggestions provided to the
     * user. (Called directly from an RPC call.)
     *
     * @param number New maximum number of suggestions, a number < 0 and <= 100.
     * @
     * return Void
     * @throws InvalidValueException Throws an exception if the number is not
     * between 1 and 100.
     */
    public Void setMaximumNumberOfSuggestions(int number) throws InvalidValueException {
        if (number < 0 || number > 100) {
            throw new InvalidValueException("The maximum number of suggestion must be a positive integer lesser than 100; '" + number + "' is incorrect.");
        }
        user.setMaximumNumberOfSuggestions(number);
        saveUser();
        return null;
    }

    /**
     * Sets the flag is the user wants to include the machine translation by
     * moses in the tranlsation suggestion. (Called directly from an RPC call.)
     *
     * @param useMoses Flag if the Moses should be used.
     * @return Void
     */
    public Void setUseMoses(boolean useMoses) {
        user.setUseMoses(useMoses);
        saveUser();
        return null;
    }

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    // HANDLING DOCUMENTS
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    /**
     * Creates a new document of given properties and sends back a new shared
     * Document object representing the document and a list of media sources
     * that matches the movie title provided by the user. (Called directly from
     * an RPC call.)
     *
     * @param documentTitle Title of the document as it should displayed to the
     * user.
     * @param movieTitle A movie title filled in by the user
     * @param language Source language
     * @param mediaSourceFactory A searcher for the media sources provided by
     * the FilmTitBackendServer
     * @param moviePath Path the to movie vide on the user's machine.
     * @return An object wrapping a shared document object of given
     */
    public DocumentResponse createNewDocument(String documentTitle, String movieTitle, String language, MediaSourceFactory mediaSourceFactory, String moviePath) {
        updateLastOperationTime();

        List<DocumentUsers> documentUsers = new ArrayList<DocumentUsers>();

        //  usDocument.getDocument().setDocumentUsers(documentUsers);
        USDocument usDocument = new USDocument(new Document(documentTitle, language, moviePath), user, documentUsers);
        List<MediaSource> suggestions = mediaSourceFactory.getSuggestions(movieTitle);

        activeDocuments.put(usDocument.getDatabaseId(), usDocument);

        user.addDocument(usDocument);
        logger.info("User " + user.getUserName() + " opened document " + usDocument.getDatabaseId() + " ("
                + usDocument.getTitle() + ").");
        return new DocumentResponse(usDocument.getDocument(), suggestions);
    }

    /**
     * Marks given document as deleted. The document is removed from the
     * collection of owned and active documents immediately. After that a thread
     * is run that deletes chunks that already provided feedback. It is done in
     * a separate thread not to delay the response for the RPC call. (Called
     * directly from an RPC call.)
     *
     * @param documentId ID of document to be deleted.
     * @return Void
     * @throws InvalidDocumentIdException It throws an exception if the document
     * with such ID is not owned by the user.
     */
    public Void deleteDocument(long documentId) throws InvalidDocumentIdException {
        USDocument document = getActiveDocument(documentId);

        activeDocuments.remove(documentId);
        user.getOwnedDocuments().remove(documentId);
        document.setToBeDeleted(true);

        // take care of the database and the translation results in separate thread not to delay the RPC response
        new DeleteDocumentRunner(document).run();
        return null;
    }

    /**
     * A thread that performs the additional operation after the document
     * deletion. It deletes the document from the database all the document's
     * translation results that has already provided feedback to the core and if
     * they are no translation results left it it deletes the
     */
    private class DeleteDocumentRunner extends Thread {

        /**
         * A document that has been signed as to be deleted.
         */
        USDocument document;

        /**
         * Creates the delete document runner for the particular document.
         *
         * @param document A document that has been signed as to be deleted.
         */
        public DeleteDocumentRunner(USDocument document) {
            this.document = document;
        }

        /**
         * Goes through all the chunks the to-be-deleted document has a deletes
         * all of them that already provided feedback. If there are no chunks
         * left, the document is deleted as well.
         */
        @Override
        public void run() {
            // if it wasn't an active document, translation results need to be loaded
            if (document.getTranslationResultValues() == null) {
                document.loadChunksFromDb();
            }

            org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();
            // delete all document chunks that already provided feedback to the core
            boolean deleteDocument = true;
            if (document.getTranslationResultValues() != null) {
                // check each result ...
                for (USTranslationResult result : document.getTranslationResultValues()) {
                    if (result.isFeedbackSent()) { // feedback provided => delete from db
                        result.deleteFromDatabase(dbSession);
                    } else { // there's a result that hasn't provided => don't delete the document
                        deleteDocument = false;
                    }
                }
            }

            if (deleteDocument) {
                document.deleteFromDatabase(dbSession);
            }
            usHibernateUtil.closeAndCommitSession(dbSession);
        }
    }

    /**
     * Selects the media source of the document according to the users
     * selection. If such media source is already in the database, it is update
     * to have the latest data, otherwise a new media source is stored to the
     * database. (Called directly from an RPC call.)
     *
     * @param documentId ID of the document the ID selected for
     * @param selectedMediaSource Selected media source
     * @return Void
     * @throws InvalidDocumentIdException It throws an exception if the document
     * with such ID is not owned by the user.
     */
    public Void selectSource(long documentId, MediaSource selectedMediaSource)
            throws InvalidDocumentIdException {
        updateLastOperationTime();

        USDocument document = getActiveDocument(documentId);

        org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();

        List sourcesFromDb = dbSession.createQuery("select m from MediaSource m where m.title like :title and m.year like :year").
                setParameter("title", selectedMediaSource.getTitle()).setParameter("year", selectedMediaSource.getYear()).list();

        if (sourcesFromDb.size() == 0) {
            try {
                dbSession.save(selectedMediaSource);
            } catch (Exception e) {
            }
            document.setMediaSource(selectedMediaSource);
        } else {
            // if the media source is already in db, update it to the latest freebase version
            MediaSource fromDb = (MediaSource) sourcesFromDb.get(0);
            fromDb.setGenres(selectedMediaSource.getGenres());
            fromDb.setThumbnailURL(selectedMediaSource.getThumbnailURL());
            dbSession.update(fromDb);

            document.setMediaSource(fromDb);
        }

        document.saveToDatabase(dbSession);
        usHibernateUtil.closeAndCommitSession(dbSession);

        return null;
    }

    /**
     * Gets the list of document the session user owns. In fact surface clones
     * of the documents not having having the translation results is sent.
     * (Called directly from an RPC call.)
     *
     * @return List of documents owned by the user.
     */
    public List<Document> getListOfDocuments() {
        updateLastOperationTime();
        List<Document> result = new ArrayList<Document>();

        for (USDocument usDocument : user.getOwnedDocuments().values()) {
            result.add(usDocument.getDocument().documentWithoutResults());
        }

        for (USDocument accessibleDocument : user.getAccessibleDocuments()) {
            result.add(accessibleDocument.getDocument().documentWithoutResults());
        }

        Collections.sort(result);
        return result;
    }

    /**
     * Loads the document's translation results and place the document among the
     * active documents and sends back a shared document object with the loaded
     * translation results. (Called directly from an RPC call.)
     *
     * @param documentID ID of document to be loaded.
     * @return The loaded document with translation results.
     * @throws InvalidDocumentIdException It throws an exception if the document
     * with such ID is not owned by the user.
     */
    public Document loadDocument(long documentID) throws InvalidDocumentIdException {
        updateLastOperationTime();
        return getActiveDocument(documentID).getDocument();
    }

    /**
     * Changes the title of document (this is user's document title which is not
     * connected to the media source) and saves it immediately to the database.
     * (Called directly from an RPC call.)
     *
     * @param documentId ID of the document
     * @param newTitle New title of the document.
     * @return Void
     * @throws InvalidDocumentIdException It throws an exception if the document
     * with such ID is not owned by the user.
     */
    public Void changeDocumentTitle(long documentId, String newTitle) throws InvalidDocumentIdException {
        updateLastOperationTime();

        USDocument document = getActiveDocument(documentId);

        org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();
        document.setTitle(newTitle);
        document.saveToDatabase(dbSession);
        usHibernateUtil.closeAndCommitSession(dbSession);

        return null;
    }

    /**
     * Starts the change of the media source. I takes a new movie title sent by
     * the user and sends back a list of suggestion what movies / TV shows the
     * user may have meant. Then the client calls again the selectSource method.
     * (Called directly from an RPC call.)
     *
     * @param documentId ID of the document
     * @param newMovieTitle Movie title suggested by the user
     * @param mediaSourceFactory A searcher for the media sources provided by
     * the FilmTitBackendServer
     * @return
     * @throws InvalidDocumentIdException
     */
    public List<MediaSource> changeMovieTitle(long documentId, String newMovieTitle, MediaSourceFactory mediaSourceFactory)
            throws InvalidDocumentIdException {
        updateLastOperationTime();

        USDocument document = getActiveDocument(documentId);

        org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();
        // keep original media source by default
        // document.setMediaSource(null);
        document.saveToDatabase(dbSession);
        usHibernateUtil.closeAndCommitSession(dbSession);

        return mediaSourceFactory.getSuggestions(newMovieTitle);
    }

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    // HANDLING TRANSLATION RESULTS
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    /**
     * Creates the translation results from the list of timed chunks and save to
     * the database. It does start the translation suggestion generation. A
     * whole parsed document is supposed to be sent this way. (Called directly
     * from an RPC call.)
     *
     * @param chunks List of timed chunks
     * @return Void
     * @throws InvalidDocumentIdException Throws an exception if the session
     * user does not own document with given ID.
     * @throws InvalidChunkIdException Throws an exception in case the chunks
     * are from different documents.
     * @throws InvalidValueException Throws an exception when the timing is not
     * in the right format.
     */
    public Void saveSourceChunks(List<TimedChunk> chunks)
            throws InvalidDocumentIdException, InvalidChunkIdException, InvalidValueException {
        updateLastOperationTime();
        if (chunks.size() == 0) {
            return null;
        }
        USDocument document = getActiveDocument(chunks.get(0).getDocumentId());
        List<USTranslationResult> usTranslationResults = new ArrayList<USTranslationResult>(chunks.size());
        for (TimedChunk chunk : chunks) {
            if (chunk.getDocumentId() != document.getDatabaseId()) {
                throw new InvalidChunkIdException("Mismatch in documents IDs of the chunks.");
            }

            // test the start timing
            try {
                new SrtTime(chunk.getStartTime());
            } catch (InvalidValueException e) {
                throw new InvalidValueException("The start time value '" + chunk.getStartTime() + "' has wrong format. " + e.getLocalizedMessage());
            }
            // test the end timing
            try {
                new SrtTime(chunk.getEndTime());
            } catch (InvalidValueException e) {
                throw new InvalidValueException("The end time value '" + chunk.getEndTime() + "' has wrong format. " + e.getLocalizedMessage());
            }

            USTranslationResult usTranslationResult = new USTranslationResult(chunk);
            usTranslationResult.setDocument(document);
            usTranslationResults.add(usTranslationResult);
        }
        saveTranslationResults(document, usTranslationResults);
        return null;
    }

    /**
     * Receives a list of timed chunks from the client and generates the MT
     * suggestion for them. It calls the ParallelHelper Scala object that
     * parallels the translation suggestion generation by running the
     * getTranslationResults method in multiple threads. (Called directly from
     * an RPC call.)
     *
     * @param chunks List of timed chunk from the client
     * @param TM An instance of translation memory passed form the
     * FilmTitBackendServer
     * @return List of translation results with suggestions
     * @throws InvalidDocumentIdException Throws an exception if the session
     * user does not own document with given ID.
     */
    public List<TranslationResult> getTranslationResultsParallel(List<TimedChunk> chunks, TranslationMemory TM) throws InvalidDocumentIdException {

        // set chunks active
        if (chunks == null || chunks.isEmpty()) {
            return null;
        } else {
            USDocument document = getActiveDocument(chunks.get(0).getDocumentId());

            for (TimedChunk chunk : chunks) {
                ChunkIndex index = chunk.getChunkIndex();
                USTranslationResult usTranslationResult = document.getTranslationResultForIndex(index);
                usTranslationResult.setChunkActive(true);
            }
        }

        // get the results
        List<TranslationResult> res = ParallelHelper.getTranslationsParallel(chunks, this, TM);
        return res;
    }

    /**
     * Gets the translation result with translation memory suggestions when a
     * timed chunk is given. The methods is called by the ParallelHelper scala
     * object.
     *
     * @param chunk Timed chunk
     * @param TM An instance of translation memory provided by the
     * FilmTitBackendServer.
     * @return Translation result with generated suggestions
     * @throws InvalidDocumentIdException Throws an exception if the session
     * user does not own document with given ID.
     */
    public TranslationResult getTranslationResults(TimedChunk chunk, TranslationMemory TM) throws InvalidDocumentIdException {
        updateLastOperationTime();
        USDocument document = getActiveDocument(chunk.getDocumentId());

        ChunkIndex index = chunk.getChunkIndex();
        USTranslationResult usTranslationResult = document.getTranslationResultForIndex(index);

        usTranslationResult.generateMTSuggestions(TM);
        return usTranslationResult.getResultCloneAndRemoveSuggestions();
    }

    /**
     * Stops the translation results generation in the Translation Memory Core.
     * (Called directly from an RPC call.)
     *
     * @param chunks List of timed chunks to be stopped.
     * @return Void
     * @throws InvalidDocumentIdException Throws an exception if the session
     * user does not own document with given ID.
     */
    public Void stopTranslationResults(List<TimedChunk> chunks) throws InvalidDocumentIdException {
        if (chunks == null || chunks.isEmpty()) {
            return null;
        } else {
            USDocument document = getActiveDocument(chunks.get(0).getDocumentId());

            for (TimedChunk chunk : chunks) {
                ChunkIndex index = chunk.getChunkIndex();
                document.getTranslationResultForIndex(index).setChunkActive(false);
            }

            logger.info("!!! STOP TRANSLATION RESULTS !!! " + chunks.size() + " chunks");
            return null;
        }
    }

    /**
     * Sets the translation the user has written and ID of the translation pair
     * he has chosen as the best suggestion and has postedited. (Called directly
     * from an RPC call.)
     *
     * @param chunkIndex Index of the chunk that has been translated
     * @param documentId ID of the document the chunk belongs to
     * @param userTranslation The tranlsation provided by user
     * @param chosenTranslationPairID ID of the tranlsation pair the user has
     * chosen
     * @return Void
     * @throws InvalidDocumentIdException Throws an exception if the session
     * user does not own document with given ID.
     * @throws InvalidChunkIdException Throws an exception if the document does
     * not contain chunk with given index.
     */
    public Void setUserTranslation(ChunkIndex chunkIndex, long documentId, String userTranslation, long chosenTranslationPairID)
            throws InvalidDocumentIdException, InvalidChunkIdException {
        updateLastOperationTime();

        USDocument document = getActiveDocument(documentId);
        USTranslationResult tr = document.getTranslationResultForIndex(chunkIndex);

        if (tr == null) {
            String s = ("TranslationResult is null for index " + chunkIndex + ", document has id : " + document.getDatabaseId() + ", translationresults : " + document.getTranslationResultKeys());
            throw new RuntimeException(s);

        }

        // update count of already translated chunks
        if ((tr.getUserTranslation() == null || tr.getUserTranslation().equals(""))
                && (userTranslation != null && !userTranslation.equals(""))) {
            // increment if was empty and is not
            document.setTranslatedChunksCount(document.getTranslatedChunksCount() + 1);
        } else if ((tr.getUserTranslation() != null && !tr.getUserTranslation().equals(""))
                && (userTranslation == null || userTranslation.equals(""))) {
            // decrement if wasn't empty and now it is
            document.setTranslatedChunksCount(document.getTranslatedChunksCount() - 1);
        }

        // set the translation
        tr.setUserTranslation(userTranslation);
        tr.setSelectedTranslationPairID(chosenTranslationPairID);
        saveTranslationResult(document, tr);

        return null;
    }

    /**
     * Sets the new start time of the given chunk, resp. translation results.
     * (Called directly from an RPC call.)
     *
     * @param chunkIndex Index of chunk being changed
     * @param documentId ID of the document the chunk belongs to.
     * @param newStartTime New start time of the chunk
     * @return Void
     * @throws InvalidDocumentIdException Throws an exception if the session
     * user does not own document with given ID.
     * @throws InvalidChunkIdException Throws an exception if the document does
     * not contain chunk with given index.
     * @throws InvalidValueException Throws an exception if the timing is
     * wrongly formated.
     */
    public Void setChunkStartTime(ChunkIndex chunkIndex, long documentId, String newStartTime)
            throws InvalidDocumentIdException, InvalidChunkIdException, InvalidValueException {
        updateLastOperationTime();

        if (!timingRegexp.matcher(newStartTime).matches()) {
            throw new InvalidValueException("Wrong format of the timing '" + newStartTime + "'.");
        }

        USDocument document = activeDocuments.get(documentId);

        USTranslationResult tr = document.getTranslationResultForIndex(chunkIndex);

        if (new SrtTime(newStartTime).compareTo(new SrtTime(tr.getEndTime())) > 0) {
            throw new InvalidValueException("Start time would be later than end time.");
        }

        tr.setStartTime(newStartTime);
        saveTranslationResult(document, tr);
        return null;
    }

    /**
     * Sets the new end time of the given chunk, resp. translation results.
     * (Called directly from an RPC call.)
     *
     * @param chunkIndex Index of chunk being changed
     * @param documentId ID of the document the chunk belongs to.
     * @param newEndTime New end time of the chunk
     * @return Void
     * @throws InvalidDocumentIdException Throws an exception if the session
     * user does not own document with given ID.
     * @throws InvalidChunkIdException Throws an exception if the document does
     * not contain chunk with given index.
     * @throws InvalidValueException Throws an exception if the timing is
     * wrongly formated.
     */
    public Void setChunkEndTime(ChunkIndex chunkIndex, long documentId, String newEndTime)
            throws InvalidDocumentIdException, InvalidChunkIdException, InvalidValueException {
        updateLastOperationTime();

        if (!timingRegexp.matcher(newEndTime).matches()) {
            throw new InvalidValueException("Wrong format of the timing '" + newEndTime + "'.");
        }

        USDocument document = getActiveDocument(documentId);

        USTranslationResult tr = document.getTranslationResultForIndex(chunkIndex);

        if (new SrtTime(tr.getStartTime()).compareTo(new SrtTime(newEndTime)) > 0) {
            throw new InvalidValueException("Start time would be later than end time.");
        }

        tr.setEndTime(newEndTime);
        saveTranslationResult(document, tr);
        return null;
    }

    /**
     * Change the start time and end time of the given chunk to the values.
     *
     * @param chunkIndex Identifier of the chunk that has been changed
     * @param documentId ID of the document the chunk belongs
     * @param newStartTime New value of chunk start time
     * @param newEndTime New value of chunk end time
     * @return Void
     * @throws InvalidDocumentIdException Throws an exception when the user does
     * not have document of given ID.
     * @throws InvalidChunkIdException Throws an exception when such chunk does
     * not exist in the document.
     */
    public Void setChunkTimes(ChunkIndex chunkIndex, long documentId, String newStartTime, String newEndTime)
            throws InvalidValueException, InvalidDocumentIdException {

        if (!timingRegexp.matcher(newStartTime).matches()) {
            throw new InvalidValueException("Wrong format of the timing '" + newStartTime + "'.");
        }
        if (!timingRegexp.matcher(newEndTime).matches()) {
            throw new InvalidValueException("Wrong format of the timing '" + newEndTime + "'.");
        }

        USDocument document = getActiveDocument(documentId);

        USTranslationResult tr = document.getTranslationResultForIndex(chunkIndex);

        if (new SrtTime(newStartTime).compareTo(new SrtTime(newEndTime)) > 0) {
            throw new InvalidValueException("Start time would be later than end time.");
        }

        tr.setEndTime(newEndTime);
        tr.setStartTime(newStartTime);
        saveTranslationResult(document, tr);
        return null;
    }

    /**
     * Sets the new source text of the chunk, resp. translation results and
     * generates new translation suggestions.
     *
     * @param chunk The timed chunk with the changed source text
     * @param dbForm New source text, in DB form (i.e. with pipes)
     * @param TM A translation memory instance provided by FilmTitBackendServer
     * @return Translation result object that reflects the changes
     * @throws InvalidDocumentIdException Throws an exception if the session
     * user does not own document with given ID.
     * @throws InvalidChunkIdException Throws an exception if the document does
     * not contain chunk with given index.
     */
    public TranslationResult changeText(TimedChunk chunk, String dbForm, TranslationMemory TM)
            throws InvalidDocumentIdException, InvalidChunkIdException {
        updateLastOperationTime();

        // find the document and translation result
        USDocument document = getActiveDocument(chunk.getDocumentId());
        USTranslationResult usTranslationResult = document.getTranslationResultForIndex(chunk.getChunkIndex());

        // set the text
        usTranslationResult.getTranslationResult().getSourceChunk().setDatabaseFormForce(dbForm);
        saveTranslationResult(document, usTranslationResult);

        // generate suggestions
        usTranslationResult.generateMTSuggestions(TM);
        return usTranslationResult.getResultCloneAndRemoveSuggestions();
    }

    /**
     * Deletes a chunk having particular chunk index and belonging to a
     * particular document.
     *
     * @param chunkIndex Index of chunk to be deleted.
     * @param documentId ID of the document containing the chunk to be deleted.
     * @return Void
     * @throws InvalidDocumentIdException Throws an exception if the user does
     * not own document with given ID.
     * @throws InvalidChunkIdException Throws an exception if the document does
     * not contain chunk with given index.
     */
    public Void deleteChunk(ChunkIndex chunkIndex, long documentId)
            throws InvalidDocumentIdException, InvalidChunkIdException {
        updateLastOperationTime();

        USDocument document = getActiveDocument(documentId);
        USTranslationResult translationResult = document.getTranslationResultForIndex(chunkIndex);

        org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();
        translationResult.deleteFromDatabase(dbSession);
        usHibernateUtil.closeAndCommitSession(dbSession);

        document.removeTranslationResult(chunkIndex);

        return null;
    }

    /**
     * Gets the information if the owner of the session owns document of
     * particular id.
     *
     * @param id ID of required document.
     * @return A sign if the user owns the document.
     */
    public boolean hasDocument(long id) {
        return user.getOwnedDocuments().containsKey(id);
    }

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    /**
     * Saves all translation results in a document.
     *
     * @param document Document to be saved.
     */
    public void saveAllTranslationResults(USDocument document) {
        Collection<USTranslationResult> results = document.getTranslationResultValues();
        saveTranslationResults(document, results);
    }

    /**
     * Adds the given (exatcly one) translation result to the document (or
     * updates if it already exists - it is identified by ChunkIndex) and saves
     * the updated document to the database.
     *
     * @param document Document to be saved to the database
     * @param result Translation result to be saved.
     *
     */
    public void saveTranslationResult(USDocument document, USTranslationResult result) {
        ArrayList<USTranslationResult> al = new ArrayList<USTranslationResult>(1);
        al.add(result);
        saveTranslationResults(document, al);
    }

    /**
     * Adds the given translation results to the document (or updates if they
     * already exist - they are identified by ChunkIndex) and saves the updated
     * document to the database.
     *
     * @param document USDocument do be saved to the database
     * @param results Collection of translation results to be saved or updated
     * in the database.
     */
    public synchronized void saveTranslationResults(USDocument document, Collection<USTranslationResult> results) {
        org.hibernate.Session session = usHibernateUtil.getSessionWithActiveTransaction();

        // save document because of changes in last edit time and translated chunks count
        document.saveToDatabaseJustDocument(session);

        // save the translation results
        for (USTranslationResult tr : results) {
            document.addOrReplaceTranslationResult(tr);
            tr.saveToDatabase(session);
        }
        usHibernateUtil.closeAndCommitSession(session);
    }

    /**
     * Updates the last operation time to the current time. It should be called
     * by every method which is responding for an RPC call.
     */
    private void updateLastOperationTime() {
        lastOperationTime = new Date().getTime();
    }

    /**
     * Gets the active document of given ID. If the document is not active at
     * the time this method is called it is loaded to active documents.
     *
     * @param documentID ID of the requested document
     * @return USDocument object of given ID
     * @throws InvalidDocumentIdException It throws an exception if the document
     * with such ID is not owned by the user.
     */
    public synchronized USDocument getActiveDocument(long documentID) throws InvalidDocumentIdException {
        if (!activeDocuments.containsKey(documentID)) {
            logger.info("Loading document " + documentID + "to memory.");
            loadDocumentIfNotActive(documentID);
        }

        USDocument document = activeDocuments.get(documentID);
        document.setLastChange(new Date().getTime());
        return document;
    }

    /**
     * It loads translation results of a document a place it to the map of
     * active documents.
     *
     * @param documentID ID of the document to be loaded
     * @return USDocument object with loaded translation results.
     * @throws InvalidDocumentIdException It throws an exception if the document
     * with such ID is not owned by the user.
     */
    private synchronized USDocument loadDocumentIfNotActive(long documentID) throws InvalidDocumentIdException {

        org.hibernate.Session session = usHibernateUtil.getSessionWithActiveTransaction();
        USDocument usDocument = (USDocument) session.get(USDocument.class, documentID);

        if (usDocument == null) {
            throw new InvalidDocumentIdException(String.valueOf(documentID));
        }

        usDocument.loadChunksFromDb();
        activeDocuments.put(documentID, usDocument);
        logger.info("User " + user.getUserName() + " opened document " + documentID + " ("
                + usDocument.getTitle() + ").");
        return usDocument;

    }

    /**
     * Save the user who is owner of the session to the database. It called when
     * a change of user settings is made.
     */
    private synchronized void saveUser() {
        org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();
        user.saveToDatabase(dbSession);
        usHibernateUtil.closeAndCommitSession(dbSession);
    }

    /**
     * Change login
     *
     * @param newLogin
     * @return if change was successful
     */
    public Void setUsername(String newLogin) throws InvalidValueException {

        // USUser usUser = FilmTitBackendServer.checkUser(user,"",CheckUserEnum.UserName);
        if (newLogin != null) {
            USUser check = FilmTitBackendServer.checkUser(newLogin, "", CheckUserEnum.UserName);

            if (check != null) {
                // exist user with login same like new login
                throw new InvalidValueException("A user with the username '" + newLogin + "' already exists!");
            }

            user.setUserName(newLogin);

            // save into db
            saveUser();
        }
        return null;
    }

    public Void setPassword(String password) {
        user.setPassword(FilmTitBackendServer.passHash(password));
        saveUser();
        return null;
    }

}

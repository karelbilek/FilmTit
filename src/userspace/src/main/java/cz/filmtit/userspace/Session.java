package cz.filmtit.userspace;

import cz.filmtit.core.model.MediaSourceFactory;
import cz.filmtit.core.model.TranslationMemory;
import cz.filmtit.share.*;
import cz.filmtit.share.exceptions.InvalidChunkIdException;
import cz.filmtit.share.exceptions.InvalidDocumentIdException;
import cz.filmtit.share.exceptions.InvalidValueException;
import cz.filmtit.userspace.servlets.FilmTitBackendServer;
import org.jboss.logging.Logger;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Represents a running session.
 * @author Jindřich Libovický
 */
public class Session {
    private String sessionId;
    private long databaseId = Long.MIN_VALUE;
    private USUser user;

    private volatile long sessionStart;
    private volatile long lastOperationTime;
    private volatile SessionState state;
    Logger logger = Logger.getLogger("Session");

    private static final Pattern timingRegexp = Pattern.compile("[0-9]{2}:[0-9]{2}:[0-9]{2},[0-9]{3}]");

    private static USHibernateUtil usHibernateUtil = USHibernateUtil.getInstance();

    enum SessionState {active, loggedOut, terminated, killed}

    /**
     * Cache hash table for active documents owned by current user, to make them quickly available using their IDs.
     */
    private Map<Long, USDocument> activeDocuments;
    /**
     * Cache hash table for translation results belonging to active documents of the current user
     */

    public Session(USUser user) {
        activeDocuments = Collections.synchronizedMap(new HashMap<Long, USDocument>());
        
        sessionStart = new Date().getTime();
        updateLastOperationTime();
        state = SessionState.active;
        this.user = user;
    }

    public long getLastOperationTime() {
        return lastOperationTime;
    }

    private void setLastOperationTime(long time) {}

    public long getSessionStart() {
        return sessionStart;
    }

    private void setSessionStart(long time) {}

    public USUser getUser() {
        return user;
    }

    public long getUserDatabaseId() {
        return user.getDatabaseId();
    }

    private void setUserDatabaseId(long id) {}

    private long getDatabaseId() {
        return databaseId;
    }

    private void setDatabaseId(long databaseId) {
        if (this.databaseId == databaseId) { return; }
        if (this.databaseId == Long.MIN_VALUE) {
            this.databaseId = databaseId;
            return;
        }
        throw new UnsupportedOperationException("Once the database ID is set, it can't be changed.");
    }

    private String getStateString() {
        return state.toString();
    }

    private void setStateString(String stateString) {}

    public boolean isPermanent() {
        return user.isPermanentlyLoggedId();
    }

    public void logout() {
        state = SessionState.loggedOut;
        terminate();
    }

    public void terminateOnNewLogin() {
        state = SessionState.terminated;
        logger.info("Previous session of " + user.getUserName() + "was terminated before creating a new one.");
        terminate();
    }

    /**
     * Terminates the session. Used in all session terminating situations (i.e. logout, time-out, re-login).
     */
    private synchronized void terminate() {
        // the session was already terminated and is in database => skip this method
        if (databaseId != Long.MIN_VALUE) { return; }

        org.hibernate.Session session = usHibernateUtil.getSessionWithActiveTransaction();
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
    public Void setPermanentlyLoggedIn(boolean permanentlyLoggedIn) {
        user.setPermanentlyLoggedId(permanentlyLoggedIn);
        return null;
    }

    public Void setEmail(String email) throws InvalidChunkIdException {
        if (!FilmTitBackendServer.mailRegexp.matcher(email).matches()) {
            throw new InvalidChunkIdException(email + "is not a valid email address.");
        }

        user.setEmail(email);
        saveUser();
        return null;
    }

    public Void setMaximumNumberOfSuggestions(int number) throws InvalidValueException {
        if (number < 0 || number > 100) {
            throw new InvalidValueException("The maximum number of suggestion must be a positive integer lesser than 100.");
        }
        user.setMaximumNumberOfSuggestions(number);
        saveUser();
        return null;
    }

    public Void setUseMoses(boolean useMoses) {
        user.setUseMoses(useMoses);
        saveUser();
        return null;
    }

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    // HANDLING DOCUMENTS
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public DocumentResponse createNewDocument(String documentTitle, String movieTitle, String language, MediaSourceFactory mediaSourceFactory) {
        updateLastOperationTime();
        USDocument usDocument = new USDocument( new Document(documentTitle, language) , user);
        List<MediaSource> suggestions = mediaSourceFactory.getSuggestions(movieTitle);

        activeDocuments.put(usDocument.getDatabaseId(), usDocument);

        user.addDocument(usDocument);
        logger.info("User " + user.getUserName() + " opened document " + usDocument.getDatabaseId() + " (" +
                usDocument.getTitle() + ").");
        return new DocumentResponse(usDocument.getDocument(), suggestions);
    }

    public Void deleteDocument(long documentId) throws InvalidDocumentIdException {
        USDocument document = getActiveDocument(documentId);

        activeDocuments.remove(documentId);
        user.getOwnedDocuments().remove(documentId);
        document.setToBeDeleted(true);

        // take care of the database and the translation results in separate thread
        new DeleteDocumentRunner(document).run();
        return null;
    }

    /**
     * A thread that performs the addition operation on the document deletion. It deletes the document from
     * the database all the document's translation results that has already provided feedback to the core
     * and if they are no translation results left it it deletes the
     */
    private class DeleteDocumentRunner extends Thread {
        USDocument document;

        public DeleteDocumentRunner(USDocument document) {
            this.document = document;
        }

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
                    }
                    else { // there's a result that hasn't provided => don't delete the document
                        deleteDocument = false;
                    }
                }
            }

            if  (deleteDocument) {
                document.deleteFromDatabase(dbSession);
            }
            usHibernateUtil.closeAndCommitSession(dbSession);}
    }

    public Void selectSource(long documentId, MediaSource selectedMediaSource)
            throws InvalidDocumentIdException {
        updateLastOperationTime();

        USDocument document = getActiveDocument(documentId);

        org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();

        List sourcesFromDb = dbSession.createQuery("select m from MediaSource m where m.title like '" +
                selectedMediaSource.getTitle()+"' and m.year like '" + selectedMediaSource.getYear() + "'").list();

        if (sourcesFromDb.size() == 0) {
            dbSession.save(selectedMediaSource);
            document.setMovie(selectedMediaSource);
        }
        else {
            // if the media source is already in db, update it to the latest freebase version
            MediaSource fromDb = (MediaSource) sourcesFromDb.get(0);
            fromDb.setGenres(selectedMediaSource.getGenres());
            fromDb.setThumbnailURL(selectedMediaSource.getThumbnailURL());
            dbSession.update(fromDb);

            document.setMovie(fromDb);
        }


        document.saveToDatabase(dbSession);
        usHibernateUtil.closeAndCommitSession(dbSession);

        return null;
    }

    public List<Document> getListOfDocuments() {
        updateLastOperationTime();
        List<Document> result = new ArrayList<Document>();

        for(USDocument usDocument : user.getOwnedDocuments().values()) {
            result.add(usDocument.getDocument().documentWithoutResults());
        }

        Collections.sort(result);
        return result;
    }

    /**
     * Implements FilmTitService.loadDocument
     * @param documentID
     * @return the document, with the chunks loaded
     * @throws InvalidDocumentIdException
     */
    public Document loadDocument(long documentID) throws InvalidDocumentIdException {
        updateLastOperationTime();
        return getActiveDocument(documentID).getDocument();
    }

    public Void closeDocument(long documentId) throws InvalidDocumentIdException {
        updateLastOperationTime();

        USDocument document = getActiveDocument(documentId);

        activeDocuments.remove(documentId);
        logger.info("User " + user.getUserName() + " closed document " + documentId + " (" + document.getTitle() + ")." );
        saveAllTranslationResults(document);
        return null;
    }

    public Void changeDocumentTitle(long documentId, String newTitle) throws InvalidDocumentIdException {
        updateLastOperationTime();

        USDocument document = getActiveDocument(documentId);

        org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();
        document.setTitle(newTitle);
        document.saveToDatabase(dbSession);
        usHibernateUtil.closeAndCommitSession(dbSession);

        return null;
    }

    public List<MediaSource> changeMovieTitle (long documentId, String newMovieTitle,  MediaSourceFactory mediaSourceFactory)
            throws InvalidDocumentIdException {
        updateLastOperationTime();

        USDocument document = getActiveDocument(documentId);

        org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();
        document.setMovie(null);
        document.saveToDatabase(dbSession);
        usHibernateUtil.closeAndCommitSession(dbSession);

        return mediaSourceFactory.getSuggestions(newMovieTitle);
    }

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    // HANDLING TRANSLATION RESULTS
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public Void saveSourceChunks(List<TimedChunk> chunks)
            throws InvalidDocumentIdException, InvalidChunkIdException {
        updateLastOperationTime();
        if (chunks.size() == 0) {
            return null;
        }
        USDocument document = getActiveDocument(chunks.get(0).getDocumentId());
        List<USTranslationResult> usTranslationResults = new ArrayList<USTranslationResult>(chunks.size());
        for (TimedChunk chunk: chunks) {
            if (chunk.getDocumentId() != document.getDatabaseId()) {
                throw new InvalidChunkIdException("Mismatch in documents IDs of the chunks.");
            }

            USTranslationResult usTranslationResult = new USTranslationResult(chunk);
            usTranslationResult.setDocument(document);
            usTranslationResults.add(usTranslationResult);
        }
        saveTranslationResults(document, usTranslationResults);
        return null;
    }

    /**
     * Implements FilmTitService.getTranslationResults,
     * calls ParallelHelper,
     * ParallelHelper calls getTranslationResults().
     */
    public List<TranslationResult> getTranslationResultsParallel(List<TimedChunk> chunks, TranslationMemory TM) throws InvalidDocumentIdException {

    	// set chunks active
        if (chunks == null || chunks.isEmpty()) {
        	return null;
        }
        else {
            USDocument document = getActiveDocument(chunks.get(0).getDocumentId());

            for (TimedChunk chunk : chunks) {
                ChunkIndex index = chunk.getChunkIndex();
                USTranslationResult usTranslationResult = document.getTranslationResultForIndex(index);
                usTranslationResult.setChunkActive(true);
    		}
        }
        // TODO: do not throw away document and usTranslationResult, pass them directly through ParallellHelper!
        // (and change getTranslationResults() accordingly)
        
        // get the results
        List<TranslationResult> res = ParallelHelper.getTranslationsParallel(chunks, this, TM);
        return res;
    }
    
    /**
     * Implements FilmTitService.getTranslationResults,
     * called by ParallelHelper.
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
     * Implements FilmTitService.stopTranslationResults
     * @param chunks
     * @throws InvalidDocumentIdException
     */
    public Void stopTranslationResults(List<TimedChunk> chunks) throws InvalidDocumentIdException {
        if (chunks == null || chunks.isEmpty()) {
        	return null;
        }
        else {
            USDocument document = getActiveDocument(chunks.get(0).getDocumentId());

            for (TimedChunk chunk : chunks) {
                ChunkIndex index = chunk.getChunkIndex();
                document.getTranslationResultForIndex(index).setChunkActive(false);
    		}
            
            logger.info("!!! STOP TRANSLATION RESULTS !!! " + chunks.size() + " chunks");
            return null;
        }
    }

    public Void setUserTranslation(ChunkIndex chunkIndex, long documentId, String userTranslation, long chosenTranslationPairID)
            throws InvalidDocumentIdException, InvalidChunkIdException {
        updateLastOperationTime();

        USDocument document = getActiveDocument(documentId);
        USTranslationResult tr = document.getTranslationResultForIndex(chunkIndex);

        if (tr==null) {
            String s = ("TranslationResult is null for index "+chunkIndex +", document has id : "+document.getDatabaseId()+", translationresults : "+document.getTranslationResultKeys());
           throw new RuntimeException(s);

        }

        // update count of already translated chunks
        if ((tr.getUserTranslation() == null || tr.getUserTranslation().equals("")) &&
                (userTranslation != null && !userTranslation.equals("") )) {
        	// increment if was empty and is not
            document.setTranslatedChunksCount(document.getTranslatedChunksCount() + 1);
        }
        else if ((tr.getUserTranslation() != null && !tr.getUserTranslation().equals("")) &&
                (userTranslation == null || userTranslation.equals("") )) {
        	// decrement if wasn't empty and now it is
            document.setTranslatedChunksCount(document.getTranslatedChunksCount() - 1);
        }
        
        // set the translation
        tr.setUserTranslation(userTranslation);
        tr.setSelectedTranslationPairID(chosenTranslationPairID);
        saveTranslationResult(document, tr);

        return null;
    }

    public Void setChunkStartTime(ChunkIndex chunkIndex, long documentId, String newStartTime)
            throws InvalidDocumentIdException, InvalidChunkIdException, InvalidValueException {
        updateLastOperationTime();

        if (!timingRegexp.matcher(newStartTime).matches()) {
            throw new InvalidValueException("Wrong format of the timing.");
        }

        USDocument document = activeDocuments.get(documentId);

        USTranslationResult tr = document.getTranslationResultForIndex(chunkIndex);
        tr.setStartTime(newStartTime);
        saveTranslationResult(document, tr);
        return  null;
    }

    public Void setChunkEndTime(ChunkIndex chunkIndex, long documentId, String newEndTime)
            throws InvalidDocumentIdException, InvalidChunkIdException, InvalidValueException {
        updateLastOperationTime();

        if (!timingRegexp.matcher(newEndTime).matches()) {
            throw new InvalidValueException("Wrong format of the timing.");
        }

        USDocument document = getActiveDocument(documentId);

        USTranslationResult tr = document.getTranslationResultForIndex(chunkIndex);

        tr.setEndTime(newEndTime);
        saveTranslationResult(document, tr);
        return  null;
    }

    public List<TranslationPair> changeText(ChunkIndex chunkIndex, long documentId, String text, TranslationMemory TM)
            throws InvalidDocumentIdException, InvalidChunkIdException {
        updateLastOperationTime();

        USDocument document = getActiveDocument(documentId);

        USTranslationResult translationResult = document.getTranslationResultForIndex(chunkIndex);
        translationResult.setText(text);

        saveTranslationResult(document, translationResult);
        return requestTMSuggestions(chunkIndex, documentId, TM);
    }

    public List<TranslationPair> requestTMSuggestions(ChunkIndex chunkIndex, long documentId, TranslationMemory TM)
            throws InvalidDocumentIdException, InvalidChunkIdException {
        updateLastOperationTime();

        USDocument document = getActiveDocument(documentId);
        
        USTranslationResult selected = document.getTranslationResultForIndex(chunkIndex);
        selected.generateMTSuggestions(TM);
        List<TranslationPair> l = new ArrayList<TranslationPair>();
        l.addAll( selected.getTranslationResult().getTmSuggestions());
        selected.getTranslationResult().setTmSuggestions(null); // do not store suggestion in the user space

        return l;
    }

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

    public boolean hasDocument(long id) {
        return user.getOwnedDocuments().containsKey(id);
    }

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public void saveAllTranslationResults(long l) {
        saveAllTranslationResults(activeDocuments.get(l));
    }

    public void saveAllTranslationResults(USDocument document) {
        Collection<USTranslationResult> results = document.getTranslationResultValues();
        saveTranslationResults(document, results);
    }

    /**
     * Adds the given translation result to the document
     * (or updates if it already exists - it is identified by ChunkIndex)
     * and saves the updated document to the database.
     */
    public void saveTranslationResult(USDocument document, USTranslationResult result) {
       ArrayList<USTranslationResult> al = new ArrayList<USTranslationResult>(1);
       al.add(result);
       saveTranslationResults(document, al);
    }

    /**
     * Adds the given translation results to the document
     * (or updates if they already exist - they are identified by ChunkIndex)
     * and saves the updated document to the database.
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

    private void updateLastOperationTime() {
        lastOperationTime = new Date().getTime();
    }

    public USDocument getActiveDocument(long documentID) throws InvalidDocumentIdException {
        if (!activeDocuments.containsKey(documentID)) {
            logger.info("Loading document " + documentID + "to memory.");
            loadDocumentIfNotActive(documentID);
        }

        USDocument document = activeDocuments.get(documentID);
        document.setLastChange(new Date().getTime());
        return document;
    }

    private synchronized USDocument loadDocumentIfNotActive(long documentID) throws InvalidDocumentIdException {
        if (user.getOwnedDocuments().containsKey(documentID)) {
            USDocument usDocument = user.getOwnedDocuments().get(documentID);
            usDocument.loadChunksFromDb();
            activeDocuments.put(documentID, usDocument);
            logger.info("User " + user.getUserName() + " opened document " + documentID + " (" +
                    usDocument.getTitle() + ").");
            return  usDocument;
        }
        throw new InvalidDocumentIdException("User does not have document with such ID.");
    }

    private synchronized void saveUser() {
        org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();
        user.saveToDatabase(dbSession);
        usHibernateUtil.closeAndCommitSession(dbSession);
    }
}

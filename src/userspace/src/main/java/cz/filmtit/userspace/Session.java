package cz.filmtit.userspace;

import cz.filmtit.core.model.MediaSourceFactory;
import cz.filmtit.core.model.TranslationMemory;
import cz.filmtit.share.*;
import cz.filmtit.share.exceptions.InvalidChunkIdException;
import cz.filmtit.share.exceptions.InvalidDocumentIdException;
import org.jboss.logging.Logger;

import java.util.*;

/**
 * Represents a running session.
 * @author Jindřich Libovický
 */
public class Session {
    private String sessionId;
    private long databaseId = Long.MIN_VALUE;
    private USUser user;

    private long sessionStart;
    private volatile long lastOperationTime;
    private volatile SessionState state;
    Logger logger = Logger.getLogger("Session");
    
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

        // load the active documents and active translation results from the user object
        for (Long documentID : user.getActiveDocumentIDs()) {
            activeDocuments.put(documentID, user.getOwnedDocuments().get(documentID));
        }
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

        user.getActiveDocumentIDs().clear();

        session = usHibernateUtil.getSessionWithActiveTransaction();
        for (USDocument activeDoc : activeDocuments.values()) {
            activeDoc.saveToDatabase(session);
            user.getActiveDocumentIDs().add(activeDoc.getDatabaseId());
        }
        usHibernateUtil.closeAndCommitSession(session);

        session = usHibernateUtil.getSessionWithActiveTransaction();
        user.saveToDatabase(session);
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

        user.getOwnedDocuments().remove(document);
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
        document.setMovie(selectedMediaSource);
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

        if (user.getOwnedDocuments().containsKey(documentID)) {
            USDocument usDocument = user.getOwnedDocuments().get(documentID);
            usDocument.loadChunksFromDb();
            activeDocuments.put(documentID, usDocument);
            logger.info("User " + user.getUserName() + " opened document " + documentID + " (" +
                    usDocument.getTitle() + ").");
            return  usDocument.getDocument();
        }
        throw new InvalidDocumentIdException("The user does not own a document with such ID.");
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
        return mediaSourceFactory.getSuggestions(newMovieTitle);
    }

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    // HANDLING TRANSLATION RESULTS
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public Void saveSourceChunks(List<TimedChunk> chunks) throws InvalidDocumentIdException {
        updateLastOperationTime();
        if (chunks.size() == 0) {
            return null;
        }
        USDocument document = getActiveDocument(chunks.get(0).getDocumentId());
        List<USTranslationResult> usTranslationResults = new ArrayList<USTranslationResult>(chunks.size());
        for (TimedChunk chunk: chunks) {
            // TODO: maybe we should check here that all of the chunks have the same documentId
            USTranslationResult usTranslationResult = new USTranslationResult(chunk);
            // TODO why does it not simply get the id from the chunk?
            usTranslationResult.setDocument(document);
            usTranslationResults.add(usTranslationResult);
        }
        saveTranslationResults(document, usTranslationResults);
        return null;
    }

    /**
     * Implements FilmTitService.getTranslationResults
     */
    public TranslationResult getTranslationResults(TimedChunk chunk, TranslationMemory TM) throws InvalidDocumentIdException {
        updateLastOperationTime();
        USDocument document = getActiveDocument(chunk.getDocumentId());

        ChunkIndex index = chunk.getChunkIndex();
        USTranslationResult usTranslationResult = document.getTranslationResultForIndex(index);

        usTranslationResult.generateMTSuggestions(TM);
        return usTranslationResult.getResultCloneAndRemoveSuggestions();
    }

    public Void setUserTranslation(ChunkIndex chunkIndex, long documentId, String userTranslation, long chosenTranslationPairID)
            throws InvalidDocumentIdException, InvalidChunkIdException {
        updateLastOperationTime();

        // TODO I need to be able to access any of my documents, not only the ones I loaded
        if (!activeDocuments.containsKey(documentId)) {
        	loadDocument(documentId);
        }
        
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
            throws InvalidDocumentIdException, InvalidChunkIdException {
        updateLastOperationTime();

        if (!activeDocuments.containsKey(documentId)) {
            throw new InvalidDocumentIdException("Not existing document ID.");
        }
        USDocument document = activeDocuments.get(documentId);

        USTranslationResult tr = document.getTranslationResultForIndex(chunkIndex);
        tr.setStartTime(newStartTime);
        saveTranslationResult(document, tr);
        return  null;
    }

    public Void setChunkEndTime(ChunkIndex chunkIndex, long documentId, String newEndTime)
            throws InvalidDocumentIdException, InvalidChunkIdException {
        updateLastOperationTime();

        if (!activeDocuments.containsKey(documentId)) {
            throw new InvalidDocumentIdException("Not existing document ID.");
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

    public synchronized USDocument getActiveDocument(long documentID) throws InvalidDocumentIdException {
        if (!activeDocuments.containsKey(documentID)) {
            logger.info("Loading document " + documentID + "to memory.");
            activeDocuments.put(documentID, user.getOwnedDocuments().get(documentID));
        }

        USDocument document = activeDocuments.get(documentID);
        document.setLastChange(new Date().getTime());
        return document;
    }
}

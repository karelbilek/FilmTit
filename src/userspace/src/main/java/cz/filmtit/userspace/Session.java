package cz.filmtit.userspace;

import cz.filmtit.core.model.MediaSourceFactory;
import cz.filmtit.core.model.TranslationMemory;
import cz.filmtit.share.*;
import cz.filmtit.share.exceptions.InvalidChunkIdException;
import cz.filmtit.share.exceptions.InvalidDocumentIdException;

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
    private long lastOperationTime;
    private SessionState state;
    
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
        for (USDocument document : user.getOwnedDocuments()) {
            // if the document has been active the last time the session was terminated, ...
            if (user.getActiveDocumentIDs().contains(document.getDatabaseId())) {
                // load the document
                activeDocuments.put(document.getDatabaseId(), document);
                // load its chunks
            }
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

    public long getDatabaseId() {
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

    public void logout() {
        state = SessionState.loggedOut;
        terminate();
    }

    /**
     * Terminates the session. Usually in the situation when the user open a new one.
     */
    private void terminate() {
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

    public DocumentResponse createNewDocument(String documentTitle, String movieTitle, String language, MediaSourceFactory mediaSourceFactory) {
        updateLastOperationTime();
        USDocument usDocument = new USDocument( new Document(documentTitle, language) , user);
        List<MediaSource> suggestions = mediaSourceFactory.getSuggestions(movieTitle);

        activeDocuments.put(usDocument.getDatabaseId(), usDocument);

        user.addDocument(usDocument);

        return new DocumentResponse(usDocument.getDocument(), suggestions);
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
    
    public Void setUserTranslation(ChunkIndex chunkIndex, long documentId, String userTranslation, long chosenTranslationPairID)
            throws InvalidDocumentIdException, InvalidChunkIdException {
        updateLastOperationTime();

        USDocument document = getActiveDocument(documentId);
        USTranslationResult tr = document.getTranslationResultForIndex(chunkIndex);

        if (tr==null) {
            String s = ("TranslationResult is null for index "+chunkIndex +", document has id : "+document.getDatabaseId()+", translationresults : "+document.getTranslationResultKeys());
           throw new RuntimeException(s);

        }

        // update count of translated chunks
        if ((tr.getUserTranslation() == null || tr.getUserTranslation().equals("")) &&
                (userTranslation != null && !userTranslation.equals("") )) {
        	// increment if was empty and is not
            document.setTranslatedChunksCount(document.getTranslatedChunksCount() + 1);
        }
        else if ((tr.getUserTranslation() != null && !tr.getUserTranslation().equals("")) &&
                (userTranslation == null || userTranslation.equals("") )) {
        	// decrement if wasn't empty and now is
            document.setTranslatedChunksCount(document.getTranslatedChunksCount() - 1);
        }
        
        // set the translation
        tr.setUserTranslation(userTranslation);
        tr.setSelectedTranslationPairID(chosenTranslationPairID);
        saveTranslationResult(document, tr);
        
        System.out.println("setUserTranslation: " + tr);
        
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

    public Void selectSource(long documentId, MediaSource selectedMediaSource)
            throws InvalidDocumentIdException {
        updateLastOperationTime();
        USDocument document = getActiveDocument(documentId);
        document.setMovie(selectedMediaSource);
        return null;
    }


    public boolean hasDocument(long id) {
        //TODO make this a set, not a list
        for(USDocument usDocument : user.getOwnedDocuments()) {
            if (usDocument.getDatabaseId() == id) {
                return true;
            }
        }
        return false;
 
    }

    public List<Document> getListOfDocuments() {
        updateLastOperationTime();
        List<Document> result = new ArrayList<Document>();

        for(USDocument usDocument : user.getOwnedDocuments()) {
            result.add(usDocument.getDocument().documentWithoutResults());
        }

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
        for (USDocument usDocument : user.getOwnedDocuments()) {
              if (usDocument.getDatabaseId() == documentID) {

                  activeDocuments.put(documentID, usDocument);

                  usDocument.loadChunksFromDb();
                  
                  return  usDocument.getDocument();
              }
        }

        throw new InvalidDocumentIdException("The user does not own a document with such ID.");
    }

    public Void closeDocument(long documentId) throws InvalidDocumentIdException {
        updateLastOperationTime();

        USDocument document = getActiveDocument(documentId);

        activeDocuments.remove(documentId);
        
        saveAllTranslationResults(document);
        return null;
    }
    

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

    // TODO: not sure if "synchronized" is needed - used because of the following note in getTranslationResults:
    // not saving it right away, because I do this in parallel, db doesn't like it
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

    private USDocument getActiveDocument(long documentID) throws InvalidDocumentIdException {
        if (!activeDocuments.containsKey(documentID)) {
            throw new InvalidDocumentIdException("The session does not have an active document with such ID.");
        }

        USDocument document = activeDocuments.get(documentID);
        document.setLastChange(new Date().getTime());
        return document;
    }
}

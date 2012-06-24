package cz.filmtit.userspace;

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

    enum SessionState {active, loggedOut, terminated, killed}

    /**
     * Cache hash table for active documents owned by current user, to make them quickly available using their IDs.
     */
    private Map<Long, USDocument> activeDocuments;
    /**
     * Cache hash table for translation results belonging to active documents of the current user
     */
    private Map<Long, Map<Integer, USTranslationResult>> activeTranslationResults;

    public Session(USUser user) {
        activeDocuments = Collections.synchronizedMap(new HashMap<Long, USDocument>());
        activeTranslationResults = Collections.synchronizedMap(new HashMap<Long, Map<Integer, USTranslationResult>>());
        sessionStart = new Date().getTime();
        lastOperationTime = new Date().getTime();
        state = SessionState.active;
        this.user = user;

        // load the active documents and active translation results from the user object
        for (USDocument document : user.getOwnedDocuments()) {
            // if the document has been active the last time the session was terminated, ...
            if (user.getActiveDocumentIDs().contains(document.getDatabaseId())) {
                // load the document
                activeDocuments.put(document.getDatabaseId(), document);
                // load its chunks
                document.loadChunksFromDb();
                activeTranslationResults.put(document.getDatabaseId(), new HashMap<Integer, USTranslationResult>());
                for (USTranslationResult tr : document.getTranslationsResults()) {
                    activeTranslationResults.get(document.getDatabaseId()).put(tr.getSharedId(), tr);
                }
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

    public SessionState getState() {
        return state;
    }

    private void setState(SessionState state) {  }

    public void logout() {
        state = SessionState.loggedOut;
        terminate();
    }

    /**
     * Terminates the session. Usually in the situation when the user open a new one.
     */
    private void terminate() {
        
        org.hibernate.Session session = HibernateUtil.getSessionWithActiveTransaction();

        session.save(this);

        // TODO: when proper users, save to database here
        //user.saveToDatabase(session);
        for (USDocument activeDoc : activeDocuments.values()) {
            activeDoc.saveToDatabase(session);
        }
        HibernateUtil.closeAndCommitSession(session);

    }

    /**
     * Kills the session when it times out.
     */
    public void kill() {
        state = SessionState.killed;
        terminate();
    }

    public Document createDocument(String movieTitle, String year, String language) {
        lastOperationTime = new Date().getTime();
        USDocument usDocument = new USDocument( new Document(movieTitle, year, language) );

        activeDocuments.put(usDocument.getDatabaseId(), usDocument);
        activeTranslationResults.put(usDocument.getDatabaseId(), Collections.synchronizedMap(new HashMap<Integer, USTranslationResult>()));
        // TODO: add the document to the correct user

        return usDocument.getDocument();
    }

    public DocumentResponse createNewDocument(String movieTitle, String year, String language, TranslationMemory TM) {
        lastOperationTime = new Date().getTime();
        USDocument usDocument = new USDocument( new Document(movieTitle, year, language) );
        List<MediaSource> suggestions = TM.mediaStorage().getSuggestions(movieTitle, year);

        activeDocuments.put(usDocument.getDatabaseId(), usDocument);
        activeTranslationResults.put(usDocument.getDatabaseId(), Collections.synchronizedMap(new HashMap<Integer, USTranslationResult>()));

        // TODO: add the document to the user !

        return new DocumentResponse(usDocument.getDocument(), suggestions);
    }

    public TranslationResult getTranslationResults(TimedChunk chunk, TranslationMemory TM) throws InvalidDocumentIdException {
        lastOperationTime = new Date().getTime();
        if (!activeDocuments.containsKey(chunk.getDocumentId())) {
            throw new InvalidDocumentIdException("Sent time chunk is referring to a document using an invalid ID.");
        }

        USDocument document = activeDocuments.get(chunk.getDocumentId());

        USTranslationResult usTranslationResult = new USTranslationResult(chunk);
        usTranslationResult.setParent(document);

        usTranslationResult.generateMTSuggestions(TM);
        document.addTranslationResult(usTranslationResult);

        activeTranslationResults.get(document.getDatabaseId()).put(chunk.getId(), usTranslationResult);
        return usTranslationResult.getTranslationResult();
    }

    public Void setUserTranslation(int chunkId, long documentId, String userTranslation, long chosenTranslationPairID)
            throws InvalidDocumentIdException, InvalidChunkIdException {
        lastOperationTime = new Date().getTime();

        if (!activeDocuments.containsKey(documentId)) {
            throw new InvalidDocumentIdException("Not existing document ID.");
        }
        if (!activeTranslationResults.get(documentId).containsKey(chunkId)) {
            throw new InvalidChunkIdException("Not existing chunk ID given.");
        }

        USTranslationResult tr = activeTranslationResults.get(documentId).get(chunkId);
        tr.setUserTranslation(userTranslation);
        tr.setSelectedTranslationPairID(chosenTranslationPairID);
        return null;
    }

    public Void setChunkStartTime(int chunkId, long documentId, String newStartTime) throws InvalidDocumentIdException, InvalidChunkIdException {
        lastOperationTime = new Date().getTime();

        if (!activeDocuments.containsKey(documentId)) {
            throw new InvalidDocumentIdException("Not existing document ID.");
        }
        if (!activeTranslationResults.get(documentId).containsKey(chunkId)) {
            throw new InvalidChunkIdException("Not existing chunk ID given.");
        }

        activeTranslationResults.get(documentId).get(chunkId).setStartTime(newStartTime);
        return  null;
    }

    public Void setChunkEndTime(int chunkId, long documentId, String newEndTime) throws InvalidDocumentIdException, InvalidChunkIdException {
        lastOperationTime = new Date().getTime();

        if (!activeDocuments.containsKey(documentId)) {
            throw new InvalidDocumentIdException("Not existing document ID.");
        }
        if (!activeTranslationResults.get(documentId).containsKey(chunkId)) {
            throw new InvalidChunkIdException("Not existing chunk ID given.");
        }

        activeTranslationResults.get(documentId).get(chunkId).setStartTime(newEndTime);
        return  null;
    }

    public TranslationResult regenerateTranslationResult(int chunkId, long documentId, TimedChunk chunk, TranslationMemory TM)
            throws InvalidDocumentIdException, InvalidChunkIdException {
        lastOperationTime = new Date().getTime();

        if (!activeDocuments.containsKey(documentId)) {
            throw new InvalidDocumentIdException("Not existing document ID.");
        }
        if (!activeTranslationResults.get(documentId).containsKey(chunkId)) {
            throw new InvalidChunkIdException("Not existing chunk ID given.");
        }

        USDocument document = activeDocuments.get(documentId);

        USTranslationResult usTranslationResult = new USTranslationResult(chunk);
        usTranslationResult.setParent(document);

        usTranslationResult.generateMTSuggestions(TM);
        document.replaceTranslationResult(usTranslationResult);

        activeTranslationResults.get(document.getDatabaseId()).put(chunk.getId(), usTranslationResult);
        return usTranslationResult.getTranslationResult();
    }

    public Void selectSource(long documentID, MediaSource selectedMediaSource) {
        lastOperationTime = new Date().getTime();
        USDocument usDocument = activeDocuments.get(documentID);
        usDocument.setMovie(selectedMediaSource);
        return null;
    }

    public List<Document> getListOfDocuments() {
        lastOperationTime = new Date().getTime();
        List<Document> result = new ArrayList<Document>();

        for(USDocument usDocument : user.getOwnedDocuments()) {
            result.add(usDocument.getDocument());
        }

        return result;
    }

    public Document loadDocument(long documentID) throws InvalidDocumentIdException {
        lastOperationTime = new Date().getTime();
        for (USDocument usDocument : user.getOwnedDocuments()) {
              if (usDocument.getDatabaseId() == documentID) {

                  activeDocuments.put(documentID, usDocument);
                  activeTranslationResults.put(documentID, new HashMap<Integer, USTranslationResult>());

                  usDocument.loadChunksFromDb();
                  for (USTranslationResult result : usDocument.getTranslationsResults()) {
                        activeTranslationResults.get(documentID).put(result.getSharedId(), result);
                  }

                  return  usDocument.getDocument();
              }
        }

        throw new InvalidDocumentIdException("The user does not own a document with such ID.");
    }

    public Void closeDocument(long documentID) throws InvalidDocumentIdException {
        lastOperationTime = new Date().getTime();
        if (!activeDocuments.containsKey(documentID)) {
            throw new InvalidDocumentIdException("The session does not have an active document with such ID.");
        }

        org.hibernate.Session dbSession = HibernateUtil.getSessionWithActiveTransaction();
        activeDocuments.get(documentID).saveToDatabase(dbSession);
        HibernateUtil.closeAndCommitSession(dbSession);

        activeDocuments.remove(documentID);
        activeTranslationResults.remove(documentID);

        return null;
    }
}

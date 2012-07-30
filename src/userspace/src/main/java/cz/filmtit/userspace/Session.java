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
        HibernateUtil.closeAndCommitSession(session);

        user.getActiveDocumentIDs().clear();

        session = HibernateUtil.getSessionWithActiveTransaction();
        for (USDocument activeDoc : activeDocuments.values()) {
            activeDoc.saveToDatabase(session);
            user.getActiveDocumentIDs().add(activeDoc.getDatabaseId());
        }
        HibernateUtil.closeAndCommitSession(session);

        session = HibernateUtil.getSessionWithActiveTransaction();
        user.saveToDatabase(session);
        HibernateUtil.closeAndCommitSession(session);
    }

    /**
     * Kills the session when it times out.
     */
    public void kill() {
        state = SessionState.killed;
        terminate();
    }

    public DocumentResponse createNewDocument(String movieTitle, String year, String language, TranslationMemory TM) {
        updateLastOperationTime();
        USDocument usDocument = new USDocument( new Document(movieTitle, year, language) , user);
        List<MediaSource> suggestions = TM.mediaStorage().getSuggestions(movieTitle, year);

        activeDocuments.put(usDocument.getDatabaseId(), usDocument);

        user.addDocument(usDocument);
       
        

        return new DocumentResponse(usDocument.getDocument(), suggestions);
    }

    public TranslationResult getTranslationResults(TimedChunk chunk, TranslationMemory TM) throws InvalidDocumentIdException {
        updateLastOperationTime();
        if (!activeDocuments.containsKey(chunk.getDocumentId())) {
            throw new InvalidDocumentIdException("Sent time chunk is referring to a document using an invalid ID.");
        }

        USDocument document = activeDocuments.get(chunk.getDocumentId());

        USTranslationResult usTranslationResult = new USTranslationResult(chunk);
        usTranslationResult.setDocument(document);

        usTranslationResult.generateMTSuggestions(TM);
        document.addTranslationResult(usTranslationResult);

        saveTranslationResult(document, usTranslationResult);
        return usTranslationResult.getTranslationResult();
    }

    public Void setUserTranslation(ChunkIndex chunkIndex, long documentId, String userTranslation, long chosenTranslationPairID)
            throws InvalidDocumentIdException, InvalidChunkIdException {
        updateLastOperationTime();

        if (!activeDocuments.containsKey(documentId)) {
            throw new InvalidDocumentIdException("Not existing document ID.");
        }

        USDocument doc = activeDocuments.get(documentId);
        
        USTranslationResult tr = doc.getTranslationResults().get(chunkIndex);
        tr.setUserTranslation(userTranslation);
        tr.setSelectedTranslationPairID(chosenTranslationPairID);
        saveTranslationResult(activeDocuments.get(documentId), tr);
        return null;
    }

    public Void setChunkStartTime(ChunkIndex chunkIndex, long documentId, String newStartTime) throws InvalidDocumentIdException, InvalidChunkIdException {
        updateLastOperationTime();

        if (!activeDocuments.containsKey(documentId)) {
            throw new InvalidDocumentIdException("Not existing document ID.");
        }
        USDocument doc = activeDocuments.get(documentId);
        USTranslationResult tr = doc.getTranslationResults().get(chunkIndex);
        tr.setStartTime(newStartTime);
        saveTranslationResult(doc, tr);
        return  null;
    }

    public Void setChunkEndTime(ChunkIndex chunkIndex, long documentId, String newEndTime) throws InvalidDocumentIdException, InvalidChunkIdException {
        updateLastOperationTime();

        if (!activeDocuments.containsKey(documentId)) {
            throw new InvalidDocumentIdException("Not existing document ID.");
        }
        USDocument doc = activeDocuments.get(documentId);
        USTranslationResult tr = doc.getTranslationResults().get(chunkIndex);

        tr.setEndTime(newEndTime);
        saveTranslationResult(doc, tr);
        return  null;
    }

    public List<TranslationPair> changeText(ChunkIndex chunkIndex, long documentId, String text, TranslationMemory TM)
            throws InvalidDocumentIdException, InvalidChunkIdException {
        updateLastOperationTime();

        if (!activeDocuments.containsKey(documentId)) {
            throw new InvalidDocumentIdException("Not existing document ID.");
        }

        USDocument document = activeDocuments.get(documentId);
        USTranslationResult translationResult = document.getTranslationResults().get(chunkIndex);
        translationResult.setText(text);

        saveTranslationResult(document, translationResult);
        return requestTMSuggestions(chunkIndex, documentId, TM);
    }

    public List<TranslationPair> requestTMSuggestions(ChunkIndex chunkIndex, long documentId, TranslationMemory TM)
            throws InvalidDocumentIdException, InvalidChunkIdException {
        updateLastOperationTime();

        if (!activeDocuments.containsKey(documentId)) {
            throw new InvalidDocumentIdException("Not existing document ID.");
        }
        
        USTranslationResult selected = activeDocuments.get(documentId).getTranslationResults().get(chunkIndex);
        selected.generateMTSuggestions(TM);
        List<TranslationPair> l = new ArrayList<TranslationPair>();
        l.addAll( selected.getTranslationResult().getTmSuggestions());
        
        saveTranslationResult(activeDocuments.get(documentId), selected);
        return l;
    }

    public Void deleteChunk(ChunkIndex chunkIndex, long documentId) throws InvalidDocumentIdException {
        updateLastOperationTime();

        if (!activeDocuments.containsKey(documentId)) {
            throw new InvalidDocumentIdException("Not existing document ID.");
        }

        USDocument document = activeDocuments.get(documentId);
        USTranslationResult translationResult = document.getTranslationResults().get(chunkIndex);

        org.hibernate.Session dbSession = HibernateUtil.getSessionWithActiveTransaction();
        translationResult.deleteFromDatabase(dbSession);
        HibernateUtil.closeAndCommitSession(dbSession);

        activeDocuments.get(documentId).getTranslationResults().remove(chunkIndex);

        return null;
    }

    public Void selectSource(long documentID, MediaSource selectedMediaSource) {
        updateLastOperationTime();
        USDocument usDocument = activeDocuments.get(documentID);
        usDocument.setMovie(selectedMediaSource);
        return null;
    }

    public List<Document> getListOfDocuments() {
        updateLastOperationTime();
        List<Document> result = new ArrayList<Document>();

        for(USDocument usDocument : user.getOwnedDocuments()) {
            result.add(usDocument.getDocument());
  }

        return result;
    }

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

    public Void closeDocument(long documentID) throws InvalidDocumentIdException {
        updateLastOperationTime();
        if (!activeDocuments.containsKey(documentID)) {
            throw new InvalidDocumentIdException("The session does not have an active document with such ID.");
        }

        org.hibernate.Session dbSession = HibernateUtil.getSessionWithActiveTransaction();
        activeDocuments.get(documentID).saveToDatabase(dbSession);
        HibernateUtil.closeAndCommitSession(dbSession);

        activeDocuments.remove(documentID);
        
        saveAllTranslationResults(activeDocuments.get(documentID));
        return null;
    }
    

    public void saveAllTranslationResults(long l) {
        saveAllTranslationResults(activeDocuments.get(l));
    }

    public void saveAllTranslationResults(USDocument document) {
        Collection<USTranslationResult> results = document.getTranslationResults().values();
        saveTranslationResults(document, results);
    }

    public void saveTranslationResult(USDocument document, USTranslationResult result) {
       ArrayList<USTranslationResult> al = new ArrayList<USTranslationResult>(1);
       al.add(result);
       saveTranslationResults(document, al);
    }

    public void saveTranslationResults(USDocument document, Collection<USTranslationResult> results) {
        org.hibernate.Session session = HibernateUtil.getSessionWithActiveTransaction();
        document.saveToDatabase(session);

        for (USTranslationResult tr : results) {

            document.addOrReplaceTranslationResult(tr);
            tr.saveToDatabase(session); 
                        
        }
        HibernateUtil.closeAndCommitSession(session); 
    }

    private void updateLastOperationTime() {
        lastOperationTime = new Date().getTime();
    }
}

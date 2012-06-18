package cz.filmtit.userspace;

import cz.filmtit.core.model.TranslationMemory;
import cz.filmtit.share.*;
import cz.filmtit.share.exceptions.InvalidDocumentIdException;

import java.util.*;

/**
 * Represents a running session.
 * @author Jindřich Libovický
 */
public class Session {
    private int sessionId;
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

    public Session() {
        activeDocuments = Collections.synchronizedMap(new HashMap<Long, USDocument>());
        activeTranslationResults = Collections.synchronizedMap(new HashMap<Long, Map<Integer, USTranslationResult>>());
        lastOperationTime = new Date().getTime();
        state = SessionState.active;
    }

    public long getLastOperationTime() {
        return lastOperationTime;
    }

    public void setLastOperationTime(long lastOperationTime) {
        this.lastOperationTime = lastOperationTime;
    }

    public USUser getUser() {
        return user;
    }

    public void logout() {
        state = SessionState.loggedOut;
        terminate();
    }

    /**
     * Terminates the session. Usually in the situation when the user open a new one.
     */
    private void terminate() {
        
        org.hibernate.Session session = HibernateUtil.getCurrentSession();

        session.save(this);

        user.saveToDatabase(session);
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
        //user TODO: add the document to the correct user


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
            throw new InvalidDocumentIdException("Sent time chunk is refering to a document using an invalid ID.");
        }

        USDocument document = activeDocuments.get(chunk.getDocumentId());

        USTranslationResult usTranslationResult = new USTranslationResult(chunk);
        usTranslationResult.setParent(document);



        usTranslationResult.generateMTSuggestions(TM);

        activeTranslationResults.get(document.getDatabaseId()).put(chunk.getId(), usTranslationResult);
        return usTranslationResult.getTranslationResult();
    }

    public Void setUserTranslation(int chunkId, long documentId, String userTranslation, long chosenTranslationPairID) {
        lastOperationTime = new Date().getTime();
        USTranslationResult tr = activeTranslationResults.get(documentId).get(chunkId);
        tr.setUserTranslation(userTranslation);
        tr.setSelectedTranslationPairID(chosenTranslationPairID);
        return null;
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
                  return  usDocument.getDocument();
              }
        }
        throw new InvalidDocumentIdException("The user does not own a document with such ID.");
    }
}

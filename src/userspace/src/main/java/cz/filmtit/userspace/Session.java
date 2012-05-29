package cz.filmtit.userspace;

import cz.filmtit.core.model.TranslationMemory;
import cz.filmtit.share.Document;
import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.TranslationResult;
import cz.filmtit.share.exceptions.InvalidDocumentIdException;

import java.util.HashMap;
import java.util.Map;

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

    enum SessionState {active, loggedOut, terminated, kill}

    /**
     * Cache hash table for active documents owned by current user, to make them quickly available using their IDs.
     */
    private Map<Long, USDocument> activeDocuments;
    /**
     * Cache hash table for translation results belonging to active documents of the current user
     */
    private Map<Long, Map<Integer, USTranslationResult>> activeTranslationResults;

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
        org.hibernate.Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        session.save(this);

        user.saveToDatabase(session);
        for (USDocument activeDoc : activeDocuments.values()) {
            activeDoc.saveToDatabase(session);
        }

        session.getTransaction().commit();
    }

    /**
     * Kills the session when it times out.
     */
    public void Kill() {
        // save everything to database and write a log of this session
    }


    public Document createDocument(String movieTitle, String year, String language) {
        USDocument usDocument = new USDocument( new Document(movieTitle, year, language) );

        activeDocuments.put(usDocument.getDatabaseId(), usDocument);
        activeTranslationResults.put(usDocument.getDatabaseId(), new HashMap<Integer, USTranslationResult>());
        //user TODO: add the document to the correct user


        return usDocument.getDocument();
    }

    public TranslationResult getTranslationResults(TimedChunk chunk, TranslationMemory TM) throws InvalidDocumentIdException {
        if (!activeDocuments.containsKey(chunk.getDocumentId())) {
            throw new InvalidDocumentIdException("Sent time chunk is refering to a document using an invalid ID.");
        }

        USDocument document = activeDocuments.get(chunk.getDocumentId());

        USTranslationResult usTranslationResult = new USTranslationResult(chunk);
        usTranslationResult.setParent(document);

        // TODO: STORE THE CHUNK IN THE DOCUMENT

        usTranslationResult.generateMTSuggestions(TM);

        activeTranslationResults.get(document.getDatabaseId()).put(chunk.getId(), usTranslationResult);
        return usTranslationResult.getTranslationResult();
    }
}

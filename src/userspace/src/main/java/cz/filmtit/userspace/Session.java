package cz.filmtit.userspace;

/*
    - whenever a JSON message comes, update the lastOperation
    - pass further all the JSON messages except logging out
 */

import cz.filmtit.core.model.TranslationMemory;
import cz.filmtit.share.Document;
import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.TranslationResult;

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
    private long lastOperation;
    private SessionState state;

    enum SessionState {active, loggedOut, terminated, kill}

    private Map<Long, USDocument> activeDocuments;
    private Map<Long, Map<Integer, USTranslationResult>> activeTranslationResults;

    public long getLastOperation() {
        return lastOperation;
    }

    public void setLastOperation(long lastOperation) {
        this.lastOperation = lastOperation;
    }

    public USUser getUser() {
        return user;
    }

    public void Logout() {
        state = SessionState.loggedOut;
        // ...
    }

    /**
     * Terminates the session. Usually in the situation when the user open a new one.
     */
    public void Terminate() {
        // save everything to database and write a log of this session
    }

    /**
     * Kills the session when it times out.
     */
    public void Kill() {
        // save everything to database and write a log of this session
    }

    /**
     * Writes log of this session to the database.
     */
    public void saveToDatabase() {
        org.hibernate.Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        session.save(this);
        session.getTransaction().commit();
    }

    public Document createDocument(String movieTitle, String year, String language) {
        USDocument usDocument = new USDocument( new Document(movieTitle, year, language) );

        activeDocuments.put(usDocument.getDatabaseId(), usDocument);
        activeTranslationResults.put(usDocument.getDatabaseId(), new HashMap<Integer, USTranslationResult>());
        //user TODO: add the document to the correct user


        return usDocument.getDocument();
    }

    public TranslationResult getTranslationResults(TimedChunk chunk, TranslationMemory TM) {
        //this looks terribly unsafe, nothing is checked here
        USDocument document = activeDocuments.get(chunk.getDocumentId());
        USTranslationResult usTranslationResult = new USTranslationResult(chunk);
        usTranslationResult.setParent(document);

        usTranslationResult.generateMTSuggestions(TM);

        // TODO: make maps of maps to deal with the two ID policy
        activeTranslationResults.put(usTranslationResult.getDatabaseId(), usTranslationResult);

        return usTranslationResult.getTranslationResult();
    }
}

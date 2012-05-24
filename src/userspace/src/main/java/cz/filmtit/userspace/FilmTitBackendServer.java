package cz.filmtit.userspace;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import cz.filmtit.core.ConfigurationSingleton;
import cz.filmtit.core.Configuration;
import cz.filmtit.core.Factory;
import cz.filmtit.core.model.TranslationMemory;
import cz.filmtit.share.Document;
import cz.filmtit.share.FilmTitService;
import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.TranslationResult;
import cz.filmtit.share.exceptions.InvalidSessionId;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FilmTitBackendServer extends RemoteServiceServlet implements
		FilmTitService {
	
	private static final long serialVersionUID = 3546115L;
    private static long SESSION_TIME_OUT_LIMIT = ConfigurationSingleton.getConf().sessionTimeout();

	private TranslationMemory TM;
    private Map<Long, USDocument> activeDocuments;                  // delete ASAP sessions introduced
    private Map<Long, USTranslationResult> activeTranslationResults; // delete ASAP sessions introduced
    private Map<String, Session> activeSessions;

    public FilmTitBackendServer(/*Configuration configuration*/) {
        TM = Factory.createTM(ConfigurationSingleton.getConf(), true);

        activeDocuments = Collections.synchronizedMap(new HashMap<Long, USDocument>());
        activeTranslationResults = Collections.synchronizedMap(new HashMap<Long, USTranslationResult>());

        new WatchSessionTimeOut().run(); // runs deleting timed out sessions

        System.err.println("FilmTitBackendServer started fine!");
    }

	/*public FilmTitBackendServer() throws Exception {
        this(new Configuration(new File("/filmtit/git/FilmTit/src/configuration.xml")));
    }*/

    public TranslationMemory getTM() {
        return TM;
    }

	public TranslationResult getTranslationResults(TimedChunk chunk) {
		//this looks terribly unsafe, nothing is checked here
        USDocument docu = activeDocuments.get(chunk.getDocumentId());
        USTranslationResult usTranslationResult = new USTranslationResult(chunk);
        usTranslationResult.setParent(docu);

        usTranslationResult.generateMTSuggestions(TM);

        // TODO: make maps of maps to deal with the two ID policy
        activeTranslationResults.put(usTranslationResult.getDatabaseId(), usTranslationResult);

        return usTranslationResult.getTranslationResult();
	}

    public TranslationResult getTranslationResult(String sessionId, TimedChunk chunk) throws InvalidSessionId {
        if (!activeSessions.containsKey(sessionId)) {
            throw new InvalidSessionId("Session ID expired or invalid.");
        }

        return null;
    }

    @Override
    public Void setUserTranslation(int chunkId, long documentId, String userTranslation, long chosenTranslationPairID) {
        //USTranslationResult tr = activeTranslationResults.get(translationResultId);
        //tr.setUserTranslation(userTranslation);
        //tr.setSelectedTranslationPairID(chosenTranslationPairID);

        return null;
    }

	public Document createDocument(String movieTitle, String year, String language) {
		USDocument usDocument = new USDocument( new Document(movieTitle, year, language) );
		activeDocuments.put(usDocument.getDatabaseId(), usDocument);
        return usDocument.getDocument();
	}

    public Document createDocument(String sessionId, String movieTitle, String year, String language) throws InvalidSessionId {
        if (!activeSessions.containsKey(sessionId)) {
            throw new InvalidSessionId("Session ID expired or invalid.");
        }

        return activeSessions.get(sessionId).createDocument(year, year, language);
    }

    /**
     * A thread that checks out whether the sessions should be timed out.
     */
    class WatchSessionTimeOut extends Thread {
        public void run() {
            while(true) {
                for (String sessionId : activeSessions.keySet()) {
                    long now = new Date().getTime();
                    Session thisSession = activeSessions.get(sessionId);
                    if (thisSession.getLastOperation() + SESSION_TIME_OUT_LIMIT < now) {
                        activeSessions.remove(thisSession.getUser());
                        thisSession.Kill();
                        activeSessions.remove(sessionId);
                    }
                }
                try { Thread.sleep(60 * 1000); }
                catch (Exception e) {}
            }
        }
    }

}

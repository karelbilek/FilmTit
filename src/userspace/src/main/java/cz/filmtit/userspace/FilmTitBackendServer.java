package cz.filmtit.userspace;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import cz.filmtit.core.ConfigurationSingleton;
import cz.filmtit.core.Factory;
import cz.filmtit.core.model.TranslationMemory;
import cz.filmtit.share.*;
import cz.filmtit.share.exceptions.InvalidSessionIdException;
import org.expressme.openid.Association;
import org.expressme.openid.Endpoint;
import org.expressme.openid.OpenIdManager;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FilmTitBackendServer extends RemoteServiceServlet implements
        FilmTitService {

    private static final long serialVersionUID = 3546115L;
    private static long SESSION_TIME_OUT_LIMIT = ConfigurationSingleton.getConf().sessionTimeout();
    private static int SESSION_ID_LENGHT = 47;

    protected TranslationMemory TM;
    private Map<Long, USDocument> activeDocuments;                   // delete ASAP sessions introduced
    private Map<Long, USTranslationResult> activeTranslationResults; // delete ASAP sessions introduced

    private Map<Long, Session> authenticatingSessions = new HashMap<Long, Session>();
    private Map<String, Session> activeSessions = new HashMap<String,Session>();

    public FilmTitBackendServer(/*Configuration configuration*/) {



        activeDocuments = Collections.synchronizedMap(new HashMap<Long, USDocument>());
        activeTranslationResults = Collections.synchronizedMap(new HashMap<Long, USTranslationResult>());

        new WatchSessionTimeOut().start(); // runs deleting timed out sessions

        System.err.println("FilmTitBackendServer started fine!");
    }

    protected void loadTranslationMemory() {
        TM = Factory.createTMFromConfiguration(
                ConfigurationSingleton.getConf(),
                false, // readonly
                false  // in memory
        );
    }

    public TranslationMemory getTM() {
        return TM;
    }

    public TranslationResult getTranslationResults(TimedChunk chunk) {
        //this looks terribly unsafe, nothing is checked here
        USDocument docu = activeDocuments.get(chunk.getDocumentId());
        USTranslationResult usTranslationResult = new USTranslationResult(chunk);
        usTranslationResult.setParent(docu);

        usTranslationResult.generateMTSuggestions(TM);

        activeTranslationResults.put(usTranslationResult.getDatabaseId(), usTranslationResult);

        return usTranslationResult.getTranslationResult();
    }

    public TranslationResult getTranslationResults(String sessionId, TimedChunk chunk) throws InvalidSessionIdException {
        if (!activeSessions.containsKey(sessionId)) {
            throw new InvalidSessionIdException("Session ID expired or invalid.");
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

    @Override
    public String getAuthenticationURL(long authID, AuthenticationServiceType serviceType) {
        OpenIdManager manager = new OpenIdManager();

        String serverAddress = ConfigurationSingleton.getConf().serverAddress();
        manager.setReturnTo(serverAddress + "/openId");
        manager.setRealm(serverAddress);
        // TODO: the manager should be stored for further use in  validateAuthentication method

        authenticatingSessions.put(authID, null);

        // TODO: Add the service type resolving
        Endpoint endpoint = manager.lookupEndpoint("Google");
        Association association = manager.lookupAssociation(endpoint);

        return association.toString();
    }

    @Override
    public Boolean validateAuthentication(long authID, String responseURL) {

        //HttpServletRequest request = createRequest(responseURL);
        //Authentication authentication = manager.getAuthentication(request, association.getRawMacKey());
        //authentication.getIdentity() <- this will be as user identification

        return null;
    }

    @Override
    public String getSessionID(long authID) {
        if (authenticatingSessions.containsKey(authID) && authenticatingSessions.get(authID) != null) {
            Session session = authenticatingSessions.get(authID);
            authenticatingSessions.remove(authID);

            String newSessionID = (new IdGenerator().generateId(SESSION_ID_LENGHT));
            activeSessions.put(newSessionID, session);

            return newSessionID;
        }
        return null;
    }

    public Document createDocument(String sessionId, String movieTitle, String year, String language) throws InvalidSessionIdException {
        if (!activeSessions.containsKey(sessionId)) {
            throw new InvalidSessionIdException("Session ID expired or invalid.");
        }

        return activeSessions.get(sessionId).createDocument(year, year, language);
    }

    public Void logout(String sessionId) throws InvalidSessionIdException {
        if (!activeSessions.containsKey(sessionId)) {
            throw new InvalidSessionIdException("Session ID expired or invalid.");
        }

        activeSessions.get(sessionId).logout();
        activeSessions.remove(sessionId);

        return null;
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
                    if (thisSession.getLastOperationTime() + SESSION_TIME_OUT_LIMIT < now) {
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
package cz.filmtit.userspace;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import cz.filmtit.core.ConfigurationSingleton;
import cz.filmtit.core.Factory;
import cz.filmtit.core.model.TranslationMemory;
import cz.filmtit.share.*;
import cz.filmtit.share.exceptions.InvalidDocumentIdException;
import cz.filmtit.share.exceptions.InvalidSessionIdException;
import org.expressme.openid.Association;
import org.expressme.openid.Authentication;
import org.expressme.openid.Endpoint;
import org.expressme.openid.OpenIdManager;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URLDecoder;
import java.util.*;

public class FilmTitBackendServer extends RemoteServiceServlet implements
        FilmTitService {

    private static final long serialVersionUID = 3546115L;
    private static long SESSION_TIME_OUT_LIMIT = ConfigurationSingleton.getConf().sessionTimeout();
    private static int SESSION_ID_LENGHT = 47;


    protected TranslationMemory TM;
    private Map<Long, USDocument> activeDocuments;                   // delete ASAP sessions introduced
    private Map<Long, Map<Integer, USTranslationResult>> activeTranslationResults;  // delete ASAP sessions introduced

    // AuthId which are in process
    private Map<Long, AuthData> authenticatingSessions = new HashMap<Long, AuthData>();
    // AuthId which are authenticated but not activated
    private Map<Long,Authentication> authenticatedSessions = new HashMap<Long, Authentication>();
    // Activated User
    private Map<String, Session> activeSessions = new HashMap<String,Session>();

    protected OpenIdManager  manager;
    public FilmTitBackendServer(/*Configuration configuration*/) {

        loadTranslationMemory();

        activeDocuments = Collections.synchronizedMap(new HashMap<Long, USDocument>());
        activeTranslationResults = Collections.synchronizedMap(new HashMap<Long, Map<Integer, USTranslationResult>>());
        String serverAddress = ConfigurationSingleton.getConf().serverAddress();
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

        activeTranslationResults.get(docu.getDatabaseId()).put(chunk.getId(), usTranslationResult);
        return usTranslationResult.getTranslationResult();
    }


    public List<TranslationResult> getTranslationResults(List<TimedChunk> chunks) {
        List<TranslationResult> res = new ArrayList<TranslationResult>(chunks.size());
        
        for (TimedChunk timedchunk:chunks) {
            res.add(getTranslationResults(timedchunk));
        }
        return res;   
    }

    public TranslationResult getTranslationResults(String sessionId, TimedChunk chunk) throws InvalidSessionIdException {
        if (!activeSessions.containsKey(sessionId)) {
            throw new InvalidSessionIdException("Session ID expired or invalid.");
        }
        return null;
    }

    @Override
    public Void setUserTranslation(int chunkId, long documentId, String userTranslation, long chosenTranslationPairID) {
        USTranslationResult tr=null;  
        try {
            tr = activeTranslationResults.get(documentId).get(chunkId);
            tr.setUserTranslation(userTranslation);
            tr.setSelectedTranslationPairID(chosenTranslationPairID);
        } catch (NullPointerException e) {
            System.err.println("Nullpointerexception "+e);
            return null;
        }

        // a Session free temporary saving solution
        org.hibernate.Session dbSession = HibernateUtil.getCurrentSession();

        tr.saveToDatabase(dbSession);

        
        HibernateUtil.closeAndCommitSession(dbSession);

        return null;
    }

    public Void setUserTranslation(String sessionId, int chunkId, long documentId, String userTranslation, long chosenTranslationPairID)
            throws InvalidSessionIdException {
        if (!activeSessions.containsKey(sessionId)) {
            throw new InvalidSessionIdException("Session ID expired or invalid.");
        }
        return activeSessions.get(sessionId).setUserTranslation(chunkId, documentId, userTranslation, chosenTranslationPairID);
    }

    @Override
    public Document createDocument(String movieTitle, String year, String language) {
        USDocument usDocument = new USDocument( new Document(movieTitle, year, language) );
        activeDocuments.put(usDocument.getDatabaseId(), usDocument);
        activeTranslationResults.put(usDocument.getDatabaseId(), Collections.synchronizedMap(new HashMap<Integer, USTranslationResult>()));
        return usDocument.getDocument();
    }

    @Override
    public DocumentResponse createNewDocument(String movieTitle, String year, String language) {
        USDocument usDocument = new USDocument( new Document(movieTitle, year, language) );
        List<MediaSource> suggestions = TM.mediaStorage().getSuggestions(movieTitle, year);

        activeDocuments.put(usDocument.getDatabaseId(), usDocument);
        activeTranslationResults.put(usDocument.getDatabaseId(), Collections.synchronizedMap(new HashMap<Integer, USTranslationResult>()));

        return new DocumentResponse(usDocument.getDocument(), suggestions);
    }

    public DocumentResponse createNewDocument(String sessionId, String movieTitle, String year, String language) throws InvalidSessionIdException {
        if (!activeSessions.containsKey(sessionId)) {
            throw new InvalidSessionIdException("Session ID expired or invalid.");
        }
        return activeSessions.get(sessionId).createNewDocument(movieTitle, year, language, TM);
    }

    @Override
    public Void selectSource(long documentID, MediaSource selectedMediaSource) {
        USDocument usDocument = activeDocuments.get(documentID);
        usDocument.setMovie(selectedMediaSource);
        return null;
    }

    public Void selectSource(String sessionId, long documentID, MediaSource selectedMediaSource) throws InvalidSessionIdException {
        if (!activeSessions.containsKey(sessionId)) {
            throw new InvalidSessionIdException("Session ID expired or invalid.");
        }
        return activeSessions.get(sessionId).selectSource(documentID, selectedMediaSource);
    }

    public List<Document> getListOfDocuments(String sessionId) throws InvalidSessionIdException {
        if (!activeSessions.containsKey(sessionId)) {
            throw new InvalidSessionIdException("Session ID expired or invalid.");
        }
        return activeSessions.get(sessionId).getListOfDocuments();
    }

    public Document loadDocument(String sessionId, long documentID) throws InvalidDocumentIdException, InvalidSessionIdException {
        if (!activeSessions.containsKey(sessionId)) {
            throw new InvalidSessionIdException("Session ID expired or invalid.");
        }
        return activeSessions.get(sessionId).loadDocument(documentID);
    }

    @Override
    public String getAuthenticationURL(long authID, AuthenticationServiceType serviceType) {

        // Manager is field in class it seem  quite non useful copy 1 object 1000x
        // add setting in constructor
        // TODO: the manager should be stored for further use in  validateAuthentication method
        // TODO: Add the service type resolving   -  is enough send  name of service like enum
        // lib is open source and we can added for example seznam or myid

        Endpoint endpoint = manager.lookupEndpoint("Google");
        Association association = manager.lookupAssociation(endpoint);
         // create url for authentication
        // need to raw data for getting authentication
        AuthData authData = new AuthData();
        authData.Mac_key = association.getRawMacKey();
        authData.endpoint = endpoint;
        authenticatingSessions.put(authID, authData);
        return manager.getAuthenticationUrl(endpoint,association);
    }

    @Override
    public Boolean validateAuthentication(long authID, String responseURL) {
        //  response url - one if you succesfull with login
        //                  sec if you are not
        // if you are you can create authentication object which contains  information
        // using http://code.google.com/p/jopenid/source/browse/trunk/JOpenId/src/test/java/org/expressme/openid/MainServlet.java?r=111&spec=svn111
        //HttpServletRequest request = createRequest(responseURL);
        //Authentication authentication = manager.getAuthentication(request, association.getRawMacKey());
        //authentication.getIdentity() <- this will be as user identification

        try {
            AuthData authData = authenticatingSessions.get(authID);
            HttpServletRequest request = FilmTitBackendServer.createRequest(responseURL);
            Authentication authentication;
            authentication = manager.getAuthentication(request,authData.Mac_key, authData.endpoint.getAlias());
            authenticatedSessions.put(authID,authentication);


        } catch (UnsupportedEncodingException e) {
           return false;
        }


        return null;
    }

    @Override
    public String getSessionID(long authID) {
        if (authenticatedSessions.containsKey(authID) && authenticatedSessions.get(authID) != null) {
            Authentication authentication = authenticatedSessions.get(authID);
            authenticatingSessions.remove(authID);
            // USUser user = createUser(authentication);          create user from auth information (select or create in db)
            String newSessionID = (new IdGenerator().generateId(SESSION_ID_LENGHT));
            Session session = new Session(); //= createSession(newSessionID,user)      create session with user
            activeSessions.put(newSessionID, session);

            return newSessionID;
        }
        return null;
    }

    public Void logout(String sessionId) throws InvalidSessionIdException {
        if (!activeSessions.containsKey(sessionId)) {
            throw new InvalidSessionIdException("Session ID expired or invalid.");
        }

        activeSessions.get(sessionId).logout();
        activeSessions.remove(sessionId);

        return null;
    }

    static HttpServletRequest createRequest(String url) throws UnsupportedEncodingException {
        int pos = url.indexOf('?');
        if (pos==(-1))
            throw new IllegalArgumentException("Bad url.");
        String query = url.substring(pos + 1);
        String[] params = query.split("[\\&]+");
        final Map<String, String> map = new HashMap<String, String>();
        for (String param : params) {
            pos = param.indexOf('=');
            if (pos==(-1))
                throw new IllegalArgumentException("Bad url.");
            String key = param.substring(0, pos);
            String value = param.substring(pos + 1);
            map.put(key, URLDecoder.decode(value, "UTF-8"));
        }
        return (HttpServletRequest) Proxy.newProxyInstance(
               FilmTitBackendServer.class.getClassLoader(),
                new Class[]{HttpServletRequest.class},
                new InvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (method.getName().equals("getParameter"))
                            return map.get((String) args[0]);
                        throw new UnsupportedOperationException(method.getName());
                    }
                }
        );
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
                        thisSession.kill();
                        activeSessions.remove(sessionId);
                    }
                }
                try { Thread.sleep(60 * 1000); }
                catch (Exception e) {}
            }
        }
    }

    class AuthData
    {
        public byte[] Mac_key;
        public Endpoint endpoint;

    }
}

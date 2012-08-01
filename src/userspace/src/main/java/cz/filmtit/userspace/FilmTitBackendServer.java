package cz.filmtit.userspace;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import cz.filmtit.core.Configuration;
import cz.filmtit.core.ConfigurationSingleton;
import cz.filmtit.core.CoreHibernateUtil;
import cz.filmtit.core.Factory;
import cz.filmtit.core.io.data.FreebaseMediaSourceFactory;
import cz.filmtit.core.model.MediaSourceFactory;
import cz.filmtit.core.model.TranslationMemory;
import cz.filmtit.share.*;
import cz.filmtit.share.exceptions.InvalidChunkIdException;
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
    private static long SESSION_TIME_OUT_LIMIT = ConfigurationSingleton.conf().sessionTimeout();
    private static int SESSION_ID_LENGHT = 47;


    private static CoreHibernateUtil coreHibernateUtil = CoreHibernateUtil.getInstance();
    protected static USHibernateUtil usHibernateUtil = USHibernateUtil.getInstance();

    private enum CheckUserEnum {
        UserName,
        UserNamePass
    }

    protected TranslationMemory TM;
    protected MediaSourceFactory mediaSourceFactory;
    protected Configuration configuration;

    // AuthId which are in process
    private Map<Long, AuthData> authenticatingSessions =
            Collections.synchronizedMap(new HashMap<Long, AuthData>());
    // AuthId which are authenticated but not activated
    private Map<Long,Authentication> authenticatedSessions =
            Collections.synchronizedMap(new HashMap<Long, Authentication>());
    // Activated User
    private Map<String, Session> activeSessions =
            Collections.synchronizedMap(new HashMap<String,Session>());

    protected OpenIdManager manager = new OpenIdManager();
    public FilmTitBackendServer() {
        configuration = ConfigurationSingleton.conf();

        loadTranslationMemory();

        mediaSourceFactory = new FreebaseMediaSourceFactory(configuration.freebaseKey(), 10);

        String serverAddress = configuration.serverAddress();
        new WatchSessionTimeOut().start(); // runs deleting timed out sessions

        // set up the OpenID returning address
        // localhost hack
        //serverAddress = "localhost:8080/";
        manager.setReturnTo(serverAddress + "?page=AuthenticationValidationWindow");
        manager.setRealm(serverAddress);

        // initialize the database by opening and closing a session
        org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();
        usHibernateUtil.closeAndCommitSession(dbSession);

        // initialize also the core part of hibernate
        dbSession = coreHibernateUtil.getSessionWithActiveTransaction();
        coreHibernateUtil.closeAndCommitSession(dbSession);

        System.err.println("FilmTitBackendServer started fine!");
    }

    protected void loadTranslationMemory() {
        TM = Factory.createTMFromConfiguration(
                ConfigurationSingleton.conf(),
                true, // readonly
                false  // in memory
        );

        new Thread(new Runnable() {
            public void run() {
                TM.warmup();
            }
        }).start();

    }

    public TranslationMemory getTM() {
        return TM;
    }

    public TranslationResult getTranslationResults(String sessionID, TimedChunk chunk)
            throws InvalidSessionIdException, InvalidDocumentIdException {
        return getSessionIfCan(sessionID).getTranslationResults(chunk, TM);
    }

    public List<TranslationResult> getTranslationResults(String sessionID, List<TimedChunk> chunks)
            throws InvalidSessionIdException, InvalidDocumentIdException {
        Session session = getSessionIfCan(sessionID);

        List<TranslationResult> res = ParallelHelper.getTranslationsParallel(chunks, session, TM);
        session.saveAllTranslationResults(chunks.get(0).getDocumentId());
        return res;
    }

    public Void setUserTranslation(String sessionID, ChunkIndex chunkIndex, long documentId,
                                   String userTranslation, long chosenTranslationPairID)
            throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException {
        return getSessionIfCan(sessionID).setUserTranslation(chunkIndex, documentId, userTranslation, chosenTranslationPairID);
    }

    public Void setChunkStartTime(String sessionID, ChunkIndex chunkIndex, long documentId, String newStartTime)
            throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException {
        return getSessionIfCan(sessionID).setChunkStartTime(chunkIndex, documentId, newStartTime);
    }

    public Void setChunkEndTime(String sessionID, ChunkIndex chunkIndex, long documentId, String newEndTime)
            throws InvalidDocumentIdException, InvalidChunkIdException, InvalidSessionIdException {
        return getSessionIfCan(sessionID).setChunkEndTime(chunkIndex, documentId, newEndTime);
    }

    public List<TranslationPair> changeText(String sessionID, ChunkIndex chunkIndex, long documentId, String newText)
            throws InvalidChunkIdException, InvalidDocumentIdException, InvalidSessionIdException {
        return getSessionIfCan(sessionID).changeText(chunkIndex, documentId, newText, TM);
    }

    public List<TranslationPair> requestTMSuggestions(String sessionID, ChunkIndex chunkIndex, long documentId)
            throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException {
        return getSessionIfCan(sessionID).requestTMSuggestions(chunkIndex, documentId, TM);
    }

    public Void deleteChunk(String sessionID, ChunkIndex chunkIndex, long documentId)
            throws InvalidSessionIdException, InvalidDocumentIdException, InvalidChunkIdException {
        return getSessionIfCan(sessionID).deleteChunk(chunkIndex, documentId);
    }

    public DocumentResponse createNewDocument(String sessionID, String documentTitle, String movieTitle, String language)
            throws InvalidSessionIdException {
         return getSessionIfCan(sessionID).createNewDocument(documentTitle, movieTitle, language, mediaSourceFactory);
    }

    public Void selectSource(String sessionID, long documentID, MediaSource selectedMediaSource)
            throws InvalidSessionIdException, InvalidDocumentIdException {
        return getSessionIfCan(sessionID).selectSource(documentID, selectedMediaSource);
    }

    public List<Document> getListOfDocuments(String sessionID) throws InvalidSessionIdException {
        return getSessionIfCan(sessionID).getListOfDocuments();
    }

    public Document loadDocument(String sessionID, long documentID)
            throws InvalidDocumentIdException, InvalidSessionIdException {
        return getSessionIfCan(sessionID).loadDocument(documentID);
    }

    public Void closeDocument(String sessionID, long documentId)
            throws InvalidSessionIdException, InvalidDocumentIdException {
        return getSessionIfCan(sessionID).closeDocument(documentId);
    }

    @Override
    public String getAuthenticationURL(long authID, AuthenticationServiceType serviceType) {

        // TODO: Add the service type resolving   -  is enough send  name of service like enum
        // lib is open source and we can added for example seznam or myid

        configuration = ConfigurationSingleton.conf();
        String serverAddress = configuration.serverAddress();
        manager.setReturnTo(serverAddress + "?page=AuthenticationValidationWindow&authID=" + authID);
        
        Endpoint endpoint = manager.lookupEndpoint("Google");
        Association association = manager.lookupAssociation(endpoint);
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
            Session session = new Session(null); //= createSession(newSessionID,user)      create session with user
            activeSessions.put(newSessionID, session);

            return newSessionID;
        }
        return null;
    }

    public String simple_login(String username, String password) {
        if (password.equals("guest")) {
        	// TODO: this branch must be removed once login works properly!!!
        	USUser user = new USUser(username);
            String newSessionID = (new IdGenerator().generateId(SESSION_ID_LENGHT));
            Session session = new Session(user);
            activeSessions.put(newSessionID, session);

            return newSessionID;
        }
        else {
            USUser user = checkUser(username,password,CheckUserEnum.UserNamePass);
            if (user == null)
            {
                return  "";
            }
            else {
                String newSessionID = (new IdGenerator().generateId(SESSION_ID_LENGHT));
                Session session = new Session(user);
                activeSessions.put(newSessionID, session);
                return newSessionID;
            }

        }
    }

    public Void logout(String sessionID) throws InvalidSessionIdException {
        Session session = getSessionIfCan(sessionID);

        session.logout();
        activeSessions.remove(sessionID);

        return null;
    }

    public Boolean  registration(String name ,  String pass  , String email, String openId) {
          // create user

         USUser check = checkUser(name,pass,CheckUserEnum.UserName);
         if (check == null)
         {
             String hash = pass_hash(pass);
             USUser user = new USUser(name,hash,email,openId);
          // create hibernate session
             org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();

          // save into db
             user.saveToDatabase(dbSession);
            usHibernateUtil.closeAndCommitSession(dbSession);
         return true;
         }
        return false;
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
     * Get hash of string
     * if return same string like input - problem with algortithm
     */
    public static String pass_hash(String pass)
    {
       return BCrypt.hashpw(pass,BCrypt.gensalt(12));
    }
    /**
     * Check if found sessionId is still active
     */
    public String checkSessionID(String sessionID)
    {
       if (activeSessions.isEmpty())
       {
           return null;     // no session at all
       }
        Session s = activeSessions.get(sessionID);
        if (s == null) return null;   // not found sessionId
        return s.getUser().getUserName(); // return name of user who had  session

     }

    /**
     * Check if user exists
     *  switch according CheckUserEnum
     *     CheckName - return first user of given name
     *     CheckNamePass - return user with given name and pass
     */
    private  USUser checkUser(String username , String password, CheckUserEnum type)
    {
        org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();

        List UserResult = dbSession.createQuery("select d from USUser d where d.userName like :username")
                .setParameter("username",username).list(); //UPDATE hibernate  for more constraints

        USUser succesUser = null;
        int count= 0;
        if (type == CheckUserEnum.UserNamePass)
        {
            usHibernateUtil.closeAndCommitSession(dbSession);
            for (Object aUserResult : UserResult) {
                USUser user = (USUser) aUserResult;
                if (BCrypt.checkpw(password, user.getPassword())) {
                    succesUser = user;
                    count++;
                }
            }

            if (count > 1)
            {
                throw new ExceptionInInitializerError("Two users with same name and passwords");
            }
        }
        else if (type == CheckUserEnum.UserName)
        {
            if (!UserResult.isEmpty())
            {
                succesUser=(USUser)UserResult.get(0);
            }

        }
    return succesUser;
    }
    /**
     * A thread that checks out whether the sessions should be timed out.
     */
    class WatchSessionTimeOut extends Thread {
        public void run() {
            while(true) {
                for (String sessionID : activeSessions.keySet()) {
                    long now = new Date().getTime();
                    Session thisSession = activeSessions.get(sessionID);
                    if (thisSession.getLastOperationTime() + SESSION_TIME_OUT_LIMIT < now) {
                        activeSessions.remove(thisSession.getUser());
                        thisSession.kill();
                        activeSessions.remove(sessionID);
                    }
                }
                try { Thread.sleep(60 * 1000); }
                catch (Exception e) {}
            }
        }
    }



    class AuthData {
        public byte[] Mac_key;
        public Endpoint endpoint;
    }


	@Override
	public Boolean sendChangePasswordMail(String username) {
		// TODO nahradit skuteÄŤnou metodou
		System.out.println("called sendChangePasswordMail, username = " + username);
		return null;
	}

	@Override
	public Boolean changePassword(String username, String password, String token) {
		// TODO nahradit skuteÄŤnou metodou
		System.out.println("called changePassword, username = " + username + ", password = " + password + ", toekn = " + token);
		return null;
	}

    private Session getSessionIfCan(String sessionId) throws InvalidSessionIdException {
        if (!activeSessions.containsKey(sessionId)) {
            throw new InvalidSessionIdException("Session ID expired or invalid.");
        }

        return activeSessions.get(sessionId);
    }

	@Override
	public String exportSubtitles(String sessionID, long documentID) {
		// TODO Auto-generated method stub

		// preliminary version:
		// just return the subtitles as a String
		
		return "1\n" +
		"00:02:46,039 --> 00:02:47,081\n" +
		"So...?\n" +
		"\n" +
		"2\n" +
		"00:02:47,291 --> 00:02:49,957\n" +
		"Well, professor, I was hoping I could talk to you about your research?\n" +
		"\n" +
		"3\n" +
		"00:02:50,165 --> 00:02:55,582\n" +
		"What for? It's all on my webpage.\n";
	}

}

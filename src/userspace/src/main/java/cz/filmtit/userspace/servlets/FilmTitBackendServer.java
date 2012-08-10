package cz.filmtit.userspace.servlets;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import cz.filmtit.core.Configuration;
import cz.filmtit.core.ConfigurationSingleton;
import cz.filmtit.core.Factory;
import cz.filmtit.core.io.data.FreebaseMediaSourceFactory;
import cz.filmtit.core.model.MediaSourceFactory;
import cz.filmtit.core.model.TranslationMemory;
import cz.filmtit.share.*;
import cz.filmtit.share.exceptions.AuthenticationFailedException;
import cz.filmtit.share.exceptions.InvalidChunkIdException;
import cz.filmtit.share.exceptions.InvalidDocumentIdException;
import cz.filmtit.share.exceptions.InvalidSessionIdException;
import cz.filmtit.userspace.*;
import cz.filmtit.userspace.login.AuthData;
import cz.filmtit.userspace.login.ChangePassToken;
import org.expressme.openid.Association;
import org.expressme.openid.Authentication;
import org.expressme.openid.Endpoint;
import org.expressme.openid.OpenIdManager;
import org.jboss.logging.Logger;

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
    private static long PERMANENT_SESSION_TIME_OUT_LIMIT = ConfigurationSingleton.conf().permanentSessionTimeout();
    private static int SESSION_ID_LENGTH = 47;
    private static int LENGTH_OF_TOKEN = 10;

    protected static USHibernateUtil usHibernateUtil = USHibernateUtil.getInstance();

    private enum CheckUserEnum {
        UserName,
        UserNamePass,
        OpenId
    }

    protected TranslationMemory TM;
    protected MediaSourceFactory mediaSourceFactory;
    protected Configuration configuration;
    private Logger logger = Logger.getLogger("FilmtitBackendServer");

    // AuthId which are in process
    private Map<Integer, AuthData> authDataInProgress =
            Collections.synchronizedMap(new HashMap<Integer, AuthData>());
    // AuthId which are authenticated but not activated
    private Map<Integer,Authentication> finisehdAuthentications =
            Collections.synchronizedMap(new HashMap<Integer, Authentication>());
    // Activated User
    private Map<String, Session> activeSessions =
            Collections.synchronizedMap(new HashMap<String,Session>());

    private Map<String, ChangePassToken> activeTokens =
            Collections.synchronizedMap(new HashMap<String,ChangePassToken>());

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


        logger.info("FilmtitBackendServer started fine!");
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

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    // HANDLING DOCUMENTS
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    @Override
    public DocumentResponse createNewDocument(String sessionID, String documentTitle, String movieTitle, String language)
            throws InvalidSessionIdException {
        return getSessionIfCan(sessionID).createNewDocument(documentTitle, movieTitle, language, mediaSourceFactory);
    }

    @Override
    public Void selectSource(String sessionID, long documentID, MediaSource selectedMediaSource)
            throws InvalidSessionIdException, InvalidDocumentIdException {
        return getSessionIfCan(sessionID).selectSource(documentID, selectedMediaSource);
    }

    @Override
    public List<Document> getListOfDocuments(String sessionID) throws InvalidSessionIdException {
        return getSessionIfCan(sessionID).getListOfDocuments();
    }

    @Override
    public Document loadDocument(String sessionID, long documentID)
            throws InvalidDocumentIdException, InvalidSessionIdException {
        return getSessionIfCan(sessionID).loadDocument(documentID);
    }

    @Override
    public Void closeDocument(String sessionID, long documentId)
            throws InvalidSessionIdException, InvalidDocumentIdException {
        return getSessionIfCan(sessionID).closeDocument(documentId);
    }

    @Override
    public Void deleteDocument(String sessionID, long documentID)
            throws InvalidSessionIdException, InvalidDocumentIdException {
        return getSessionIfCan(sessionID).deleteDocument(documentID);
    }

    @Override
    public Void changeDocumentTitle(String sessionId, long documentID, String newTitle)
            throws InvalidSessionIdException, InvalidDocumentIdException {
        return getSessionIfCan(sessionId).changeDocumentTitle(documentID, newTitle);
    }

    @Override
    public List<MediaSource> changeMovieTitle(String sessionId, long documentID, String newMovieTitle)
            throws InvalidSessionIdException, InvalidDocumentIdException {
        return  getSessionIfCan(sessionId).changeMovieTitle(documentID, newMovieTitle, mediaSourceFactory);
    }


    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    // HANDLING TRANSLATION RESULTS
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    @Override
    public Void saveSourceChunks(String sessionID, List<TimedChunk> chunks)
            throws InvalidSessionIdException, InvalidDocumentIdException {
        return getSessionIfCan(sessionID).saveSourceChunks(chunks);
    }

    @Override
    public TranslationResult getTranslationResults(String sessionID, TimedChunk chunk)
            throws InvalidSessionIdException, InvalidDocumentIdException {
        return getSessionIfCan(sessionID).getTranslationResults(chunk, TM);
    }

    @Override
    public List<TranslationResult> getTranslationResults(String sessionID, List<TimedChunk> chunks)
            throws InvalidSessionIdException, InvalidDocumentIdException {
        return getSessionIfCan(sessionID).getTranslationResultsParallel(chunks, TM);
    }

    @Override
    public Void stopTranslationResults(String sessionID, List<TimedChunk> chunks)
            throws InvalidSessionIdException, InvalidDocumentIdException {
        return getSessionIfCan(sessionID).stopTranslationResults(chunks);
    }

    @Override
    public Void setUserTranslation(String sessionID, ChunkIndex chunkIndex, long documentId,
                                   String userTranslation, long chosenTranslationPairID)
            throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException {
        return getSessionIfCan(sessionID).setUserTranslation(chunkIndex, documentId, userTranslation, chosenTranslationPairID);
    }

    @Override
    public Void setChunkStartTime(String sessionID, ChunkIndex chunkIndex, long documentId, String newStartTime)
            throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException {
        return getSessionIfCan(sessionID).setChunkStartTime(chunkIndex, documentId, newStartTime);
    }

    @Override
    public Void setChunkEndTime(String sessionID, ChunkIndex chunkIndex, long documentId, String newEndTime)
            throws InvalidDocumentIdException, InvalidChunkIdException, InvalidSessionIdException {
        return getSessionIfCan(sessionID).setChunkEndTime(chunkIndex, documentId, newEndTime);
    }

    @Override
    public List<TranslationPair> changeText(String sessionID, ChunkIndex chunkIndex, long documentId, String newText)
            throws InvalidChunkIdException, InvalidDocumentIdException, InvalidSessionIdException {
        return getSessionIfCan(sessionID).changeText(chunkIndex, documentId, newText, TM);
    }

    @Override
    public List<TranslationPair> requestTMSuggestions(String sessionID, ChunkIndex chunkIndex, long documentId)
            throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException {
        return getSessionIfCan(sessionID).requestTMSuggestions(chunkIndex, documentId, TM);
    }

    @Override
    public Void deleteChunk(String sessionID, ChunkIndex chunkIndex, long documentId)
            throws InvalidSessionIdException, InvalidDocumentIdException, InvalidChunkIdException {
        return getSessionIfCan(sessionID).deleteChunk(chunkIndex, documentId);
    }

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    // LOGIN & REGISTRATION STUFF
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    @Override
    public LoginSessionResponse getAuthenticationURL(AuthenticationServiceType serviceType) {
        // generate the unique authentication ID first
        Random random = new Random();
        int authID = random.nextInt();
        while (authDataInProgress.containsKey(authID) || finisehdAuthentications.containsKey(authID)) {
            authID = random.nextInt();
        }

        String serverAddress = ConfigurationSingleton.conf().serverAddress();

        // sets everything necessary ... see the JOpenID page if you want to know details
        manager.setReturnTo(serverAddress + "?page=AuthenticationValidationWindow&authID=" + authID);
        Endpoint endpoint = manager.lookupEndpoint("Google");
        Association association = manager.lookupAssociation(endpoint);
        AuthData authData = new AuthData();
        authData.Mac_key = association.getRawMacKey();
        authData.endpoint = endpoint;
        authDataInProgress.put(authID, authData);
        return new LoginSessionResponse(authID, manager.getAuthenticationUrl(endpoint,association));
    }

    @Override
    public Boolean validateAuthentication(int authID, String responseURL) {
        //  response url - one if you succesfull with login
        //                  sec if you are not
        // if you are you can create authentication object which contains  information
        // using http://code.google.com/p/jopenid/source/browse/trunk/JOpenId/src/test/java/org/expressme/openid/MainServlet.java?r=111&spec=svn111

        try {
            // in progress login session is removed here
            AuthData authData = authDataInProgress.get(authID);
            HttpServletRequest request = FilmTitBackendServer.createRequest(responseURL);
            Authentication authentication = manager.getAuthentication(request, authData.Mac_key, authData.endpoint.getAlias());

            // if no exception was thrown, everything is OK
            finisehdAuthentications.put(authID, authentication);
            logger.info("Testing User is Validate " + authID + " " +authentication.getEmail());
            return true;

        }
        catch (UnsupportedEncodingException e) {
            logger.error("UnsupportedEncodingException caught in validateAuthentication() - " + e.toString());
            return false;
        }
        catch (org.expressme.openid.OpenIdException e) {
            logger.error("OpenIdException caught in validateAuthentication() - " + e.toString());
            return false;
        }
        catch (Exception e) {
            logger.error("Exception caught in validateAuthentication() - " + e.toString());
            return false;
        }
        finally {
            authDataInProgress.remove(authID);        	
        }

    }

    @Override
    public SessionResponse getSessionID(int authID) throws AuthenticationFailedException {
        if (finisehdAuthentications.containsKey(authID)) {
            // the authentication process was successful
        	
        	// cancel the authentication session
            Authentication authentication = finisehdAuthentications.remove(authID);

            String openid = extractOpenId(authentication.getIdentity());
            
            // check whether the user is registered
            if (checkUser(openid) == null) {
            	boolean registrationSuccessful = registration(openid, authentication);
            	if ( !registrationSuccessful ){
                    throw new ExceptionInInitializerError("Registration failed");
                }            	
            }
            // now the user is definitely registered
            return openIDLogin(openid);
        }
        else if (authDataInProgress.containsKey(authID)) {
            // the authentication process has not been finished...
            return null;
        }
        else {
            // since it's neither in the in process table nor the finished table,
            // the authentication must have failed
            throw new AuthenticationFailedException("Authentication failed.");
        }
    }

    @Override
    public SessionResponse simpleLogin(String username, String password) {
        USUser user = checkUser(username, password, CheckUserEnum.UserNamePass);
        if (user == null){ return  null; }
        else {
            Logger.getLogger("User " + user.getUserName() + "logged in.");
            return new SessionResponse(generateSession(user), user.sharedUserWithoutDocuments());
        }
    }

    public SessionResponse openIDLogin(String openId) {
        USUser user = checkUser(openId);
        if (user != null){
            logger.info("User " + user.getUserName() + "logged in.");
            return new SessionResponse(generateSession(user), user.sharedUserWithoutDocuments());
        }
        return null;
    }

    @Override
    public Void logout(String sessionID) throws InvalidSessionIdException {
        Session session = getSessionIfCan(sessionID);
        session.logout();
        logger.info("User " + session.getUser().getUserName() + "logged out.");
        activeSessions.remove(sessionID);

        return null;
    }

    @Override
    public Boolean registration(String name, String pass, String email, String openId) {
        // create user
        USUser check = checkUser(name,pass,CheckUserEnum.UserName);
        if (check == null){
            USUser user = null;

            // pass validation
            String hash = passHash(pass);
            user = new USUser(name,hash,email,openId);

            // create hibernate session
            org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();

            // save into db
            user.saveToDatabase(dbSession);

            usHibernateUtil.closeAndCommitSession(dbSession);
            sendRegistrationMail(user, pass);
            logger.info("Registered user" + user.getUserName());
            return true;
        } else {
            // bad, there is already a user with the given name
            return false;
        }
    }

    /**
     * Register the user with the given openId
     * @param openId
     * @param data
     * @return true if registration is successful, false otherwise
     */
    public Boolean registration(String openId, Authentication data){

        if (data != null){
            Random r = new Random();
            int pin = r.nextInt(9000) + 1000; // 4 random digits, the first one non-zero
            String password = Integer.toString(pin);
            String name = getUniqueName(data.getEmail());
            return registration(name,password,data.getEmail(),openId);
        }
        else {
            return false;        	
        }
    }

    private String extractOpenId(String url){
        String id = url.substring(url.indexOf("?id")+4); // ..oi/id?id=*****
        return id;
    }

    // TODO : what is this good for
    private String getUniqueName(String email){
        String name = email.substring(0,email.indexOf('@'));
        org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();

        List UserResult = dbSession.createQuery("select d from USUser d where d.userName like :username")
                .setParameter("username",name+'%').list(); //UPDATE hibernate  for more constraints
        usHibernateUtil.closeAndCommitSession(dbSession);
        int count = UserResult.size();
        if (count > 0)
        {
            long num = count;
            do {
            String  newName = new StringBuilder(name).append(count).toString();
            num = num << 2 ;
            if (num < 0) {
                count++;
                num = count;
            }
            } while (checkUser(name,null,CheckUserEnum.UserName) != null);
        }
        return name;
    }

    @Override
    public Boolean changePassword(String user  , String pass, String string_token){
        USUser usUser = checkUser(user,"",CheckUserEnum.UserName);
        ChangePassToken token = activeTokens.get(user);

        if (usUser != null && token != null && token.isValidToken(string_token)){

            usUser.setPassword(passHash(pass));
            org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();
            usUser.saveToDatabase(dbSession);
            usHibernateUtil.closeAndCommitSession(dbSession);
            token.deactivate();
            return true;
        }
        return false;
    }

    public Boolean sendChangePasswordMail(USUser user){

        Emailer email = new Emailer();
        if (user.getEmail()!=null) {
            return email.sendForgottenPassMail(
                    user.getEmail(),
                    user.getUserName(),
                    this.forgotUrl(user));
        }
        return false;

    }

    public boolean sendRegistrationMail(USUser user , String pass){
        Emailer email = new Emailer();
        if (user.getEmail()!=null) {
            return email.sendRegistrationMail(
                    user.getEmail(),
                    user.getUserName(),
                    pass
            );
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
     * Check if found sessionId is still active
     */
    @Override
    public SessionResponse checkSessionID(String sessionID){
        if (activeSessions.containsKey(sessionID)) {
            USUser user = activeSessions.get(sessionID).getUser();
            return new SessionResponse(sessionID, user.sharedUserWithoutDocuments());
        }
        return null;
    }

    @Override
    public Boolean sendChangePasswordMail(String username){
        USUser user = checkUser(username,null,CheckUserEnum.UserName);
        if (user != null){
            return sendChangePasswordMail(user);
        }

        return false;
    }


    private boolean sendMail(USUser user){
        Emailer email = new Emailer();
        //if (user.getEmail()!=null) return email.send(user.getEmail(),"You were succesfully login");
        return true;
    }

    private String forgotUrl(USUser user ){
        // string defaultUrl = "?page=ChangePass&login=Pepa&token=000000";       "/?username=%login%&token=%token%#ChangePassword"

        String templateUrl = configuration.serverAddress() + "/?username=%login%&token=%token%#ChangePassword";
        String login = user.getUserName();
        String _token = new IdGenerator().generateId(LENGTH_OF_TOKEN);
        ChangePassToken token = new ChangePassToken(_token);
        String actualUrl = templateUrl.replaceAll("%login%",login).replaceAll("%token%",_token);
        activeTokens.put(login, token);
        return actualUrl;
    }

    /**
     * Get hash of string
     * if return same string like input - problem with algortithm
     */
    private String passHash(String pass){
        return BCrypt.hashpw(pass,BCrypt.gensalt(12));
    }
    /**
     *  generate session;
     *
     */
    private String generateSession(USUser user){
        String newSessionID = (new IdGenerator().generateId(SESSION_ID_LENGTH));
        Session session = new Session(user);

        // check if there isn't a session from the same user
        for (String oldSessionId : activeSessions.keySet()) {
            if (activeSessions.get(oldSessionId).getUserDatabaseId() == user.getDatabaseId()) {
                Session sessionToRemove = activeSessions.get(oldSessionId);
                sessionToRemove.terminateOnNewLogin();
                activeSessions.remove(oldSessionId);
            }
        }

        activeSessions.put(newSessionID, session);
        return newSessionID;
    }

    /**
     * Check if user exists
     *  switch according CheckUserEnum
     *     CheckName - return first user of given name
     *     CheckNamePass - return user with given name and pass
     */
    private USUser checkUser(String username , String password, CheckUserEnum type){
        org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();

        List UserResult = dbSession.createQuery("select d from USUser d where d.userName like :username")
                .setParameter("username",username).list(); //UPDATE hibernate  for more constraints
        usHibernateUtil.closeAndCommitSession(dbSession);
        USUser succesUser = null;
        int count= 0;
        if (type == CheckUserEnum.UserNamePass)
        {
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
            // check if exist user with name
            if (!UserResult.isEmpty())
            {
                succesUser=(USUser)UserResult.get(0);
            }
        }
        return succesUser;
    }

    /**
     * Check for a user with the given openid.
     * @param openid
     * @return The corresponding user if there is one, null if there is not.
     */
    private USUser checkUser(String openid){
        org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();
        List UserResult = new ArrayList(0);
        try {
            UserResult = dbSession.createQuery("select d from USUser d where d.openId like :openid")
                    .setParameter("openid",openid).list(); //UPDATE hibernate  for more constraints
            usHibernateUtil.closeAndCommitSession(dbSession);
        }
        catch (ExceptionInInitializerError ex) {
            logger.warn("Problem with querying the users table.");
        }

        if (UserResult.size() > 1){
            throw new ExceptionInInitializerError("Two users with same authId");
        }
        if (UserResult.size() == 0){
            return null;
        }
        return (USUser)UserResult.get(0);
    }

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    // USER SETTINGS
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public Void setPermanentlyLoggedIn(String sessionID, boolean permanentlyLoggedIn) throws InvalidSessionIdException {
        return getSessionIfCan(sessionID).setPermanentlyLoggedIn(permanentlyLoggedIn);
    }

    public Void setEmail(String sessionID, String email) throws InvalidSessionIdException {
        return getSessionIfCan(sessionID).setEmail(email);
    }

    public Void setMaximumNumberOfSuggestions(String sessionID, int number) throws InvalidSessionIdException {
        return getSessionIfCan(sessionID).setMaximumNumberOfSuggestions(number);
    }

    public Void setUseMoses(String sessionID, boolean useMoses) throws InvalidSessionIdException {
        return getSessionIfCan(sessionID).setUseMoses(useMoses);
    }

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    /**
     * A thread that checks out whether the sessions should be timed out.
     */
    class WatchSessionTimeOut extends Thread {
        public void run() {
            while(true) {
                // removing already existing sessions  that timed out
                for (String sessionID : activeSessions.keySet()) {
                    long now = new Date().getTime();
                    Session thisSession = activeSessions.get(sessionID);
                    if ((thisSession.isPermanent() && thisSession.getLastOperationTime() + PERMANENT_SESSION_TIME_OUT_LIMIT < now)
                            || thisSession.getLastOperationTime() + SESSION_TIME_OUT_LIMIT < now) {
                        activeSessions.remove(thisSession.getUser());
                        thisSession.kill();
                        logger.info("Session of user " + thisSession.getUser().getUserName() + "timed out.");
                        activeSessions.remove(sessionID);
                    }

                }

                // after ask to change a password
                // you get token which is valid 1hour
                // if you use it before limit became token invalid too
                for (String login : activeTokens.keySet()) {
                    ChangePassToken token =  activeTokens.get(login);
                    if (!token.isValidTime()) {
                        activeTokens.remove(login);
                    }

                }
                try { Thread.sleep(60 * 1000); }
                catch (Exception e) {}
            }
        }
    }


    public boolean canReadDocument(String sessionId, long documentId) {
        try {
            Session session = getSessionIfCan(sessionId);
            return session.hasDocument(documentId);
        } catch (InvalidSessionIdException e) {
            return false;
        }
    }

    private Session getSessionIfCan(String sessionId) throws InvalidSessionIdException {
        if (!activeSessions.containsKey(sessionId)) {
            throw new InvalidSessionIdException("Session ID expired or invalid.");
        }

        return activeSessions.get(sessionId);
    }

    /**
     * Gets the string containing the subtitle file of given parameters from the document of given ID.
     * @param sessionID  A valid session ID.
     * @param documentID ID of the exported document
     * @param fps        Frames per second (important for subrip format)
     * @param type       Format of the subtitles
     * @param converter  ???
     * @return
     * @throws InvalidSessionIdException
     * @throws InvalidDocumentIdException
     */
    public String getSourceSubtitles(String sessionID, long documentID, double fps, TimedChunk.FileType type,
                                     ChunkStringGenerator.ResultToChunkConverter converter) throws InvalidSessionIdException, InvalidDocumentIdException {
        Document document = getSessionIfCan(sessionID).getActiveDocument(documentID).getDocument();
        return new ChunkStringGenerator(document, type, fps, converter).toString();
    }

    /**
     * Gets an active document by ID.
     * @param sessionID
     * @param documentID
     * @return
     * @throws InvalidSessionIdException
     * @throws InvalidDocumentIdException
     */
    public USDocument getActiveDocument(String sessionID, long documentID)
            throws InvalidSessionIdException, InvalidDocumentIdException {
        return getSessionIfCan(sessionID).getActiveDocument(documentID);
    }


}

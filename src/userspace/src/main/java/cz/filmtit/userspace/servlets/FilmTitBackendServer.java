package cz.filmtit.userspace.servlets;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import cz.filmtit.core.Configuration;
import cz.filmtit.core.ConfigurationSingleton;
import cz.filmtit.core.Factory;
import cz.filmtit.core.io.data.FreebaseMediaSourceFactory;
import cz.filmtit.core.model.MediaSourceFactory;
import cz.filmtit.core.model.TranslationMemory;
import cz.filmtit.share.*;
import cz.filmtit.share.exceptions.*;
import cz.filmtit.userspace.*;
import cz.filmtit.userspace.login.AuthData;
import cz.filmtit.userspace.login.ChangePassToken;
import cz.filmtit.userspace.login.SeznamData;
import cz.filmtit.userspace.login.TimedAuthenticationWrapper;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FilmTitBackendServer extends RemoteServiceServlet implements
        FilmTitService {

    private static final long serialVersionUID = 3546115L;
    /**
     * Time after which sessions are timed out and closed in milliseconds. It is load from the configuration file.
     */
    private static long SESSION_TIME_OUT_LIMIT = ConfigurationSingleton.conf().sessionTimeout();
    /**
     * Time after which sessions of user who wants to be permanently logged in are timed out and closed in milliseconds.
     * It is load from the configuration file.
     */
    private static long PERMANENT_SESSION_TIME_OUT_LIMIT = ConfigurationSingleton.conf().permanentSessionTimeout();
    /**
     * Time after which a open authentication session is canceled.
     */
    private static long OPEN_ID_AUTH_TIME_OUT = 10 * 60 * 1000; // 10 minutes
    /**
     * Length of session ID.
     */
    private static int SESSION_ID_LENGTH = 47;
    /**
     * Length of token used in the case of forgotten password.
     */
    private static int FORGOTTEN_PASS_TOKEN_LENGTH = 10;



    /**
     * Instance of Hibernate util.
     */
    protected static USHibernateUtil usHibernateUtil = USHibernateUtil.getInstance();

    public enum CheckUserEnum {
        UserName,
        UserNamePass,
        OpenId
    }

    /**
     * Instance of Translation Memory which is used in the server.
     * It is passed to session object at the time
     */
    protected TranslationMemory TM;
    /**
     * Instance of Media Source Factory (Scala object providing the Freebase data about movies)
     * used in the server.
     */
    protected MediaSourceFactory mediaSourceFactory;
    /**
     * The project configuration loaded from the XML file.
     */
    protected Configuration configuration;
    /**
     * Instance of JBoss logger used for logging the server events.
     */
    private USLogger logger =  USLogger.getInstance(); //Logger.getLogger("FilmtitBackendServer");

    /**
     * Map of OpenID authentication which hasn't been resolved yet.
     */
    private Map<Integer, AuthData> authDataInProgress =
            Collections.synchronizedMap(new HashMap<Integer, AuthData>());
    /**
     * Map of OpenID authentications which were successfully resolved and are prepared to generate session id
     * for the user.
     */
    private Map<Integer, TimedAuthenticationWrapper> finishedAuthentications =
            Collections.synchronizedMap(new HashMap<Integer, TimedAuthenticationWrapper>());
    /**
     * Map of active sessions (i.e. logged in Users)
     */
    private Map<String, Session> activeSessions =
            Collections.synchronizedMap(new HashMap<String,Session>());
    /**
     * Map of active tokens generated for changing password.
     */
    private Map<String, ChangePassToken> activeChangePassTokens =
            Collections.synchronizedMap(new HashMap<String,ChangePassToken>());
    /**
     * Instance of JOpenID class used for managing the OpenID login.
     */
    protected OpenIdManager manager = new OpenIdManager();

    /**
     * Default constructor of the server. Initializes all necessary components (database mapping, translation memory
     * core and openID configuration) and starts the thread checking the session time out limits.
     */
    public FilmTitBackendServer() {
        configuration = ConfigurationSingleton.conf();

        loadTranslationMemory();

        mediaSourceFactory = new FreebaseMediaSourceFactory(configuration.freebaseKey(), 10);

        String serverAddress = configuration.serverAddress();
        new WatchSessionTimeOut().start(); // runs deleting timed out sessions

        // set up the OpenID returning address
        manager.setReturnTo(serverAddress + "?page=AuthenticationValidationWindow");
        manager.setRealm(serverAddress);

        // initialize the database by opening and closing a session
        org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();
        usHibernateUtil.closeAndCommitSession(dbSession);


        logger.info("Userspace/server","FilmtitBackendServer started fine!");
    }

    /**
     * Initializes the Translation Memory.
     */
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

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    // HANDLING DOCUMENTS
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *


    /**
     * Creates the document
     * (without source chunks, which have to be added by calling saveSourceChunks),
     * returns its id, together with media source suggestions based on movieTitle.
     * @param sessionID Session ID
     * @param documentTitle Title of the new document
     * @param movieTitle Title of the document's movie
     * @param language Code of the source language of the movie
     * @param moviePath Path to the local video file
     * @return Tuple of a shared document object representing the newly created document and list of suggestions
     *         of possible media sources based on the movie title.
     * @throws InvalidSessionIdException Throws exception when there does not exist a session of given ID.
     */
    @Override
    public DocumentResponse createNewDocument(String sessionID, String documentTitle, String movieTitle, String language, String moviePath)
            throws InvalidSessionIdException {
        return getSessionIfCan(sessionID).createNewDocument(documentTitle, movieTitle, language, mediaSourceFactory, moviePath);
    }

    /**
     * Sets the media source of the document.
     * @param sessionID Session ID
     * @param documentID ID of the document the media source is set to
     * @param selectedMediaSource Selected media source
     * @return Void
     * @throws InvalidSessionIdException Throws an exception when there does not exist a session of given ID.
     * @throws InvalidDocumentIdException Throws an exception when the user does not have document of given ID.
     */
    @Override
    public Void selectSource(String sessionID, long documentID, MediaSource selectedMediaSource)
            throws InvalidSessionIdException, InvalidDocumentIdException {
        return getSessionIfCan(sessionID).selectSource(documentID, selectedMediaSource);
    }

    /**
     * Returns all documents owned by the user, ordered by date and time of last change.
     * @param sessionID Session ID
     * @throws InvalidSessionIdException Throws an exception when there does not exist a session of given ID.
     */
    @Override
    public List<Document> getListOfDocuments(String sessionID) throws InvalidSessionIdException {
        return getSessionIfCan(sessionID).getListOfDocuments();
    }

    /**
     * Returns the document with the given id, with translation results containing source chunks but
     * but not translation suggestions.
     * @param sessionID Session ID
     * @param documentID ID of the document to be loaded
     * @throws InvalidSessionIdException Throws an exception when there does not exist a session of given ID.
     * @throws InvalidDocumentIdException Throws an exception when the user does not have document of given ID.
     */
    @Override
    public Document loadDocument(String sessionID, long documentID)
            throws InvalidDocumentIdException, InvalidSessionIdException {
        return getSessionIfCan(sessionID).loadDocument(documentID);
    }

    /**
     * Removes the given document from the list of user's documents. (The data might not be discarded immediately
     * as the translations still might be used to enrich the translation memory)
     * @param sessionID Session ID
     * @param documentID ID of the document to be deleted
     * @throws InvalidSessionIdException Throws an exception when there does not exist a session of given ID.
     * @throws InvalidDocumentIdException Throws an exception when the user does not have document of given ID.
     */
    @Override
    public Void deleteDocument(String sessionID, long documentID)
            throws InvalidSessionIdException, InvalidDocumentIdException {
        return getSessionIfCan(sessionID).deleteDocument(documentID);
    }

    /**
     * Changes the tile of the document
     * @param sessionId Session ID
     * @param documentID ID of the involved document
     * @param newTitle A new document title suggested by the user.
     * @throws InvalidSessionIdException Throws an exception when there does not exist a session of given ID.
     * @throws InvalidDocumentIdException Throws an exception when the user does not have document of given ID.
     */
    @Override
    public Void changeDocumentTitle(String sessionId, long documentID, String newTitle)
            throws InvalidSessionIdException, InvalidDocumentIdException {
        return getSessionIfCan(sessionId).changeDocumentTitle(documentID, newTitle);
    }

    /**
     * Returns media source suggestions based on newMovieTitle.
     * The movie title is not changed yet, this is only done on calling selectSource.
     * @param sessionId Session ID
     * @param documentID ID of the involved document
     * @param newMovieTitle New movie title suggested by the user
     * @throws InvalidSessionIdException Throws an exception when there does not exist a session of given ID.
     * @throws InvalidDocumentIdException Throws an exception when the user does not have document of given ID.
     */
    @Override
    public List<MediaSource> changeMovieTitle(String sessionId, long documentID, String newMovieTitle)
            throws InvalidSessionIdException, InvalidDocumentIdException {
        return  getSessionIfCan(sessionId).changeMovieTitle(documentID, newMovieTitle, mediaSourceFactory);
    }


    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    // HANDLING TRANSLATION RESULTS
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    /**
     * Save the given source chunks as the contents of the given document
     * (which was already created by calling createNewDocument)
     * @param sessionID Session ID
     * @param chunks List of timed chunks to be saved
     * @throws InvalidSessionIdException Throws an exception when there does not exist a session of given ID.
     * @throws InvalidDocumentIdException Throws an exception when the user does not have document of given ID.
     * @throws InvalidChunkIdException Throws an exception if the the chunks have different document IDs.
     * @throws InvalidValueException Throws an exception if the timings are in a wrong format.
     */
    @Override
    public Void saveSourceChunks(String sessionID, List<TimedChunk> chunks)
            throws InvalidSessionIdException, InvalidDocumentIdException, InvalidChunkIdException, InvalidValueException {
        return getSessionIfCan(sessionID).saveSourceChunks(chunks);
    }

    /**
     * Get the list of possible translations of the given chunk.
     * @param sessionID Session ID
     * @param chunk A timed chunk for which the translation suggestion will be generated
     * @return Translation result with generated translation suggestions.
     * @throws InvalidSessionIdException Throws an exception when there does not exist a session of given ID.
     * @throws InvalidDocumentIdException Throws an exception when the user does not have document of given ID.
     */
    @Override
    public TranslationResult getTranslationResults(String sessionID, TimedChunk chunk)
            throws InvalidSessionIdException, InvalidDocumentIdException {
        return getSessionIfCan(sessionID).getTranslationResults(chunk, TM);
    }

    /**
     * Get the list of lists of possible translations of the given chunks.
     */
    @Override
    public List<TranslationResult> getTranslationResults(String sessionID, List<TimedChunk> chunks)
            throws InvalidSessionIdException, InvalidDocumentIdException {
        return getSessionIfCan(sessionID).getTranslationResultsParallel(chunks, TM);
    }

    /**
     * Stop generating translation results for the given chunks
     * (to be used when the getTranslationResults call has been called
     * with the given chunks.
     */
    @Override
    public Void stopTranslationResults(String sessionID, List<TimedChunk> chunks)
            throws InvalidSessionIdException, InvalidDocumentIdException {
        return getSessionIfCan(sessionID).stopTranslationResults(chunks);
    }

    /**
     * Set the user translation of the given chunk.
     */
    @Override
    public Void setUserTranslation(String sessionID, ChunkIndex chunkIndex, long documentId,
                                   String userTranslation, long chosenTranslationPairID)
            throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException {
        return getSessionIfCan(sessionID).setUserTranslation(chunkIndex, documentId, userTranslation, chosenTranslationPairID);
    }

    /**
     * Change the start time of the given chunk to the new value.
     */
    @Override
    public Void setChunkStartTime(String sessionID, ChunkIndex chunkIndex, long documentId, String newStartTime)
            throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException, InvalidValueException {
        return getSessionIfCan(sessionID).setChunkStartTime(chunkIndex, documentId, newStartTime);
    }

    /**
     * Change the end time of the given chunk to the new value.
     */
    @Override
    public Void setChunkEndTime(String sessionID, ChunkIndex chunkIndex, long documentId, String newEndTime)
            throws InvalidDocumentIdException, InvalidChunkIdException, InvalidSessionIdException, InvalidValueException {
        return getSessionIfCan(sessionID).setChunkEndTime(chunkIndex, documentId, newEndTime);
    }

    /**
     * Change the start time and end time of the given chunk to the values.
     */
    @Override
	public Void setChunkTimes(String sessionID, ChunkIndex chunkIndex,
			long documentId, String newStartTime, String newEndTime)
            throws InvalidSessionIdException, InvalidChunkIdException,
            InvalidDocumentIdException, InvalidValueException {
		
		Session session = getSessionIfCan(sessionID);
		session.setChunkStartTime(chunkIndex, documentId, newStartTime);
		session.setChunkEndTime(chunkIndex, documentId, newEndTime);
		return null;
	}

    /**
     * Change the source text of the chunk,
     * resulting in new translation suggestions
     * which are sent as the result.
     */
    @Override
    public TranslationResult changeText(String sessionID, TimedChunk chunk, String newDbForm)
            throws InvalidChunkIdException, InvalidDocumentIdException, InvalidSessionIdException {
        return getSessionIfCan(sessionID).changeText(chunk, newDbForm, TM);
    }

    /**
     * Remove the chunk from the document, together with its translation if it exists.
     */
    @Override
    public Void deleteChunk(String sessionID, ChunkIndex chunkIndex, long documentId)
            throws InvalidSessionIdException, InvalidDocumentIdException, InvalidChunkIdException {
        return getSessionIfCan(sessionID).deleteChunk(chunkIndex, documentId);
    }

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    // LOGIN & REGISTRATION STUFF
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    /**
     * Register a user with the given username and password, setting the given e-mail and sending registration info to it.
     */
    @Override
    public LoginSessionResponse getAuthenticationURL(AuthenticationServiceType serviceType) {
        // generate the unique authentication ID first
        Random random = new Random();
        int authID = random.nextInt();
        while (authDataInProgress.containsKey(authID) || finishedAuthentications.containsKey(authID)) {
            authID = random.nextInt();
        }

        String serverAddress = ConfigurationSingleton.conf().serverAddress();

        // sets everything necessary ... see the JOpenID page if you want to know details
        manager.setReturnTo(serverAddress + "?page=AuthenticationValidationWindow&authID=" + authID);
        Endpoint endpoint = null;
        if (serviceType == AuthenticationServiceType.GOOGLE){
            endpoint = manager.lookupEndpoint("Google");
        }
        else if (serviceType == AuthenticationServiceType.YAHOO){
            logger.debug("OpenID","Set yahoo endpoint");
            endpoint  = manager.lookupEndpoint("Yahoo");

        }
        else if (serviceType == AuthenticationServiceType.SEZNAM){
            endpoint = manager.lookupEndpoint(configuration.SeznamEndpoint());

        }
        if (endpoint != null){
            Association association = manager.lookupAssociation(endpoint);
            AuthData authData = new AuthData();
            authData.Mac_key = association.getRawMacKey();
            authData.endpoint = endpoint;
            authDataInProgress.put(authID, authData);
            return new LoginSessionResponse(authID, manager.getAuthenticationUrl(endpoint,association));
        }
        logger.error("OpenId","Not supported Endpoint " +  serviceType.toString());
        return null;
    }

    /**
     * Send the URL of the response from the OpenID provider to Userspace for validation.
     * @param authID
     * @param responseURL
     * @return
     */
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
            if (isSeznamOpenId(authentication.getIdentity())){
                SeznamData seznam = new SeznamData(responseURL);
                authentication.setEmail(seznam.getEmail());
                authentication.setFirstname(seznam.getLogin());
                authentication.setIdentity(seznam.getOpenId());
            }

            finishedAuthentications.put(authID, new TimedAuthenticationWrapper(authentication));
            logger.info("AuthenticationOpenId","Testing User is Validate " + authID + " "+authentication.getIdentity()  +" " +authentication.getEmail() + " ");
            return true;

        }
        catch (UnsupportedEncodingException e) {
            logger.error("AuthenticationOpenId","UnsupportedEncodingException caught in validateAuthentication() - " + e.toString());
            return false;
        }
        catch (org.expressme.openid.OpenIdException e) {
            logger.error("AuthenticationOpenId","OpenIdException caught in validateAuthentication() - " + e.toString());
            return false;
        }
        catch (Exception e) {
            logger.error("AuthenticationOpenId","Exception caught in validateAuthentication() - " + e.toString());
            return false;
        }
        finally {
            authDataInProgress.remove(authID);        	
        }

    }

    /**
     * Gets flag if it is openID from Seznam.cz
     * @param url Address of OpenID
     * @return Flag if it is openID from Seznam.cz
     */
    private boolean isSeznamOpenId(String url){
        Pattern patt = Pattern.compile(new String("id\\.seznam\\.cz"));
        Matcher m = patt.matcher(url);
        return  m.find();
    }

    /**
     * Poll the Userspace to find out whether the user has already successfully logged in using his OpenID.
     * @param authID
     * @return
     * @throws AuthenticationFailedException
     * @throws InvalidValueException
     */
    @Override
    public SessionResponse getSessionID(int authID) throws AuthenticationFailedException, InvalidValueException {
        if (finishedAuthentications.containsKey(authID)) {
            // the authentication process was successful
        	
        	// cancel the authentication session
            Authentication authentication = finishedAuthentications.remove(authID).getAuthentication();

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

    /**
     * Try to log in the user with the given username and password.
     * @return Tuple containing session ID and the information about user.
     */
    @Override
    public SessionResponse simpleLogin(String username, String password) {
        USUser user = checkUser(username, password, CheckUserEnum.UserNamePass);
        if (user == null) { return  null; }
        else {
            logger.info("Login","User " + user.getUserName() + "logged in.");
            return new SessionResponse(generateSession(user), user.sharedUserWithoutDocuments());
        }
    }

    /**
     * Logs in a user (creates a session) using the openID (already authenticated).
     * @param openId
     * @return Tuple containing session ID and the information about user.
     */
    public SessionResponse openIDLogin(String openId) {
        USUser user = checkUser(openId);
        if (user != null){
            logger.info("Login","User " + user.getUserName() + "logged in.");
            return new SessionResponse(generateSession(user), user.sharedUserWithoutDocuments());
        }
        return null;
    }

    /**
     * Invalidate the user session with the given sessionID
     * @param sessionID
     * @throws InvalidSessionIdException
     */
    @Override
    public Void logout(String sessionID) throws InvalidSessionIdException {
        Session session = getSessionIfCan(sessionID);
        session.logout();
        logger.info("Login","User " + session.getUser().getUserName() + "logged out.");
        activeSessions.remove(sessionID);

        return null;
    }

    /**
     * Register new user with given cretitials.
     * @param name Picked user name.
     * @param pass Password
     * @param email Email address
     * @param openId OpenID identifyier in case it is an openID registration
     * @return flag - succes registration
     * @throws InvalidValueException
     */
    @Override
    public Boolean registration(String name, String pass, String email, String openId) throws InvalidValueException {

          validateEmail(email);
        // create user
        USUser check = checkUser(name,pass,CheckUserEnum.UserName);
        if (check == null){
            USUser user = null;

            // pass validation
            String hash = passHash(pass);
           // if (!checkEmail(email)) return false;
            user = new USUser(name,hash,email,openId);

            // create hibernate session
            org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();

            // save into db
            user.saveToDatabase(dbSession);

            usHibernateUtil.closeAndCommitSession(dbSession);
            sendRegistrationMail(user, pass);
            logger.info("Login","Registered user" + user.getUserName());
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
    public Boolean registration(String openId, Authentication data) throws InvalidValueException {

        if (data != null){
            Random r = new Random();
            int pin = r.nextInt(9000) + 1000; // 4 random digits, the first one non-zero
            String password = Integer.toString(pin);
            logger.debug("registration/email",data.getEmail());
            String name = getUniqueName(data.getEmail());
            return registration(name, password, data.getEmail(), openId);
        }
        else {
            System.out.println("Data null");
            return false;        	
        }
    }

    /**
     * Extracts openID identifier from the provided openID URL.
     * @param url URL from an OpenID provider
     * @return OpenID identifier.
     */
    private String extractOpenId(String url){
        if (url.indexOf("?id")!=-1){
        String id = url.substring(url.indexOf("?id") + 4); // google id ..oi/id?id=*****
        return id;
        }
        else if (url.indexOf("/a/",8)!=-1){
            String id = url.substring(url.indexOf("/a/",8) + 3 ); // yahoo id ../a/...*****
            return id;
        }
        else {
             return url;
        }
    }

    /**
     * From email gets unique userName
     * @param email
     * @return  unique login
     */
    private String getUniqueName(String email){

        String name = email.substring(0,email.indexOf('@'));
        org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();

        List UserResult = dbSession.createQuery("select d from USUser d where d.userName like :username")
                .setParameter("username", name + '%').list(); //UPDATE hibernate for more constraints
        usHibernateUtil.closeAndCommitSession(dbSession);
        int count = UserResult.size();
        String newName= "";
        if (count > 0)
        {
            long num = count;
            int round = 0;
            do {
            newName = new StringBuilder(name).append(count).toString();
            num = num << 2 ;
            round++;
                if (round > 63) {
                    count++;
                    num = count;
                    round = 0;
                }
                System.out.println("Check "+newName + "num:" + String.valueOf(num));
            } while (checkUser(newName,null,CheckUserEnum.UserName) != null);

        }
        return newName;
    }


    /**
     * Change password in case of forgotten password;
     * user chooses a new password,
     * user authentication is done by the token sent to user's email
     * There needs to be a valid token for changing the password.
     * @param userName
     * @param pass
     * @param stringToken
     * @return  flag - success changing
     * TODO: throw an exception
     */
    @Override
    public Boolean changePassword(String userName, String pass, String stringToken){
        USUser usUser = checkUser(userName, "", CheckUserEnum.UserName);
        ChangePassToken token = activeChangePassTokens.get(userName);

        if (usUser != null && token != null && token.isValidToken(stringToken)){

            usUser.setPassword(passHash(pass));
            org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();
            usUser.saveToDatabase(dbSession);
            usHibernateUtil.closeAndCommitSession(dbSession);
            token.deactivate();
            return true;
        }
        return false;
    }


    /**
     * Sends an email informing user about possibility of change his password.
     * @param user User who is about to change his password
     * @return  Sign if sending email was succesful.
     */
    public Boolean sendChangePasswordMail(USUser user){
        Emailer email = new Emailer();
        if (user.getEmail() != null) {
            return email.sendForgottenPassMail(
                    user.getEmail(),
                    user.getUserName(),
                    this.generateForgotenPassUrl(user));
        }
        return false;

    }

    /**
     * Sends an email informing user about his registration. Password is here as an explicit parameter because
     * it is in a hashed form in the user object and the plain text form is thrown away after registration.
     * @param user Newly registered user
     * @param pass User's password
     * @return Sign if sending email was succesful.
     */
    public boolean sendRegistrationMail(USUser user , String pass){
        Emailer email = new Emailer();
        if (user.getEmail() != null) {
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

    /**
     * Send an email with a link to password reset
     * to the user's email address.
     * @param username registered username of the user
     * @return true on success, false if username is incorrect or there is no email address
     * TODO: throw an exception
     */
    @Override
    public Boolean sendChangePasswordMail(String username){
        USUser user = checkUser(username, null, CheckUserEnum.UserName);
        if (user != null){
            return sendChangePasswordMail(user);
        }

        return false;
    }

    /**
     * Send an email with a link to password reset
     * to the user's email address.
     * @param email registered e-mail address of the user
     * @return true on success, false if username is incorrect or there is no email address
     * TODO: throw an exception
     */
    @Override
    public Boolean sendChangePasswordMailByMail(String email) throws InvalidValueException {
        validateEmail(email);
        org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();
        List usersWithSuchMail = dbSession.createQuery("select u from USUser u where u.email like :email")
                .setParameter("email", email).list();
        usHibernateUtil.closeAndCommitSession(dbSession);

        boolean result;
        if (usersWithSuchMail.size() == 0) { return false; }
        else {
        	result = true;
            for (Object userObj : usersWithSuchMail) {
                 result = result && sendChangePasswordMail((USUser)userObj);
            }
        }

        return result;
    }

    /**
     * Generates URL where a user can change his password.
     * @param user User how is about to change his password.
     * @return URL of page where the user can change the password.
     */
    private String generateForgotenPassUrl(USUser user){
        // string defaultUrl = "?page=ChangePass&login=Pepa&token=000000";       "/?username=%login%&token=%token%#ChangePassword"

        String templateUrl = configuration.serverAddress() + "/?username=%login%&token=%token%#ChangePassword";
        String login = user.getUserName();
        String _token = new IdGenerator().generateId(FORGOTTEN_PASS_TOKEN_LENGTH);
        ChangePassToken token = new ChangePassToken(_token);
        String actualUrl = templateUrl.replaceAll("%login%",login).replaceAll("%token%",_token);
        activeChangePassTokens.put(login, token);
        return actualUrl;
    }

    /**
     * Get hash of string
     * if return same string like input - problem with algortithm
     */
    public static String passHash(String pass){
        return BCrypt.hashpw(pass,BCrypt.gensalt(12));
    }
    /**
     *  generate session;
     *
     */
    private String generateSession(USUser user){
        IdGenerator idGenerator = new IdGenerator();
        String newSessionID = idGenerator.generateId(SESSION_ID_LENGTH);

        while (activeSessions.containsKey(newSessionID)) {
            newSessionID = idGenerator.generateId(SESSION_ID_LENGTH);
        }

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
    public static USUser checkUser(String username , String password, CheckUserEnum type){
        org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();

        List userDbResult = dbSession.createQuery("select d from USUser d where d.userName like :username")
                .setParameter("username", username).list(); //UPDATE hibernate  for more constraints
        usHibernateUtil.closeAndCommitSession(dbSession);
        USUser successUser = null;
        int count = 0;
        if (type == CheckUserEnum.UserNamePass) {
            for (Object aUserResult : userDbResult) {
                USUser user = (USUser) aUserResult;
                if (BCrypt.checkpw(password, user.getPassword())) {
                    successUser = user;
                    count++;
                }
            }

            if (count > 1) {
                throw new ExceptionInInitializerError("Two users with same name and passwords");
            }
        }
        else if (type == CheckUserEnum.UserName)
        {
            // check if exist user with name
            if (!userDbResult.isEmpty()) {
                successUser=(USUser)userDbResult.get(0);
            }
        }
        return successUser;
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
            logger.warning("CheckUser","Problem with querying the users table.");
        }

        if (UserResult.size() > 1){
            throw new ExceptionInInitializerError("Two users with same authId");
        }
        if (UserResult.size() == 0){
            return null;
        }
        return (USUser)UserResult.get(0);
    }
    /**
     * Validates an email address -- throws an exception if it is not valid.
     */
    private void validateEmail(String email) throws InvalidValueException {
      if (!org.apache.commons.validator.routines.EmailValidator.getInstance().isValid(email)){
            throw new InvalidValueException("Email address " + email + "is not valid.");
        }
    }

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    // USER SETTINGS
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    /**
     * Stay logged in permanently (for 1 month) instead of 1 hour (sets the session timeout)
     */
    public Void setPermanentlyLoggedIn(String sessionID, boolean permanentlyLoggedIn) throws InvalidSessionIdException {
        return getSessionIfCan(sessionID).setPermanentlyLoggedIn(permanentlyLoggedIn);
    }

    /**
     * Change user's e-mail.
     */
    public Void setEmail(String sessionID, String email) throws InvalidSessionIdException, InvalidValueException {
        return getSessionIfCan(sessionID).setEmail(email);
    }

    /**
     * Set maximum number of suggestions to show for each line.
     */
    public Void setMaximumNumberOfSuggestions(String sessionID, int number) throws InvalidSessionIdException, InvalidValueException {
        return getSessionIfCan(sessionID).setMaximumNumberOfSuggestions(number);
    }

    /**
     * Include MT results in translation suggestions.
     */
    public Void setUseMoses(String sessionID, boolean useMoses) throws InvalidSessionIdException {
        return getSessionIfCan(sessionID).setUseMoses(useMoses);
    }

    /**
     * Change user's username.
     */
    @Override
    public Void setUsername(String sessionID, String username)
            throws InvalidSessionIdException, InvalidValueException {
        return getSessionIfCan(sessionID).setUsername(username);
    }
    /**
     * Change user's password.
     */
    @Override
    public Void setPassword(String sessionID, String password)
            throws InvalidSessionIdException, InvalidValueException {
        return getSessionIfCan(sessionID).setPassword(password);
    }

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    /**
     * A thread that checks out whether the sessions and other time limited items (authentication tokens,
     * authenticated openIDs without sessions, token for changing password) should be timed out.
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
                        logger.info("SessionTimeOut","Session of user " + thisSession.getUser().getUserName() + "timed out.");
                        activeSessions.remove(sessionID);
                    }

                }

                // after ask to change a password
                // you get token which is valid 1hour
                // if you use it before limit became token invalid too
                for (String login : activeChangePassTokens.keySet()) {
                    ChangePassToken token =  activeChangePassTokens.get(login);
                    if (!token.isValidTime()) {
                        activeChangePassTokens.remove(login);
                    }

                }

                for (Integer authID : authDataInProgress.keySet()) {
                    if (authDataInProgress.get(authID).getCreationTime() + OPEN_ID_AUTH_TIME_OUT < new Date().getTime()) {
                        authDataInProgress.remove(authID);
                    }
                }

                for (Integer authID : finishedAuthentications.keySet()) {
                    if (finishedAuthentications.get(authID).getCreationTime() + OPEN_ID_AUTH_TIME_OUT < new Date().getTime()) {
                        finishedAuthentications.remove(authID);
                    }
                }


                // sleep for a minute and try it again
                try { Thread.sleep(60 * 1000); }
                catch (Exception e) {}
            }
        }
    }


    /**
     * Gets flag if there is such document available in given session.
     * @param sessionId
     * @param documentId
     * @return
     */
    public boolean canReadDocument(String sessionId, long documentId) {
        try {
            Session session = getSessionIfCan(sessionId);
            return session.hasDocument(documentId);
        } catch (InvalidSessionIdException e) {
            return false;
        }
    }

    /**
     * Gets the session if there exist session with such ID. Throws an exception otherwise.
     * @param sessionId
     * @return
     * @throws InvalidSessionIdException
     */
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
     * @param converter  Converter of the translation results to text format
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
     * Gets an active document by ID. If the document hasn't been active before (i.e. the translation results
     * haven't been loaded) it is loaded completely.
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

    /**
     * Log the given message from GUI on server.
     */
    @Override
    public Void logGuiMessage(LevelLogEnum level, String context , String message, String sessionID){
    	if (sessionID != null) {
    		try {
	        	// log some user identifier (can be userid or username)
	        	USUser user = getSessionIfCan(sessionID).getUser();
	            logger.log(level, "GUI: " + context, message, user);
	            return null;
    		}
    		catch (InvalidSessionIdException e) {
    			logger.log(level, "GUI: " + context, message);
			}
    	}
    	else {
            logger.log(level, "GUI: " + context, message);
    	}
    	return null;
    }
}
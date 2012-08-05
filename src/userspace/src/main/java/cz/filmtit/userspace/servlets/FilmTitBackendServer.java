package cz.filmtit.userspace.servlets;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import cz.filmtit.core.Configuration;
import cz.filmtit.core.ConfigurationSingleton;
import cz.filmtit.core.Factory;
import cz.filmtit.core.io.data.FreebaseMediaSourceFactory;
import cz.filmtit.core.model.MediaSourceFactory;
import cz.filmtit.core.model.TranslationMemory;
import cz.filmtit.share.*;
import cz.filmtit.share.exceptions.InvalidChunkIdException;
import cz.filmtit.share.exceptions.InvalidDocumentIdException;
import cz.filmtit.share.exceptions.InvalidSessionIdException;
import cz.filmtit.userspace.*;
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

    // AuthId which are in process
    private Map<Long, AuthData> authenticatingSessions =
            Collections.synchronizedMap(new HashMap<Long, AuthData>());
    // AuthId which are authenticated but not activated
    private Map<Long,Authentication> authenticatedSessions =
            Collections.synchronizedMap(new HashMap<Long, Authentication>());
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
        //session.saveAllTranslationResults(chunks.get(0).getDocumentId());
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

        System.out.println("validateAuthentication(" + authID + "," + responseURL + ")\n");

        try {

            AuthData authData = authenticatingSessions.get(authID);

            HttpServletRequest request = FilmTitBackendServer.createRequest(responseURL);

            Authentication authentication = manager.getAuthentication(request, authData.Mac_key, authData.endpoint.getAlias());

            // if no exception was thrown, everything is OK
            authenticatedSessions.put(authID,authentication);
            System.out.print("Testing User is Validate " + authID + " " +authentication.getEmail());
            return true;

        }
        catch (UnsupportedEncodingException e) {
            System.out.print("UnsupportedEncodingException caught in validateAuthentication() - ");
            System.out.println(e);
            return false;
        }
        catch (org.expressme.openid.OpenIdException e) {
            System.out.print("OpenIdException caught in validateAuthentication() - ");
            System.out.println(e);
            return false;
        }
        catch (Exception e) {
            System.out.print("Exception caught in validateAuthentication() - ");
            System.out.println(e);
            return false;
        }

    }

    @Override
    public String getSessionID(long authID) {
        Authentication authentication = authenticatedSessions.get(authID);
        authenticatedSessions.remove(authID);

        if ( authentication!= null) {
            String openid = extractOpenId(authentication.getIdentity());
            if (checkUser(openid) == null)
            {
                if (!(registration(openid,authentication))){
                    throw new ExceptionInInitializerError("Registration failed");
                }

            }
            return simpleLogin(openid);

        }

        return null;
    }

    @Override
    public String simpleLogin(String username, String password) {


        USUser user = checkUser(username,password,CheckUserEnum.UserNamePass);
        if (user == null){
            return  "";
        }
        else {
            return generateSession(user);
        }
    }

    public String simpleLogin(String openId) {

        USUser user = checkUser(openId);
        if (user!=null){
            return generateSession(user);
        }
        return "";
    }

    @Override
    public Void logout(String sessionID) throws InvalidSessionIdException {
        Session session = getSessionIfCan(sessionID);
        session.logout();
        activeSessions.remove(sessionID);

        return null;
    }
    @Override
    public Boolean  registration(String name ,  String pass  , String email, String openId) {
        // create user

        USUser check = checkUser(name,pass,CheckUserEnum.UserName);
        if (check == null){
            USUser user = null;

            // pass validation
            String hash = pass_hash(pass);
            user = new USUser(name,hash,email,openId);

            // create hibernate session
            org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();

            // save into db
            user.saveToDatabase(dbSession);

            usHibernateUtil.closeAndCommitSession(dbSession);
            sendRegistrationMail(user,pass);
            return true;
        } else {
            // bad, there is already a user with the given name
            return false;
        }
    }

    public Boolean  registration(String openId,Authentication data){

        if (data != null){
            Random r = new Random();
            int pin = r.nextInt(9000) + 1000; // 4 random digits, the first one non-zero
            String password = Integer.toString(pin);
            String name = getUniqName(data.getEmail());
            return registration(name,password,data.getEmail(),openId);
        }
        return false;
    }

    private String extractOpenId(String url){
        String id = url.substring(url.indexOf("?id")+4); // ..oi/id?id=*****
        return id;
    }

    private String getUniqName(String email){
        String name = email.substring(0,email.indexOf('@'));
        org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();

        List UserResult = dbSession.createQuery("select d from USUser d where d.userName like :username")
                .setParameter("username",name+'%').list(); //UPDATE hibernate  for more constraints
        usHibernateUtil.closeAndCommitSession(dbSession);
        int count = UserResult.size();
        if (count > 0)
        {
            return new StringBuilder(name).append(count).toString();
        }
        return name;
    }
    @Override
    public Boolean changePassword(String user  , String pass, String string_token){
        USUser usUser = checkUser(user,"",CheckUserEnum.UserName);
        ChangePassToken token = activeTokens.get(user);

        if (usUser != null && token != null && token.isValidToken(string_token)){

            usUser.setPassword(pass_hash(pass));
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
    public String checkSessionID(String sessionID){
        if (activeSessions.isEmpty()){
            return null;     // no session at all
        }
        Session s = activeSessions.get(sessionID);
        if (s == null) return null;   // not found sessionId
        return s.getUser().getUserName(); // return name of user who had  session

    }
    @Override
    public Boolean sendChangePasswordMail(String username){
        USUser user = checkUser(username,null,CheckUserEnum.UserName);
        if (user != null){
            return sendChangePasswordMail(user);
        }

        return false;
    }


    /**
     * Only test reason
     */
    public void createTestChange(String login , String token){
        activeTokens.put(login,new ChangePassToken(token));
    }

    private boolean sendMail(USUser user){
        Emailer email = new Emailer();
        //if (user.getEmail()!=null) return email.send(user.getEmail(),"You were succesfully login");
        return true;
    }


    /*end test zone*/
    private String forgotUrl(USUser user ){
        // string defaultUrl = "?page=ChangePass&login=Pepa&token=000000";       "/?username=%login%&token=%token%#ChangePassword"

        String templateUrl = configuration.serverAddress() + "/?username=%login%&token=%token%#ChangePassword";
        String login = user.getUserName();
        String _token = new IdGenerator().generateId(LENGTH_OF_TOKEN);
        ChangePassToken token = new ChangePassToken(_token);
        String actualUrl = templateUrl.replaceAll("%login%",login).replaceAll("%token%",_token);
        activeTokens.put(login,token);
        return actualUrl;
    }

    /**
     * Get hash of string
     * if return same string like input - problem with algortithm
     */
    private String pass_hash(String pass){
        return BCrypt.hashpw(pass,BCrypt.gensalt(12));
    }
    /**
     *  generate session;
     *
     */
    private String generateSession(USUser user){
        String newSessionID = (new IdGenerator().generateId(SESSION_ID_LENGHT));
        Session session = new Session(user);
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

    private USUser checkUser(String openid){
        org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();
        List UserResult = new ArrayList(0);
        try {
            UserResult = dbSession.createQuery("select d from USUser d where d.openId like :openid")
                    .setParameter("openid",openid).list(); //UPDATE hibernate  for more constraints
            usHibernateUtil.closeAndCommitSession(dbSession);
        }
        catch (ExceptionInInitializerError ex)
        {
            System.out.print("Problem s dotazem");
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
                for (String login : activeTokens.keySet())
                {
                    ChangePassToken token =  activeTokens.get(login);
                    if (!token.isValidTime())
                    {
                        activeTokens.remove(login);
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

    public class ChangePassToken{

        private String token;
        private Date  validTo;
        private Boolean active;
        public String getToken() {
            return token;
        }

        public ChangePassToken(String token){
            setToken(token);
            // set validity of token now()+1h
            Date actualDate = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(actualDate);
            cal.add(Calendar.HOUR_OF_DAY,1);
            setValidTo(cal.getTime());
            active = true;

        }

        public boolean isValidToken(String token){
            // token is the same and its validity isn`t off
            return ( (this.token.compareTo(token)==0) && (isValidTime()) && (this.active()));
        }

        public boolean  isValidTime()
        {
            Date actual = new Date();
            return ((validTo.compareTo(actual) > 0) && (this.active()));
        }

        public void deactivate()
        {
            this.active = false;
        }
        public Boolean active()
        {
            return this.active;
        }
        private void setValidTo(Date validTo) {
            this.validTo = validTo;
        }
        private void setToken(String token) {
            this.token = token;
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

    public String getSourceSubtitles(String sessionID, long documentID, double fps, TimedChunk.FileType type, ChunkStringGenerator.ResultToChunkConverter converter) throws InvalidSessionIdException, InvalidDocumentIdException {
        Document document = getSessionIfCan(sessionID).loadDocument(documentID);
        return new ChunkStringGenerator(document, type, fps, converter).toString();
    }

    @Override
    public Void saveSourceChunks(String sessionID, List<TimedChunk> chunks) throws InvalidSessionIdException, InvalidDocumentIdException {
        return getSessionIfCan(sessionID).saveSourceChunks(chunks);
    }

}

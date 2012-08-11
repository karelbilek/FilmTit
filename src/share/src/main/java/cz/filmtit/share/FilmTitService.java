package cz.filmtit.share;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import cz.filmtit.share.exceptions.AuthenticationFailedException;
import cz.filmtit.share.exceptions.InvalidChunkIdException;
import cz.filmtit.share.exceptions.InvalidDocumentIdException;
import cz.filmtit.share.exceptions.InvalidSessionIdException;


import java.util.List;
//import cz.filmtit.share.Feedback;

@RemoteServiceRelativePath("filmtit")
public interface FilmTitService extends RemoteService {
    // Document handling
	DocumentResponse createNewDocument(String sessionID, String documentTitle, String movieTitle, String language)
    	throws InvalidSessionIdException;
    Void selectSource(String sessionID, long documentID, MediaSource selectedMediaSource)
    	throws InvalidSessionIdException, InvalidDocumentIdException;
    List<Document> getListOfDocuments(String sessionID) throws InvalidSessionIdException;
    Document loadDocument(String sessionID, long documentID) throws InvalidDocumentIdException, InvalidSessionIdException;
    Void closeDocument(String sessionID, long documentId) throws InvalidSessionIdException, InvalidDocumentIdException;
    public Void changeDocumentTitle(String sessionId, long documentID, String newTitle)
            throws InvalidSessionIdException, InvalidDocumentIdException;
    public List<MediaSource> changeMovieTitle(String sessionId, long documentID, String newMovieTitle)
            throws InvalidSessionIdException, InvalidDocumentIdException;
    
    // Subtitles handling
    Void saveSourceChunks(String sessionID, List<TimedChunk> chunks)
            throws InvalidSessionIdException, InvalidDocumentIdException, InvalidChunkIdException;
    List<TranslationResult> getTranslationResults(String sessionID, List<TimedChunk> chunks)
    	throws InvalidSessionIdException, InvalidDocumentIdException;
    Void stopTranslationResults(String sessionID, List<TimedChunk> chunks)
    	throws InvalidSessionIdException, InvalidDocumentIdException;
    TranslationResult getTranslationResults(String sessionID, TimedChunk chunk)
    	throws InvalidSessionIdException, InvalidDocumentIdException;
    Void setUserTranslation(String sessionID, ChunkIndex chunkIndex, long documentId, String userTranslation, long chosenTranslationPairID)
    	throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException;

    // User settings
    public Void setPermanentlyLoggedIn(String sessionID, boolean permanentlyLoggedIn) throws InvalidSessionIdException;
    public Void setEmail(String sessionID, String email) throws InvalidSessionIdException;
    public Void setMaximumNumberOfSuggestions(String sessionID, int number) throws InvalidSessionIdException;
    public Void setUseMoses(String sessionID, boolean useMoses) throws InvalidSessionIdException;

    // Logging in methods
    // - Prepared for using JOpenID
    LoginSessionResponse getAuthenticationURL(AuthenticationServiceType serviceType);
    Boolean validateAuthentication(int authID, String responseURL);
    SessionResponse getSessionID(int authID) throws AuthenticationFailedException;
    
    // Simple login
    // - registration (true if successful)
    Boolean  registration(String name ,  String pass  , String email, String openId);
    // - login (returns session id on success, null in case of error (should throw an exception eventually))
    SessionResponse simpleLogin(String username, String password);
    /**
     * change password in case of forgotten password;
     * user chooses a new password,
     * user authentication is done by the token sent to user's email
     * @param username
     * @param password
     * @param token
     * @return true on success, false if token is invalid
     */
    Boolean changePassword(String username, String password, String token);
    
    /**
     * Send an email with a link to password reset
     * to the user's email address.
     * @param username
     * @return true on success, false if username is incorrect or there is no email address
     */
    Boolean sendChangePasswordMail(String username);
    
    // - Logout
    Void logout(String sessionID) throws InvalidSessionIdException;

    // Method signatures prepared for the moment we'll have users and session:


    public Void deleteDocument(String sessionID, long documentID)
            throws InvalidSessionIdException, InvalidDocumentIdException;
    Void setChunkStartTime(String sessionID, ChunkIndex chunkIndex, long documentId, String newStartTime)
            throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException;
    Void setChunkEndTime(String sessionID, ChunkIndex chunkIndex, long documentId, String newEndTime)
    		throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException;
    Void setChunkTimes(String sessionID, ChunkIndex chunkIndex, long documentId, String newStartTime, String newEndTime)
    		throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException;
    List<TranslationPair> changeText(String sessionID, ChunkIndex chunkIndex, long documentId, String newText)
            throws InvalidChunkIdException, InvalidDocumentIdException, InvalidSessionIdException;
    public List<TranslationPair> requestTMSuggestions(String sessionID, ChunkIndex chunkIndex , long documentId)
            throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException;
    Void deleteChunk(String sessionID, ChunkIndex chunkIndex, long documentId)
            throws InvalidSessionIdException, InvalidDocumentIdException, InvalidChunkIdException;

    public SessionResponse checkSessionID(String sessionID); // return name of user if succeded and null if sessionId is not found
}

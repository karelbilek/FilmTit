package cz.filmtit.share;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
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
    
    // Subtitles handling
    List<TranslationResult> getTranslationResults(String sessionID, List<TimedChunk> chunks)
    	throws InvalidSessionIdException, InvalidDocumentIdException;
    TranslationResult getTranslationResults(String sessionID, TimedChunk chunk)
    	throws InvalidSessionIdException, InvalidDocumentIdException;
    Void setUserTranslation(String sessionID, ChunkIndex chunkIndex, long documentId, String userTranslation, long chosenTranslationPairID)
    	throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException;

    // Logging in methods
    // - Prepared for using JOpenID
    String getAuthenticationURL(long authID, AuthenticationServiceType serviceType);
    Boolean validateAuthentication(long authID, String responseURL);
    String getSessionID(long authID);
    
    // Simple login
    // - registration (true if successful)
    Boolean  registration(String name ,  String pass  , String email, String openId);
    // - login (returns session id on success, null in case of error (should throw an exception eventually))
    String simple_login(String username, String password);
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
    
    // - Logout
    Void logout(String sessionID) throws InvalidSessionIdException;

    // Method signatures prepared for the moment we'll have users and session:

    List<Document> getListOfDocuments(String sessionID) throws InvalidSessionIdException;
    Document loadDocument(String sessionID, long documentID) throws InvalidDocumentIdException, InvalidSessionIdException;
    Void closeDocument(String sessionID, long documentId) throws InvalidSessionIdException, InvalidDocumentIdException;
    Void setChunkStartTime(String sessionID, ChunkIndex chunkIndex, long documentId, String newStartTime)
            throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException;
    Void setChunkEndTime(String sessionID, ChunkIndex chunkIndex, long documentId, String newEndTime)
            throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException;
    List<TranslationPair> changeText(String sessionID, ChunkIndex chunkIndex, long documentId, String newText)
            throws InvalidChunkIdException, InvalidDocumentIdException, InvalidSessionIdException;
    public List<TranslationPair> requestTMSuggestions(String sessionID, ChunkIndex chunkIndex , long documentId)
            throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException;
    Void deleteChunk(String sessionID, ChunkIndex chunkIndex, long documentId)
            throws InvalidSessionIdException, InvalidDocumentIdException, InvalidChunkIdException;

    public String checkSessionID(String sessionID); // return name of user if succeded and null if sessionId is not found
}

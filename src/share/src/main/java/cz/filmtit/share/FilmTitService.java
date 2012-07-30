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
	DocumentResponse createNewDocument(String sessionID, String movieTitle, String year, String language)
    	throws InvalidSessionIdException;
    Void selectSource(String sessionID, long documentID, MediaSource selectedMediaSource)
    	throws InvalidSessionIdException;
    
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
    TranslationResult regenerateTranslationResult(String sessionID, ChunkIndex chunkIndex, long documentId, TimedChunk chunk)
            throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException;
    public List<TranslationPair> requestTMSuggestions(String sessionID, ChunkIndex chunkIndex , long documentId)
            throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException;

    public String checkSessionID(String sessionID); // return name of user if succeded and null if sessionId is not found
}

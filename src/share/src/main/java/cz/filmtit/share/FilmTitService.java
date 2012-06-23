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
	List<TranslationResult> getTranslationResults(List<TimedChunk> chunk);
	Void setUserTranslation(int chunkId, long documentId, String userTranslation, long chosenTranslationPair);
	Document createDocument(String movieTitle, String year, String language);
    DocumentResponse createNewDocument(String movieTitle, String year, String language);
    Void selectSource(long documentID, MediaSource selectedMediaSource);

    // Logging in methods prepared for using JOpenID
    String getAuthenticationURL(long authID, AuthenticationServiceType serviceType);
    Boolean validateAuthentication(long authID, String responseURL);
    String getSessionID(long authID);

    // Simple login (returns session id on success, null in case of error (should throw an exception eventually))
    String simple_login(String username, String password);
    
    // Method signatures prepared for the moment we'll have users and session:

    Void logout(String sessionID) throws InvalidSessionIdException;
    DocumentResponse createNewDocument(String sessionID, String movieTitle, String year, String language)
            throws InvalidSessionIdException;
    TranslationResult getTranslationResults(String sessionID, TimedChunk chunk) throws InvalidSessionIdException, InvalidDocumentIdException;
    List<TranslationResult>  getTranslationResults(String sessionID, List<TimedChunk> chunks) throws InvalidSessionIdException, InvalidDocumentIdException;
    Void setUserTranslation(String sessionID, int chunkId, long documentId, String userTranslation, long chosenTranslationPairID)
            throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException;
    Void selectSource(String sessionID, long documentID, MediaSource selectedMediaSource) throws InvalidSessionIdException;
    List<Document> getListOfDocuments(String sessionID) throws InvalidSessionIdException;
    Document loadDocument(String sessionID, long documentID) throws InvalidDocumentIdException, InvalidSessionIdException;
    Void closeDocument(String sessionID, long documentId) throws InvalidSessionIdException, InvalidDocumentIdException;
    Void setChunkStartTime(String sessionID, int chunkId, long documentId, String newStartTime)
            throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException;
    Void setChunkEndTime(String sessionID, int chunkId, long documentId, String newEndTime)
            throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException;
    TranslationResult regenerateTranslationResult(String sessionID, int chunkId, long documentId, TimedChunk chunk)
            throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException;
}

package cz.filmtit.share;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
//import cz.filmtit.share.Feedback;

@RemoteServiceRelativePath("filmtit")
public interface FilmTitService extends RemoteService {
	TranslationResult getTranslationResults(TimedChunk chunk);
	Void setUserTranslation(int chunkId, long documentId, String userTranslation, long chosenTranslationPair);
	Document createDocument(String movieTitle, String year, String language);
    DocumentResponse createNewDocument(String movieTitle, String year, String language);
    Void selectSource(long documentID, MediaSource selectedMediaSource);

    String getAuthenticationURL(long authID, AuthenticationServiceType serviceType);
    Boolean validateAuthentication(long authID, String responseURL);
    String getSessionID(long authID);

    // Method signatures prepared for the moment we'll have users and session:

    //Void logout(String sessionId) throws InvalidSessionIdException;
    //Document createDocument(String sessionId, String movieTitle, String year, String language) throws InvalidSessionIdException;
    //TranslationResult getTranslationResults(String sessionId, TimedChunk chunk) throws InvalidSessionIdException;

}

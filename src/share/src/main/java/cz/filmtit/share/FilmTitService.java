package cz.filmtit.share;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
//import cz.filmtit.share.Feedback;

@RemoteServiceRelativePath("filmtit")
public interface FilmTitService extends RemoteService {
	TranslationResult getTranslationResults(TimedChunk chunk);
	Void setUserTranslation(int chunkId, long documentId, String userTranslation, long chosenTranslationPair);
	Document createDocument(String movieTitle, String year, String language);

    // Method signatures prepared for the moment we'll have users and session:

    //Void logout(String sessionId) throws InvalidSessionIdException;
    //Document createDocument(String sessionId, String movieTitle, String year, String language) throws InvalidSessionIdException;
    //TranslationResult getTranslationResult(String sessionId, TimedChunk chunk) throws InvalidSessionIdException;

}

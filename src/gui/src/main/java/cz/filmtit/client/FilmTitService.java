package cz.filmtit.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.TranslationResult;
//import cz.filmtit.share.Feedback;

@RemoteServiceRelativePath("filmtit")
public interface FilmTitService extends RemoteService {
	TranslationResult getTranslationResults(TimedChunk chunk);
	Void setUserTranslation(long translationResultId, String userTranslation, long chosenTranslationPair);
}

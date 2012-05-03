package cz.filmtit.share;

import com.google.gwt.user.client.rpc.AsyncCallback;

import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.TranslationResult;

public interface FilmTitServiceAsync {

	void getTranslationResults(TimedChunk chunk,
			AsyncCallback<TranslationResult> callback);

	void setUserTranslation(long translationResultId, String userTranslation,
			long chosenTranslationPair, AsyncCallback<Void> callback);

}

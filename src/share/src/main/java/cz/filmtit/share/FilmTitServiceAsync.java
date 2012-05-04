package cz.filmtit.share;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface FilmTitServiceAsync {

	void createDocument(String movieTitle, int year, String language,
			AsyncCallback<Document> callback);

	void getTranslationResults(TimedChunk chunk,
			AsyncCallback<TranslationResult> callback);

	void setUserTranslation(long translationResultId, String userTranslation,
			long chosenTranslationPair, AsyncCallback<Void> callback);

}

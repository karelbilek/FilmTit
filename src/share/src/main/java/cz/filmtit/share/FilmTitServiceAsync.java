package cz.filmtit.share;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface FilmTitServiceAsync {

	void createDocument(String movieTitle, String year, String language,
			AsyncCallback<Document> callback);

	void getAuthenticationURL(long authID,
			AuthenticationServiceType serviceType,
			AsyncCallback<String> callback);

	void getSessionID(long authID, AsyncCallback<String> callback);

	void getTranslationResults(TimedChunk chunk,
			AsyncCallback<TranslationResult> callback);

	void setUserTranslation(int chunkId, long documentId,
			String userTranslation, long chosenTranslationPair,
			AsyncCallback<Void> callback);

	void validateAuthentication(long authID, String responseURL,
			AsyncCallback<Boolean> callback);

}

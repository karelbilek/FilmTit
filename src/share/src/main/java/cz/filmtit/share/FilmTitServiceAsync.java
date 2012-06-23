package cz.filmtit.share;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public interface FilmTitServiceAsync
{

    void getTranslationResults(List<TimedChunk> chunk,
			AsyncCallback<List<TranslationResult>> callback);


    void setUserTranslation(String sessionID, int chunkId, long documentId,
			String userTranslation, long chosenTranslationPairID,
			AsyncCallback<Void> callback);


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see cz.filmtit.share.FilmTitService
     */
    void createDocument( java.lang.String movieTitle, java.lang.String year, java.lang.String language, AsyncCallback<cz.filmtit.share.Document> callback );


    void createNewDocument(String movieTitle, String year, String language,
			AsyncCallback<DocumentResponse> callback);


    void selectSource(String sessionID, long documentID,
			MediaSource selectedMediaSource, AsyncCallback<Void> callback);


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see cz.filmtit.share.FilmTitService
     */
    void getAuthenticationURL( long authID, cz.filmtit.share.AuthenticationServiceType serviceType, AsyncCallback<java.lang.String> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see cz.filmtit.share.FilmTitService
     */
    void validateAuthentication( long authID, java.lang.String responseURL, AsyncCallback<java.lang.Boolean> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see cz.filmtit.share.FilmTitService
     */
    void getSessionID( long authID, AsyncCallback<java.lang.String> callback );


    /**
     * Utility class to get the RPC Async interface from client-side code
     */
    public static final class Util 
    { 
        private static FilmTitServiceAsync instance;

        public static final FilmTitServiceAsync getInstance()
        {
            if ( instance == null )
            {
                instance = (FilmTitServiceAsync) GWT.create( FilmTitService.class );
                ServiceDefTarget target = (ServiceDefTarget) instance;
                target.setServiceEntryPoint( GWT.getModuleBaseURL() + "filmtit" );
            }
            return instance;
        }

        private Util()
        {
            // Utility class should not be instanciated
        }
    }


	void closeDocument(String sessionID, long documentId,
			AsyncCallback<Void> callback);


	void getListOfDocuments(String sessionID,
			AsyncCallback<List<Document>> callback);


	void logout(String sessionID, AsyncCallback<Void> callback);


	void loadDocument(String sessionID, long documentID,
			AsyncCallback<Document> callback);


	void setChunkEndTime(String sessionID, int chunkId, long documentId,
			String newEndTime, AsyncCallback<Void> callback);


	void setChunkStartTime(String sessionID, int chunkId, long documentId,
			String newStartTime, AsyncCallback<Void> callback);


	void setUserTranslation(int chunkId, long documentId,
			String userTranslation, long chosenTranslationPair,
			AsyncCallback<Void> callback);


	void regenerateTranslationResult(String sessionID, int chunkId,
			long documentId, TimedChunk chunk,
			AsyncCallback<TranslationResult> callback);


	void getTranslationResults(String sessionID, TimedChunk chunk,
			AsyncCallback<TranslationResult> callback);


	void createNewDocument(String sessionID, String movieTitle, String year,
			String language, AsyncCallback<DocumentResponse> callback);


	void selectSource(long documentID, MediaSource selectedMediaSource,
			AsyncCallback<Void> callback);


	void simple_login(String username, String password,
			AsyncCallback<String> callback);


	void getTranslationResults(String sessionID, List<TimedChunk> chunks,
			AsyncCallback<List<TranslationResult>> callback);
}

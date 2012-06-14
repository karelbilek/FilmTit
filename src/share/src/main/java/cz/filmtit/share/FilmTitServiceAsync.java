package cz.filmtit.share;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public interface FilmTitServiceAsync
{

    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see cz.filmtit.share.FilmTitService
     */
    void getTranslationResults( java.util.List<cz.filmtit.share.TimedChunk> chunk, AsyncCallback<java.util.List<cz.filmtit.share.TranslationResult>> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see cz.filmtit.share.FilmTitService
     */
    void setUserTranslation( int chunkId, long documentId, java.lang.String userTranslation, long chosenTranslationPair, AsyncCallback<java.lang.Void> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see cz.filmtit.share.FilmTitService
     */
    void createDocument( java.lang.String movieTitle, java.lang.String year, java.lang.String language, AsyncCallback<cz.filmtit.share.Document> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see cz.filmtit.share.FilmTitService
     */
    void createNewDocument( java.lang.String movieTitle, java.lang.String year, java.lang.String language, AsyncCallback<cz.filmtit.share.DocumentResponse> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see cz.filmtit.share.FilmTitService
     */
    void selectSource( long documentID, cz.filmtit.share.MediaSource selectedMediaSource, AsyncCallback<java.lang.Void> callback );


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
}

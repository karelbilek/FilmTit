package cz.filmtit.share;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import java.util.List;

public interface FilmTitServiceAsync
{

    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see cz.filmtit.share.FilmTitService
     */
    void getTranslationResults( List<cz.filmtit.share.TimedChunk> p0, AsyncCallback<List<cz.filmtit.share.TranslationResult>> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see cz.filmtit.share.FilmTitService
     */
    void setUserTranslation( int p0, long p1, java.lang.String p2, long p3, AsyncCallback<java.lang.Void> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see cz.filmtit.share.FilmTitService
     */
    void createDocument( java.lang.String p0, java.lang.String p1, java.lang.String p2, AsyncCallback<cz.filmtit.share.Document> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see cz.filmtit.share.FilmTitService
     */
    void createNewDocument( java.lang.String p0, java.lang.String p1, java.lang.String p2, AsyncCallback<cz.filmtit.share.DocumentResponse> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see cz.filmtit.share.FilmTitService
     */
    void selectSource( long p0, cz.filmtit.share.MediaSource p1, AsyncCallback<java.lang.Void> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see cz.filmtit.share.FilmTitService
     */
    void getAuthenticationURL( long p0, cz.filmtit.share.AuthenticationServiceType p1, AsyncCallback<java.lang.String> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see cz.filmtit.share.FilmTitService
     */
    void validateAuthentication( long p0, java.lang.String p1, AsyncCallback<java.lang.Boolean> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see cz.filmtit.share.FilmTitService
     */
    void getSessionID( long p0, AsyncCallback<java.lang.String> callback );


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
                target.setServiceEntryPoint( GWT.getModuleBaseURL() + "FilmTitService" );
            }
            return instance;
        }

        private Util()
        {
            // Utility class should not be instanciated
        }
    }
}

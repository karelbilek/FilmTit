package cz.filmtit.share;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import cz.filmtit.share.exceptions.AuthenticationFailedException;
import cz.filmtit.share.exceptions.InvalidChunkIdException;
import cz.filmtit.share.exceptions.InvalidDocumentIdException;
import cz.filmtit.share.exceptions.InvalidSessionIdException;
import cz.filmtit.share.exceptions.InvalidValueException;

@RemoteServiceRelativePath("filmtit")
public interface FilmTitService extends RemoteService {

    ////////////////////////////////////////
    //                                    //
    //  Document handling                 //
    //                                    //
    ////////////////////////////////////////
	
	/**
	 * Creates the document
	 * (without source chunks, which have to be added by calling saveSourceChunks),
	 * returns its id, together with media source suggestions based on movieTitle.
	 */     	
	DocumentResponse createNewDocument(String sessionID, String documentTitle, String movieTitle, String language, String moviePath)
    	throws InvalidSessionIdException;
	/**
	 * Sets the media source of the document.
	 */     	
    Void selectSource(String sessionID, long documentID, MediaSource selectedMediaSource)
    	throws InvalidSessionIdException, InvalidDocumentIdException;
	/**
	 * Returns all documents owned by the user, ordered by date and time of last change.
	 */     	
    List<Document> getListOfDocuments(String sessionID)
    	throws InvalidSessionIdException;
	/**
	 * Returns the document with the given id, with source chunks but without translation suggestions.
	 */     	
    Document loadDocument(String sessionID, long documentID)
    	throws InvalidDocumentIdException, InvalidSessionIdException;
	/**
	 * Not used. TODO: delete?
	 */     	
    Void closeDocument(String sessionID, long documentId)
    	throws InvalidSessionIdException, InvalidDocumentIdException;
	/**
	 * Sets a different title for the document.
	 */     	
    Void changeDocumentTitle(String sessionId, long documentID, String newTitle)
        throws InvalidSessionIdException, InvalidDocumentIdException;
	/**
	 * Returns media source suggestions based on newMovieTitle.
	 * The movie title is not changed yet,
	 * this is only done on calling selectSource.
	 * TODO: is this true?     	 
	 */     	
    List<MediaSource> changeMovieTitle(String sessionId, long documentID, String newMovieTitle)
        throws InvalidSessionIdException, InvalidDocumentIdException;
	/**
	 * Remove the given document from the list of user's documents.
	 * (The data might not be discarded immediately
	 * as the translations still might be used to encŕich the translation memory)	 
	 */
    Void deleteDocument(String sessionID, long documentID)
    	throws InvalidSessionIdException, InvalidDocumentIdException;
    
    ////////////////////////////////////////
    //                                    //
    //  Source subtitles handling         //
    //                                    //
    ////////////////////////////////////////
	
	/**
	 * Save the given source chunks as the contents of the given document
	 * (which was already created by calling createNewDocument)	 
	 */
    Void saveSourceChunks(String sessionID, List<TimedChunk> chunks)
    	throws InvalidSessionIdException, InvalidDocumentIdException, InvalidChunkIdException, InvalidValueException;
	/**
	 * Change the start time of the given chunk to the new value.
	 */
    Void setChunkStartTime(String sessionID, ChunkIndex chunkIndex, long documentId, String newStartTime)
	    throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException, InvalidValueException;
	/**
	 * Change the end time of the given chunk to the new value.
	 */
	Void setChunkEndTime(String sessionID, ChunkIndex chunkIndex, long documentId, String newEndTime)
	    throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException, InvalidValueException;
	/**
	 * Change the start time and end time of the given chunk to the values.
	 */
	Void setChunkTimes(String sessionID, ChunkIndex chunkIndex, long documentId, String newStartTime, String newEndTime)
		throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException, InvalidValueException;
	/**
	 * Change the source text of the chunk,
	 * resulting in new translation suggestions
	 * which are sent as the result.
	 */
	TranslationResult changeText(String sessionID, TimedChunk chunk, String newDbForm)
		throws InvalidChunkIdException, InvalidDocumentIdException, InvalidSessionIdException;
	/**
	 * Remove the chunk from the document, together with its translation if it exists.
	 */
	Void deleteChunk(String sessionID, ChunkIndex chunkIndex, long documentId)
	    throws InvalidSessionIdException, InvalidDocumentIdException, InvalidChunkIdException;
    
    ////////////////////////////////////////
    //                                    //
    //  Target subtitles handling         //
    //                                    //
    ////////////////////////////////////////
	
	/**
	 * Get the list of possible translations of the given chunk.
	 */
	TranslationResult getTranslationResults(String sessionID, TimedChunk chunk)
		throws InvalidSessionIdException, InvalidDocumentIdException;
	/**
	 * Get the list of lists of possible translations of the given chunks.
	 */
    List<TranslationResult> getTranslationResults(String sessionID, List<TimedChunk> chunks)
		throws InvalidSessionIdException, InvalidDocumentIdException;
	/**
	 * Stop generating translation results for the given chunks
	 * (to be used when the getTranslationResults call has been called
	 * with the given chunks.
	 */
	Void stopTranslationResults(String sessionID, List<TimedChunk> chunks)
		throws InvalidSessionIdException, InvalidDocumentIdException;
	/**
	 * Set the user translation of the given chunk.
	 */
	Void setUserTranslation(String sessionID, ChunkIndex chunkIndex, long documentId, String userTranslation, long chosenTranslationPairID)
		throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException;

	////////////////////////////////////////
    //                                    //
    //  User settings                     //
    //                                    //
    ////////////////////////////////////////
    
	/**
	 * Change user's username.
	 */
    Void setUsername(String sessionID, String username)
		throws InvalidSessionIdException, InvalidValueException;
    /**
     * Change user's password.
     */
    Void setPassword(String sessionID, String password)
		throws InvalidSessionIdException, InvalidValueException;
	/**
	 * Change user's e-mail.
	 */
    Void setEmail(String sessionID, String email)
    	throws InvalidSessionIdException, InvalidChunkIdException, InvalidValueException;
    /**
     * Stay logged in permanently (for 1 month) instead of 1 hour (sets the session timeout)
     */
    Void setPermanentlyLoggedIn(String sessionID, boolean permanentlyLoggedIn)
    	throws InvalidSessionIdException;
	/**
	 * Set maximum number of suggestions to show for each line.
	 */
    Void setMaximumNumberOfSuggestions(String sessionID, int number)
    	throws InvalidSessionIdException, InvalidValueException;
    /**
     * Include MT results in translation suggestions.
     */
    Void setUseMoses(String sessionID, boolean useMoses)
    	throws InvalidSessionIdException;

    ////////////////////////////////////////
    //                                    //
    //  Logging in and out, registration  //
    //                                    //
    ////////////////////////////////////////
    
    /**
     * Register a user with the given username and password, setting the given e-mail and sending registration info to it.
     * @param username
     * @param password
     * @param email
     * @param openId TODO remove
     * @return true if successful, false if not
     * TODO: throw an exception
     */
    Boolean registration(String username, String password, String email, String openId)
    	throws InvalidValueException;
    
    /**
     * Try to log in the user with the given username and password.
     * @param username
     * @param password
     * @return SessionResponse containing the sessionID and the User object on success, null on error
     * TODO: throw an exception
     */
    SessionResponse simpleLogin(String username, String password);
    
    /**
     * Validates the given sessionID.
     * @param sessionID
     * @return SessionResponse containing the sessionID and the User object if the sessionID is valid, null if not
     */
    SessionResponse checkSessionID(String sessionID);
    
    /**
     * Invalidate the user session with the given sessionID
     * @param sessionID
     * @throws InvalidSessionIdException
     */
    Void logout(String sessionID)
    	throws InvalidSessionIdException;

    /**
     * Send an email with a link to password reset
     * to the user's email address.
     * @param username registered username of the user
     * @return true on success, false if username is incorrect or there is no email address
     * TODO: throw an exception
     */
    Boolean sendChangePasswordMail(String username);
    
    /**
     * Send an email with a link to password reset
     * to the user's email address.
     * @param email registered e-mail address of the user
     * @return true on success, false if username is incorrect or there is no email address
     * TODO: throw an exception
     */
    Boolean sendChangePasswordMailByMail(String email)
    	throws InvalidValueException;
    
    /**
     * Change password in case of forgotten password;
     * user chooses a new password,
     * user authentication is done by the token sent to user's email
     * @param username
     * @param password
     * @param token
     * @return true on success, false if token is invalid
     * TODO: throw an exception
     */
    Boolean changePassword(String username, String password, String token);
    
    ////////////////////////////////////////
    //                                    //
    //  OpenID login                      //
    //                                    //
    ////////////////////////////////////////
    
    /**
     * Get the URL of a window to show to the user for him to log in using his OpenID
     */
    LoginSessionResponse getAuthenticationURL(AuthenticationServiceType serviceType);
    
    /**
     * Send the URL of the response from the OpenID provider to Userspace for validation.
     * @param authID
     * @param responseURL
     * @return
     */
    Boolean validateAuthentication(int authID, String responseURL);
    
    /**
     * Poll the Userspace to find out whether the user has already successfully logged in using his OpenID.
     * @param authID
     * @return
     * @throws AuthenticationFailedException
     * @throws InvalidValueException
     */
    SessionResponse getSessionID(int authID)
    	throws AuthenticationFailedException, InvalidValueException;
    
    ////////////////////////////////////////
    //                                    //
    //  Remote logging                    //
    //                                    //
    ////////////////////////////////////////
    
    /**
     * Log the given message from GUI on server.
     */
    Void logGuiMessage(LevelLogEnum level, String context, String message, String sessionID);
}

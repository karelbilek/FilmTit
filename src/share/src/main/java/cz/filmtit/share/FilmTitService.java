/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/
package cz.filmtit.share;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import cz.filmtit.share.exceptions.*;

import java.util.List;

@RemoteServiceRelativePath("filmtit")
public interface FilmTitService extends RemoteService {

    ////////////////////////////////////////
    //                                    //
    //  Document handling                 //
    //                                    //
    ////////////////////////////////////////
    /**
     * Creates the document (without source chunks, which have to be added by
     * calling saveSourceChunks), returns its id, together with media source
     * suggestions based on movieTitle.
     *
     * @param sessionID Session ID
     * @param documentTitle Title of the new document
     * @param movieTitle Title of the document's movie
     * @param language Code of the source language of the movie
     * @param moviePath Path to the local video file
     * @return Tuple of a shared document object representing the newly created
     * document and list of suggestions of possible media sources based on the
     * movie title.
     * @throws InvalidSessionIdException Throws exception when there does not
     * exist a session of given ID.
     */
    DocumentResponse createNewDocument(String sessionID, String documentTitle, String movieTitle, String language, String moviePath)
            throws InvalidSessionIdException;

    /**
     * Sets the media source of the document. * @param sessionID Session ID
     *
     * @param documentID ID of the document the media source is set to
     * @param selectedMediaSource Selected media source
     * @return Void
     * @throws InvalidSessionIdException Throws an exception when there does not
     * exist a session of given ID.
     * @throws InvalidDocumentIdException Throws an exception when the user does
     * not have document of given ID.
     */
    Void selectSource(String sessionID, long documentID, MediaSource selectedMediaSource)
            throws InvalidSessionIdException, InvalidDocumentIdException;

    /**
     * Returns all documents owned by the user, ordered by date and time of last
     * change.
     *
     * @param sessionID Session ID
     * @throws InvalidSessionIdException Throws an exception when there does not
     * exist a session of given ID.
     */
    List<Document> getListOfDocuments(String sessionID)
            throws InvalidSessionIdException;      

    /**
     * Returns the document with the given id, with source chunks but without
     * translation suggestions.
     *
     * @param sessionID Session ID
     * @param documentID ID of the document to be loaded
     * @throws InvalidSessionIdException Throws an exception when there does not
     * exist a session of given ID.
     * @throws InvalidDocumentIdException Throws an exception when the user does
     * not have document of given ID.
     */
    Document loadDocument(String sessionID, long documentID)
            throws InvalidDocumentIdException, InvalidSessionIdException;

    /**
     * Sets a different title for the document. * @param sessionId Session ID
     *
     * @param documentID ID of the involved document
     * @param newTitle A new document title suggested by the user.
     * @throws InvalidSessionIdException Throws an exception when there does not
     * exist a session of given ID.
     * @throws InvalidDocumentIdException Throws an exception when the user does
     * not have document of given ID.
     */
    Void changeDocumentTitle(String sessionID, long documentID, String newTitle)
            throws InvalidSessionIdException, InvalidDocumentIdException;

    /**
     * Returns media source suggestions based on newMovieTitle. The movie title
     * is not changed yet, this is only done on calling selectSource.
     *
     * @param sessionID Session ID
     * @param documentID ID of the involved document
     * @param newMovieTitle New movie title suggested by the user
     * @throws InvalidSessionIdException Throws an exception when there does not
     * exist a session of given ID.
     * @throws InvalidDocumentIdException Throws an exception when the user does
     * not have document of given ID.
     */
    List<MediaSource> changeMovieTitle(String sessionID, long documentID, String newMovieTitle)
            throws InvalidSessionIdException, InvalidDocumentIdException;

    /**
     * Remove the given document from the list of user's documents. (The data
     * might not be discarded immediately as the translations still might be
     * used to enrich the translation memory)
     *
     * @param sessionID Session ID
     * @param documentID ID of the document to be deleted
     * @throws InvalidSessionIdException Throws an exception when there does not
     * exist a session of given ID.
     * @throws InvalidDocumentIdException Throws an exception when the user does
     * not have document of given ID.
     */
    Void deleteDocument(String sessionID, long documentID)
            throws InvalidSessionIdException, InvalidDocumentIdException;

    ////////////////////////////////////////
    //                                    //
    //  Source subtitles handling         //
    //                                    //
    ////////////////////////////////////////
    /**
     * Save the given source chunks as the contents of the given document (which
     * was already created by calling createNewDocument)
     *
     * @param sessionID Session ID
     * @param chunks List of timed chunks to be saved
     * @throws InvalidSessionIdException Throws an exception when there does not
     * exist a session of given ID.
     * @throws InvalidDocumentIdException Throws an exception when the user does
     * not have document of given ID.
     * @throws InvalidChunkIdException Throws an exception if the the chunks
     * have different document IDs.
     * @throws InvalidValueException Throws an exception if the timings are in a
     * wrong format.
     */
    Void saveSourceChunks(String sessionID, List<TimedChunk> chunks)
            throws InvalidSessionIdException, InvalidDocumentIdException, InvalidChunkIdException, InvalidValueException;

    /**
     * Change the start time of the given chunk to the new value.
     *
     * @param sessionID Session ID
     * @param chunkIndex Identifier of the chunk that has been changed
     * @param documentID ID of the document the chunk belongs
     * @param newStartTime New value of chunk start time
     * @return Void
     * @throws InvalidSessionIdException Throws an exception when there does not
     * exist a session of given ID.
     * @throws InvalidDocumentIdException Throws an exception when the user does
     * not have document of given ID.
     * @throws InvalidChunkIdException Throws an exception when such chunk does
     * not exist in the document.
     */
    Void setChunkStartTime(String sessionID, ChunkIndex chunkIndex, long documentID, String newStartTime)
            throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException, InvalidValueException;

    /**
     * Change the end time of the given chunk to the new value.
     *
     * @param sessionID Session ID
     * @param chunkIndex Identifier of the chunk that has been changed
     * @param documentID ID of the document the chunk belongs
     * @param newEndTime New value of chunk end time
     * @return Void
     * @throws InvalidSessionIdException Throws an exception when there does not
     * exist a session of given ID.
     * @throws InvalidDocumentIdException Throws an exception when the user does
     * not have document of given ID.
     * @throws InvalidChunkIdException Throws an exception when such chunk does
     * not exist in the document.
     */
    Void setChunkEndTime(String sessionID, ChunkIndex chunkIndex, long documentID, String newEndTime)
            throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException, InvalidValueException;

    /**
     * Change the start time and end time of the given chunk to the values.
     *
     * @param sessionID Session ID
     * @param chunkIndex Identifier of the chunk that has been changed
     * @param documentID ID of the document the chunk belongs
     * @param newStartTime New value of chunk start time
     * @param newEndTime New value of chunk end time
     * @return Void
     * @throws InvalidSessionIdException Throws an exception when there does not
     * exist a session of given ID.
     * @throws InvalidDocumentIdException Throws an exception when the user does
     * not have document of given ID.
     * @throws InvalidChunkIdException Throws an exception when such chunk does
     * not exist in the document.
     */
    Void setChunkTimes(String sessionID, ChunkIndex chunkIndex, long documentID, String newStartTime, String newEndTime)
            throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException, InvalidValueException;

    /**
     * Change the source text of the chunk, resulting in new translation
     * suggestions which are sent as the result.
     *
     * @param sessionID Session ID
     * @param chunk Chunk whose source text has been changed
     * @param newDbForm New text of the chunk in database form
     * @return Translation result with generated suggestions
     * @throws InvalidSessionIdException Throws an exception when there does not
     * exist a session of given ID.
     * @throws InvalidDocumentIdException Throws an exception when the user does
     * not have document of given ID.
     * @throws InvalidChunkIdException Throws an exception when such chunk does
     * not exist in the document.
     */
    TranslationResult changeText(String sessionID, TimedChunk chunk, String newDbForm)
            throws InvalidChunkIdException, InvalidDocumentIdException, InvalidSessionIdException;

    /**
     * Remove the chunk from the document, together with its translation if it
     * exists.
     *
     * @param sessionID Session ID
     * @param chunkIndex Index of chunk to be deleted
     * @param documentID ID of the document the chunk belongs to
     * @return Void
     * @throws InvalidSessionIdException Throws an exception when there does not
     * exist a session of given ID.
     * @throws InvalidDocumentIdException Throws an exception when the user does
     * not have document of given ID.
     * @throws InvalidChunkIdException Throws an exception when such chunk does
     * not exist in the document.
     */
    Void deleteChunk(String sessionID, ChunkIndex chunkIndex, long documentID)
            throws InvalidSessionIdException, InvalidDocumentIdException, InvalidChunkIdException;

    ////////////////////////////////////////
    //                                    //
    //  Target subtitles handling         //
    //                                    //
    ////////////////////////////////////////
    /**
     * Get the list of possible translations of the given chunk.
     *
     * @param sessionID Session ID
     * @param chunk A timed chunk for which the translation suggestion will be
     * generated
     * @return Translation result with generated translation suggestions.
     * @throws InvalidSessionIdException Throws an exception when there does not
     * exist a session of given ID.
     * @throws InvalidDocumentIdException Throws an exception when the user does
     * not have document of given ID.
     */
    TranslationResult getTranslationResults(String sessionID, TimedChunk chunk)
            throws InvalidSessionIdException, InvalidDocumentIdException;

    /**
     * Get the list of lists of possible translations of the given chunks.
     *
     * @param sessionID Session ID
     * @param chunks List of timed chunks to which translation suggestion should
     * generated
     * @return List of translation results with suggestions
     * @throws InvalidSessionIdException Throws an exception when there does not
     * exist a session of given ID.
     * @throws InvalidDocumentIdException Throws an exception when the user does
     * not have document of given ID.
     */
    List<TranslationResult> getTranslationResults(String sessionID, List<TimedChunk> chunks)
            throws InvalidSessionIdException, InvalidDocumentIdException;
    
    /**
     * Reloads translation results from database
     * @param documentId
     * @return
     * @throws InvalidDocumentIdException 
     */
    Document reloadTranslationResults(Long documentId) throws InvalidDocumentIdException;

    /**
     * Stop generating translation results for the given chunks (to be used when
     * the getTranslationResults call has been called with the given chunks).
     *
     * @param sessionID Session ID
     * @param chunks List of timed chunks for which the suggestion generation
     * should be stopped.
     * @return Void
     * @throws InvalidSessionIdException Throws an exception when there does not
     * exist a session of given ID.
     * @throws InvalidDocumentIdException Throws an exception when the user does
     * not have document of given ID.
     */
    Void stopTranslationResults(String sessionID, List<TimedChunk> chunks)
            throws InvalidSessionIdException, InvalidDocumentIdException;

    /**
     * Set the user translation of the given chunk.
     *
     * @param sessionID Session ID
     * @param chunkIndex Identifier of the chunk that has been changed
     * @param documentID ID of the document the chunk belongs to
     * @param userTranslation New user translation
     * @param chosenTranslationPairID ID of translation suggestion picked by the
     * user
     * @return Void
     * @throws InvalidSessionIdException Throws an exception when there does not
     * exist a session of given ID.
     * @throws InvalidDocumentIdException Throws an exception when the user does
     * not have document of given ID.
     * @throws InvalidChunkIdException Throws an exception when such chunk does
     * not exist in the document.
     */
    Void setUserTranslation(String sessionID, ChunkIndex chunkIndex, long documentID, String userTranslation, long chosenTranslationPairID)
            throws InvalidSessionIdException, InvalidChunkIdException, InvalidDocumentIdException;

    ////////////////////////////////////////
    //                                    //
    //  User settings                     //
    //                                    //
    ////////////////////////////////////////
    /**
     * Change user's username.
     *
     * @param sessionID Session ID
     * @param username New user name
     * @return Void
     * @throws InvalidSessionIdException Throws an exception when there does not
     * exist a session of given ID.
     */
    Void setUsername(String sessionID, String username)
            throws InvalidSessionIdException, InvalidValueException;

    /**
     * Change user's password.
     *
     * @param sessionID Session ID
     * @param password New password
     * @return Void
     * @throws InvalidSessionIdException Throws an exception when there does not
     * exist a session of given ID.
     */
    Void setPassword(String sessionID, String password)
            throws InvalidSessionIdException, InvalidValueException;

    /**
     * Change user's e-mail.
     *
     * @param sessionID Session ID
     * @param email New users email
     * @return Void
     * @throws InvalidSessionIdException Throws an exception when there does not
     * exist a session of given ID.
     * @throws InvalidValueException Throws an excpetion if the email address is
     * not valid.
     */
    Void setEmail(String sessionID, String email)
            throws InvalidSessionIdException, InvalidChunkIdException, InvalidValueException;

    /**
     * Stay logged in permanently (for 1 month by default) instead of 1 hour
     * (sets the session timeout)
     *
     * @param sessionID Session ID
     * @param permanentlyLoggedIn Flag if the user wants to be logged in
     * permanently.
     * @return Void
     * @throws InvalidSessionIdException Throws an exception when there does not
     * exist a session of given ID.
     */
    Void setPermanentlyLoggedIn(String sessionID, boolean permanentlyLoggedIn)
            throws InvalidSessionIdException;

    /**
     * Set maximum number of suggestions to show for each line.
     *
     * @param sessionID Session ID
     * @param number Flag if the moses translation should be used.
     * @return Void
     * @throws InvalidSessionIdException Throws an exception when there does not
     * exist a session of given ID.
     * @throws InvalidValueException Throws an exception if the given number is
     * invalid (negative or over 100)
     */
    Void setMaximumNumberOfSuggestions(String sessionID, int number)
            throws InvalidSessionIdException, InvalidValueException;

    /**
     * Include MT results in translation suggestions.
     *
     * @param sessionID Session ID
     * @param useMoses Flag if the moses translation should be used.
     * @return Void
     * @throws InvalidSessionIdException Throws an exception when there does not
     * exist a session of given ID.
     */
    Void setUseMoses(String sessionID, boolean useMoses)
            throws InvalidSessionIdException;

    ////////////////////////////////////////
    //                                    //
    //  Logging in and out, registration  //
    //                                    //
    ////////////////////////////////////////
    /**
     * Register new user with given properties.
     *
     * @param username Picked user name.
     * @param password Password
     * @param email Email address
     * @param openID OpenID identifier in case it is an openID registration
     * @return Flag if the registration has been successful.
     * @throws InvalidValueException Throws an exception if an email address is
     * provided in wrong format.
     */
    Boolean registration(String username, String password, String email, String openID)
            throws InvalidValueException;

    /**
     * Try to log in the user with the given username and password.
     *
     * @param username User name
     * @param password User's password
     * @return Tuple containing session ID and the information about user.
     */
    SessionResponse simpleLogin(String username, String password);

    /**
     * Check if found sessionId is still active. If yes returns the same data as
     * at the time of logging in, return null otherwise.
     *
     * @param sessionID Session ID
     * @return Pair of session ID and user object if the session is still
     * active, null otherwise.
     */
    SessionResponse checkSessionID(String sessionID);

    /**
     * Invalidate the user session with the given sessionID.
     *
     * @param sessionID Session ID.
     * @throws InvalidSessionIdException Throws an exception when there does not
     * exist a session of given ID.
     */
    Void logout(String sessionID)
            throws InvalidSessionIdException;

    /**
     * Send an email with a link to password reset to the user's email address.
     *
     * @param username registered username of the user
     * @return true on success, false if username is incorrect or there is no
     * email address
     */
    Boolean sendChangePasswordMail(String username);

    /**
     * Send an email with a link to password reset to the user's email address.
     *
     * @param email registered e-mail address of the user
     * @return true on success, false if username is incorrect or there is no
     * email address
     */
    Boolean sendChangePasswordMailByMail(String email)
            throws InvalidValueException;

    /**
     * Change password in case of forgotten password; user chooses a new
     * password, user authentication is done by the token sent to user's email
     * There needs to be a valid token for changing the password.
     *
     * @param username User name
     * @param password Password
     * @param token Generated token for password change identification
     * @return flag - success changing
     */
    Boolean changePassword(String username, String password, String token);

    ////////////////////////////////////////
    //                                    //
    //  OpenID login                      //
    //                                    //
    ////////////////////////////////////////
    /**
     * Get the URL of a window to show to the user for him to log in using his
     * OpenID
     *
     * @param serviceType Type of service user is using to log in (e.g. Google,
     * Seznam etc.)
     * @return Pair of authentication token used for asking for session ID and
     * URL where the user should authenticate.
     */
    LoginSessionResponse getAuthenticationURL(AuthenticationServiceType serviceType);

    /**
     * Send the URL of the response from the OpenID provider to Userspace for
     * validation.
     *
     * @param authID Token used for identification of the Open ID respond
     * @param responseURL URL where the OpenID proveider redirected the user
     * @return Flag if the authentication was successful
     */
    Boolean validateAuthentication(int authID, String responseURL);

    /**
     * Poll the Userspace to find out whether the user has already successfully
     * logged in using his OpenID.
     *
     * @param authID Token used for identification of the request
     * @return Pair of newly generated session ID and user object containing the
     * user settings
     * @throws AuthenticationFailedException Throws an exception if the
     * authentication failed
     * @throws InvalidValueException Throws an exception if an email address is
     * provided in wrong format.
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
     *
     * @param level Level of the message (e.g. warn, info)
     * @param context Context in which the logged event occurred (e.g. logging
     * in, creating document)
     * @param message Message of the log
     * @param sessionID Session ID of the session where the event happened if a
     * user was looged in that time
     */
    Void logGuiMessage(LevelLogEnum level, String context, String message, String sessionID);
    
    
    
    
    Long getShareId(Document doc);
    
    Void addDocument(String shareId, User user) throws InvalidShareIdException;
    
    Void lockTranslationResult(TranslationResult tResult, String sessionID) throws InvalidSessionIdException, AlreadyLockedException;
    
    Void unlockTranslationResult(ChunkIndex chunkIndex, Long documentId, String sessionID) throws InvalidSessionIdException;
}

package cz.filmtit.client;

import cz.filmtit.client.callables.*;
import cz.filmtit.client.dialogs.Dialog;
import cz.filmtit.client.pages.AuthenticationValidationWindow;
import cz.filmtit.client.pages.TranslationWorkspace;
import cz.filmtit.client.pages.TranslationWorkspace.SendChunksCommand;
import cz.filmtit.client.pages.UserPage;
import cz.filmtit.share.AuthenticationServiceType;
import cz.filmtit.share.ChunkIndex;
import cz.filmtit.share.MediaSource;
import cz.filmtit.share.TimedChunk;

import java.util.List;

/**
 * A static class that provides access to Remote Procedure Calls to the FilmTit server.
 * @author rur
 *
 */
public final class FilmTitServiceHandler {
	
// disabled only to avoid duplicities, can be reenabled by uncommenting anytime if needed
//    public void loadDocumentFromDB(Document document) {
//        new LoadDocumentFromDB(document.getId());
//    }

    static public void loadDocumentFromDB(long documentId) {
        new LoadDocumentFromDB(documentId);    		
    }

    static public void createDocument(String documentTitle, String movieTitle, String language, String subtext, String subformat, String moviePath) {
		new CreateDocument(documentTitle, movieTitle, language, subtext, subformat, moviePath);
	}
	
    static public void saveSourceChunks(List<TimedChunk> chunks, TranslationWorkspace workspace) {
		new SaveSourceChunks(chunks, workspace);
	}

    static public void getTranslationResults(List<TimedChunk> chunks, SendChunksCommand command, TranslationWorkspace workspace) {
		new GetTranslationResults(chunks, command, workspace);
	}

    static public void setUserTranslation(ChunkIndex chunkIndex, long documentId, String userTranslation, long chosenTranslationPair) {
		new SetUserTranslation(chunkIndex, documentId, userTranslation, chosenTranslationPair);
	}

    static public void selectSource(long documentID, MediaSource selectedMediaSource, TranslationWorkspace workspace) {
    	new SelectSource(documentID, selectedMediaSource, workspace);
    }

    static public void checkSessionID() {
		if (Gui.getGui().getSessionID() == null) {
			Gui.getGui().logged_out();
    	}
		else {
	    	new CheckSessionID();
        }
    }
    
    static public void registerUser(String username, String password, String email, Dialog loginDialog) {
    	new RegisterUser(username, password, email, loginDialog);
    }
    
    static public void sendChangePasswordMail (String username, Dialog loginDialog) {
    	new SendChangePasswordMail(username, loginDialog);
    }
    
    /**
     * change password in case of forgotten password;
     * user chooses a new password,
     * user authentication is done by the token sent to user's email
     * @param username
     * @param password
     * @param token
     */
    static public void changePassword(String username, String password, String token) {
    	new ChangePasswordCallable(username, password, token);
    }
    
    /**
     * Log in the user
     * @param username
     * @param password
     * @param loginDialog can be null if we are "sure" the password is OK (i.e. after setting it)
     */
    static public void simpleLogin(String username, String password, Dialog loginDialog) {
    	new SimpleLogin(username, password, loginDialog);
	}

    static public void logout() {    	new Logout();
    }
    
    static public void getAuthenticationURL(AuthenticationServiceType serviceType, Dialog loginDialog) {
		new GetAuthenticationURL(serviceType, loginDialog);
	}
	
    static public void validateAuthentication (String responseURL, int authID, AuthenticationValidationWindow authenticationValidationWindow) {
		new ValidateAuthentication(responseURL, authID, authenticationValidationWindow);
	}
	
    static public void getListOfDocuments(UserPage userpage) {
    	new GetListOfDocuments(userpage);
    }

    static public void deleteDocument(long id) {
		new DeleteDocument(id);
	}
	
    private FilmTitServiceHandler() {
    	assert false : "FilmTitServiceHandler is a static class";
    }
}

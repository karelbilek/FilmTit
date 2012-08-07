package cz.filmtit.client;

import com.github.gwtbootstrap.client.ui.Modal;
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

public class FilmTitServiceHandler {
	
// disabled only to avoid duplicities, can be reenabled by uncommenting anytime if needed
//    public void loadDocumentFromDB(Document document) {
//        new LoadDocumentFromDB(document.getId());
//    }

    public void loadDocumentFromDB(long documentId) {
        new LoadDocumentFromDB(documentId);    		
    }


	public void createDocument(String documentTitle, String movieTitle, String language, String subtext, String subformat, String moviePath) {
		new CreateDocument(documentTitle, movieTitle, language, subtext, subformat, moviePath, this);
	}
	
	
	public void saveSourceChunks(List<TimedChunk> chunks, TranslationWorkspace workspace) {
		new SaveSourceChunks(chunks, workspace);
	}

	
	public void getTranslationResults(List<TimedChunk> chunks, SendChunksCommand command, TranslationWorkspace workspace) {
		new GetTranslationResults(chunks, command, workspace);
	}

	
	public void setUserTranslation(ChunkIndex chunkIndex, long documentId, String userTranslation, long chosenTranslationPair) {
		new SetUserTranslation(chunkIndex, documentId, userTranslation, chosenTranslationPair);
	}

	
    public void selectSource(long documentID, MediaSource selectedMediaSource, TranslationWorkspace workspace) {
    	new SelectSource(documentID, selectedMediaSource, workspace);
    }

    

    public void checkSessionID() {
		if (Gui.getGui().getSessionID() == null) {
			Gui.getGui().logged_out();
    	}
		else {
	    	new CheckSessionID();
        }
    }
    
   

    public void registerUser(String username, String password, String email, Dialog loginDialog) {
    	new RegisterUser(username, password, email, loginDialog, this);
    }
    
    
    
    public void sendChangePasswordMail (String username, Dialog loginDialog) {
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
    public void changePassword(String username, String password, String token) {
    	new ChangePasswordCallable(username, password, token, this);
    }
    
  
    /**
     * Log in the user
     * @param username
     * @param password
     * @param loginDialog can be null if we are "sure" the password is OK (i.e. after setting it)
     */
    public void simpleLogin(String username, String password, Dialog loginDialog) {
    	new SimpleLogin(username, password, loginDialog);
	}



    public void logout() {    	new Logout();
    }
    

	public void getAuthenticationURL(AuthenticationServiceType serviceType, Dialog loginDialog) {
		new GetAuthenticationURL(serviceType, loginDialog, this);
	}
	
	

	    
	public void validateAuthentication (String responseURL, int authID, AuthenticationValidationWindow authenticationValidationWindow) {
		new ValidateAuthentication(responseURL, authID, authenticationValidationWindow);
	}
	


    public void getListOfDocuments(UserPage userpage) {
    	new GetListOfDocuments(userpage);
    }


	public void deleteDocument(long id) {
		new DeleteDocument(id);
	}


    

	    
}

package cz.filmtit.client;

import com.github.gwtbootstrap.client.ui.Modal;
import cz.filmtit.client.callables.*;
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

	
    public void selectSource(long documentID, MediaSource selectedMediaSource) {
    	new SelectSource(documentID, selectedMediaSource);
    }

    

    public void checkSessionID() {
		if (Gui.getGui().getSessionID() == null) {
			Gui.getGui().logged_out();
    	}
		else {
	    	new CheckSessionID();
        }
    }
    
   

    public void registerUser(String username, String password, String email, Modal registrationForm) {
    	new RegisterUser(username, password, email, registrationForm, this);
    }
    
    
    
    public void sendChangePasswordMail (String username, Modal dialogBox) {
    	new SendChangePasswordMail(username, dialogBox);
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
    
  

    public void simpleLogin(String username, String password) {
    	new SimpleLogin(username, password);
	}



    public void logout() {    	new Logout();
    }
    

	public void getAuthenticationURL(AuthenticationServiceType serviceType, Modal loginDialogBox) {
		new GetAuthenticationURL(serviceType, loginDialogBox, this);
	}
	
	

	    
	public void validateAuthentication (String responseURL, long authID, AuthenticationValidationWindow authenticationValidationWindow) {
		new ValidateAuthentication(responseURL, authID, authenticationValidationWindow);
	}
	


    public void getListOfDocuments(UserPage userpage) {
    	new GetListOfDocuments(userpage);
    }


    

	    
}

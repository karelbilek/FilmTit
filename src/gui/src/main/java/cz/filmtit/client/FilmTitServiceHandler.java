package cz.filmtit.client;

import cz.filmtit.client.callables.*;
import com.github.gwtbootstrap.client.ui.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;

import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DialogBox;
import cz.filmtit.client.PageHandler.Page;
import cz.filmtit.client.TranslationWorkspace.SendChunksCommand;
import cz.filmtit.share.*;
import cz.filmtit.share.exceptions.InvalidSessionIdException;

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
    
   

    public void registerUser(String username, String password, String email, DialogBox registrationForm) {
    	new RegisterUser(username, password, email, registrationForm, this);
    }
    
    
    
    public void sendChangePasswordMail (String username, DialogBox dialogBox) {
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


            public void onSuccess(String SessionID) {
            	if (SessionID == null || SessionID.equals("")) {
            		gui.log("ERROR: simple login didn't succeed - incorrect username or password.");
            		displayWindow("ERROR: simple login didn't succeed - incorrect username or password.");
                    gui.showLoginDialog(username);
            	} else {
            		gui.log("logged in as " + username + " with session id " + SessionID);
            		gui.setSessionID(SessionID);
            		gui.logged_in(username);
            	}
            }

            public void onFailure(Throwable caught) {
            	gui.log("ERROR: simple login didn't succeed!");
            }
        };
		
        // constructor
        public SimpleLogin(String username, String password) {
			super();
			
			this.username = username;
			this.password = password;

	        enqueue();
		}

		@Override
		public void call() {
	        filmTitService.simpleLogin(username, password, callback);
		}
    }
    public void logout() {    	new Logout();
    }
    

	public void getAuthenticationURL(AuthenticationServiceType serviceType, DialogBox loginDialogBox) {
		new GetAuthenticationURL(serviceType, loginDialogBox, this);
	}
	
	

	    
	public void validateAuthentication (String responseURL, long authID, AuthenticationValidationWindow authenticationValidationWindow) {
		new ValidateAuthentication(responseURL, authID, authenticationValidationWindow);
	}
	


    public void getListOfDocuments(UserPage userpage) {
    	new GetListOfDocuments(userpage);
    }


    

	    
}

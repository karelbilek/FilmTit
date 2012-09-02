package cz.filmtit.client.callables;
import cz.filmtit.client.Callable;
import cz.filmtit.client.Gui;
import cz.filmtit.client.dialogs.Dialog;
import cz.filmtit.client.dialogs.LoginDialog;
import cz.filmtit.share.SessionResponse;

/**
 * Try to log in the user with the given username and password.
 * Sets the logged in state accordingly, informing the user about errors.
 * @author rur
 *
 */
public class SimpleLogin extends Callable<SessionResponse> {
	
	// parameters
	private String username;
	private String password;
	private Dialog loginDialog;

    @Override
    public String getName() {
        return getNameWithParameters(username);
    }

    @Override
    public void onSuccessAfterLog(SessionResponse response) {
    	if (response == null) {
    		Gui.log("ERROR: simple login didn't succeed - incorrect username or password.");
    		if (loginDialog != null) {
        		loginDialog.reactivateWithErrorMessage("Incorrect username or password - please try again.");            			
    		} else {
    			// this is weird, means that the password was set just before calling that, this shouldn't happen
    			new LoginDialog(username);
    		}
    	} else {
    		Gui.setSessionID(response.sessionID);
    		Gui.log("logged in as " + username + " with session id " + response.sessionID);
    		if (loginDialog != null) {
    			loginDialog.close();
    		}            		
    		Gui.logged_in(response.userWithoutDocs);
    	}
    }

    @Override
    protected void onFinalError(String message) {
        String fullMessage = "There was an error with logging in. " +
    		"Please try again. " +
            "If problems persist, try contacting the administrators. " +
            "Error message from the server: " + message;
    	if (loginDialog != null) {
    		loginDialog.reactivateWithErrorMessage(fullMessage);
    	} else {
    		new LoginDialog(username, fullMessage);
    	}
    }
    
    /**
	 * Try to log in the user with the given username and password.
	 * Sets the logged in state accordingly, informing the user about errors.
     * @param dialog can be null if we are "sure" the password is OK (i.e. after setting it)
     */
    public SimpleLogin(String username, String password, Dialog dialog) {
		super();
		
		this.username = username;
		this.password = password;
		this.loginDialog = dialog;

        enqueue();
	}

	@Override protected void call() {
        filmTitService.simpleLogin(username, password, this);
	}
}


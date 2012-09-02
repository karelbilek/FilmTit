package cz.filmtit.client.callables;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;

import cz.filmtit.client.*;
import cz.filmtit.client.pages.AuthenticationValidationWindow;

import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.core.client.*;
import cz.filmtit.share.*;
import java.util.*;

/**
 * Validate the response URL from the OpenID provider, which contains information about the result of the OpenID authentication.
 * If the authentication is found to have been successful, a new session is generated for the user and paired with the given authID.
 * The AuthenticationValidationWindow is then closed.
 * Otherwise, an error message is displayed in the AuthenticationValidationWindow.
 * @author rur
 *
 */
public class ValidateAuthentication extends Callable<Boolean> {

	// parameters
	private String responseURL;	
	private int authID;
	private AuthenticationValidationWindow authenticationValidationWindow;

    @Override
	public void onSuccessAfterLog(Boolean result) {
		if (result) {
			authenticationValidationWindow.loggedIn();
		}
		else {
			authenticationValidationWindow.logInFailed();
		}
	}
    
    @Override
    protected void onFinalError(String message) {
		authenticationValidationWindow.logInFailed(message);
    }
		
        
	/**
	 * Validate the response URL from the OpenID provider, which contains information about the result of the OpenID authentication.
	 * If the authentication is found to have been successful, a new session is generated for the user and paired with the given authID.
	 * The AuthenticationValidationWindow is then closed.
	 * Otherwise, an error message is displayed in the AuthenticationValidationWindow.
	 * @see GetAuthenticationURL
	 */
	public ValidateAuthentication(String responseURL, int authID,
			AuthenticationValidationWindow authenticationValidationWindow) {
		super();
		
		this.responseURL = responseURL;
		this.authID = authID;
		this.authenticationValidationWindow = authenticationValidationWindow;
		
		enqueue();
	}
			
	@Override
	protected void call() {
		filmTitService.validateAuthentication(authID, responseURL, this);		
	}
}

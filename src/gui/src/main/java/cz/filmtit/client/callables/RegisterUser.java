package cz.filmtit.client.callables;

import com.github.gwtbootstrap.client.ui.Modal;
import cz.filmtit.client.Callable;
import cz.filmtit.client.Gui;
import cz.filmtit.client.PageHandler.Page;
import cz.filmtit.client.dialogs.Dialog;

/**
 * Register a user with the given username and password, also setting the e-mail address if provided and sending registration info to it.
 * Logs in the user on success, informs the user in case of errors (the username is already taken or other errors, such as an invalid e-mail address).
 * @author rur
 *
 */
public class RegisterUser extends Callable<Boolean> {
    	
    	// parameters
    	private String username;
    	private String password;
    	private String email;
    	private Dialog loginDialog;
    	private String openid = null;

        @Override
        public String getName() {
            return getNameWithParameters(username, password, email);
        }


        @Override
        public void onSuccessAfterLog(Boolean result) {
            if (result) {
                loginDialog.close();
                Gui.log("registered as " + username);
                Gui.getPageHandler().setPageUrl(Page.DocumentCreator);
                new SimpleLogin(username, password, null);
                displayWindow("You successfully registered with the username '" + username + "'!");
            } else {
                // false means unavailable username
                Gui.log("ERROR: registration didn't succeed, username " + username + " already taken.");
                loginDialog.reactivateWithErrorMessage("The username '" + username + "' is not available. Please choose a different username.");
            }
        }

        @Override
        protected void onFinalError(String message) {
            loginDialog.reactivateWithErrorMessage("There was an error with the registration. " +
            		"Please try again. " +
                    "If problems persist, try contacting the administrators. " +
                    "Error message: " + message);
        };

        /**
		 * Register a user with the given username and password, also setting the e-mail address if provided and sending registration info to it.
		 * Logs in the user on success, informs the user in case of errors (the username is already taken or other errors, such as an invalid e-mail address).
         */
		public RegisterUser(String username, String password, String email,
				Dialog registrationForm) {
			super();
			
			this.username = username;
			this.password = password;
			this.email = email;
			this.loginDialog = registrationForm;
			
			enqueue();
		}

		@Override protected void call() {
	        filmTitService.registration(username, password, email, openid, this);
		}
    }

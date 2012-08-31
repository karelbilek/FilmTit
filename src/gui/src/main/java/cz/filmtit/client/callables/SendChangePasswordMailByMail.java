package cz.filmtit.client.callables;

import com.github.gwtbootstrap.client.ui.Modal;
import cz.filmtit.client.Callable;
import cz.filmtit.client.Gui;
import cz.filmtit.client.dialogs.Dialog;

 public class SendChangePasswordMailByMail extends Callable<Boolean> {
    	
    	// parameters
    	String email;
    	String username;
    	Dialog loginDialog;

        @Override
        public String getName() {
            return getNameWithParameters(email);
        }


        @Override
        public void onSuccessAfterLog(Boolean result) {
            if (result) {
                loginDialog.close();
                displayWindow("A link to password change page has been sent to your e-mail address.");
            } else {
                // false = bad email
                Gui.log("ERROR: sendChangePasswordMail didn't succeed, bad email.");
                if (username == null) {
                	// did not try with username
                    loginDialog.reactivateWithErrorMessage("There was an error sending password change email to you. " +
                            "There is no user with the e-mail address '" + email + "' registered. " +
                            "Please check the e-mail address or make a new registration. " +
                			"You can also try filling in you username instead.");
                }
                else {
                	// already failed with the username
                    loginDialog.reactivateWithErrorMessage("There was an error sending password change email to you. " +
                            "There is no user with the e-mail address '" + email + "' registered, " +
                            "and either the username '" + username + "' is not registered " +
                            "or there is no e-mail address associated with it. " +
                            "Please check the username and e-mail address, or make a new registration. ");
                }
            }
        }

        @Override
        protected void onFinalError(String message) {
            loginDialog.reactivateWithErrorMessage("There was an error sending password change email to you. " +
            		"Please try again. " +
                    "If problems persist, try contacting the administrators. " +
                    "Error message: " + message);
        }
	    	
        /**
         * 
         * @param email An email to which to try to send the password change link.
         * @param username A username with which {@link SendChangePasswordMail} already failed,
         * or null if user did not give any username.
         */
		public SendChangePasswordMailByMail(String email, String username, Dialog loginDialog) {
			super();
			
			this.email = email;
			this.username = username;
			this.loginDialog = loginDialog;
			
			enqueue();
		}

		@Override protected void call() {
			filmTitService.sendChangePasswordMailByMail(email, this);
		}    	
    }


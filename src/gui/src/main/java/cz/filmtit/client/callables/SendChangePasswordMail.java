package cz.filmtit.client.callables;

import com.github.gwtbootstrap.client.ui.Modal;
import cz.filmtit.client.Callable;
import cz.filmtit.client.Gui;
import cz.filmtit.client.dialogs.Dialog;

 public class SendChangePasswordMail extends Callable<Boolean> {
    	
    	// parameters
    	String username;
    	String email;
    	Dialog loginDialog;

        @Override
        public String getName() {
            return "sendChangePasswordMail("+username+")";
        }


        @Override
        public void onSuccessAfterLog(Boolean result) {
            if (result) {
                loginDialog.close();
                Gui.log("successful sendChangePasswordMail for " + username);
                displayWindow("A link to password change page has been sent to your e-mail address.");
            } else {
                // false = bad username or no email
                Gui.log("ERROR: sendChangePasswordMail didn't succeed, bad username or no email.");
            	if (email == null) {
                    loginDialog.reactivateWithErrorMessage("There was an error sending password change email to you. " +
                            "Either the username '" + username + "' is not registered " +
                                    "or there is no e-mail address associated with it. " +
                                    "Please check the username or register with a new one. " +
                                    "You can also try filling in you e-mail address instead.");
            	}
            	else {
                    Gui.log("Will retry with email " + email);
            		new SendChangePasswordMailByMail(email, username, loginDialog);
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
    	 * @param username A username to which associated email address to try to send the password change link.
    	 * @param email An email addres to try to use i {@link SendChangePasswordMailByMail} if this call fails.
    	 * @param loginDialog
    	 */
		public SendChangePasswordMail(String username, String email, Dialog loginDialog) {
			super();
			
			this.username = username;
			this.email = email;
			this.loginDialog = loginDialog;
			
			enqueue();
		}

		@Override protected void call() {
			filmTitService.sendChangePasswordMail(username, this);
		}    	
    }


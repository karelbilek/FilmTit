package cz.filmtit.client.callables;

import com.github.gwtbootstrap.client.ui.Modal;
import cz.filmtit.client.Callable;
import cz.filmtit.client.dialogs.Dialog;

 public class SendChangePasswordMail extends Callable<Boolean> {
    	
    	// parameters
    	String username;
    	Dialog loginDialog;

        @Override
        public String getName() {
            return "sendChangePasswordMail("+username+")";
        }


        @Override
        public void onSuccessAfterLog(Boolean result) {
            if (result) {
                loginDialog.close();
                gui.log("successful sendChangePasswordMail for " + username);
                displayWindow("A link to password change page has been sent to your e-mail address.");
            } else {
                // false = bad username or no email
                gui.log("ERROR: sendChangePasswordMail didn't succeed, bad username or no email.");
                loginDialog.reactivateWithErrorMessage("There was an error sending password change email to you. " +
                        "Either the username '" + username + "' is not registered " +
                                "or there is no e-mail address associated with it. " +
                                "Please check the username or register with a new one. " +
                                "(You can also try to contact the administrators.)");
            }
        }

        @Override
        public void onFailureAfterLog(Throwable returned) {
            loginDialog.reactivateWithErrorMessage("There was an error sending password change email to you. " +
        		"Please try again. " +
                "If problems persist, try contacting the administrators. " +
                "Error message from the server: " + returned);
        }
	    	
    	// constructor
		public SendChangePasswordMail(String username, Dialog loginDialog) {
			super();
			
			this.username = username;
			this.loginDialog = loginDialog;
			
			enqueue();
		}

		@Override
		public void call() {
			filmTitService.sendChangePasswordMail(username, this);
		}    	
    }

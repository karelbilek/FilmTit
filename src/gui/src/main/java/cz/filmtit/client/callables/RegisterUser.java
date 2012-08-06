package cz.filmtit.client.callables;

import com.github.gwtbootstrap.client.ui.Modal;
import cz.filmtit.client.Callable;
import cz.filmtit.client.FilmTitServiceHandler;
import cz.filmtit.client.dialogs.Dialog;

public class RegisterUser extends Callable<Boolean> {
    	
    	// parameters
    	String username;
    	String password;
    	String email;
    	Dialog loginDialog;
    	String openid = null;
        FilmTitServiceHandler handler;

        @Override
        public String getName() {
            return "RegisterUser("+username+","+password+","+email+")";
        }


        @Override
        public void onSuccessAfterLog(Boolean result) {
            if (result) {
                loginDialog.close();
                gui.log("registered as " + username);
                handler.simpleLogin(username, password, null);
                displayWindow("You successfully registered with the username '" + username + "'!");
            } else {
                // TODO: bool means unavailable username, right? Or are there other reasons for failing?
                gui.log("ERROR: registration didn't succeed, username " + username + " already taken.");
                loginDialog.reactivateWithErrorMessage("The username '" + username + "' is not available. Please choose a different username.");
            }
        }

        @Override
        public void onFailureAfterLog(Throwable returned) {
            loginDialog.reactivateWithErrorMessage("There was an error with the registration. " +
            		"Please try again. " +
                    "If problems persist, try contacting the administrators. " +
                    "Error message from the server: " + returned);
        }

        // constructor
		public RegisterUser(String username, String password, String email,
				Dialog registrationForm, FilmTitServiceHandler handler) {
			super();
			
			this.username = username;
			this.password = password;
			this.email = email;
			this.loginDialog = registrationForm;
			this.handler= handler;
			
			// 20s
			callTimeOut = 20000;
			
			enqueue();
		}

		@Override protected void call() {
	        filmTitService.registration(username, password, email, openid, this);
		}
    }

package cz.filmtit.client.callables;

import com.github.gwtbootstrap.client.ui.Modal;
import cz.filmtit.client.Callable;
import cz.filmtit.client.FilmTitServiceHandler;

public class RegisterUser extends Callable<Boolean> {
    	
    	// parameters
    	String username;
    	String password;
    	String email;
    	Modal registrationForm;
    	String openid = null;
        FilmTitServiceHandler handler;

        @Override
        public String getName() {
            return "RegisterUser("+username+","+password+","+email+")";
        }


        @Override
        public void onSuccessAfterLog(Boolean result) {
            if (result) {
                registrationForm.hide();
                gui.log("registered as " + username);
                handler.simpleLogin(username, password);
                displayWindow("You successfully registered with the username '" + username + "'!");
            } else {
                // TODO: bool means unavailable username, right? Or are there other reasons for failing?
                gui.log("ERROR: registration didn't succeed, username already taken.");
                displayWindow("The username '" + username + "' is not available. Please choose a different username.");
                //registrationForm.txtUsername.focus();
            }
        }


        // constructor
		public RegisterUser(String username, String password, String email,
				Modal registrationForm, FilmTitServiceHandler handler) {
			super();
			
			this.username = username;
			this.password = password;
			this.email = email;
			this.registrationForm = registrationForm;
			this.handler= handler;
			enqueue();
		}

		@Override
		public void call() {
	        filmTitService.registration(username, password, email, openid, this);
		}
    }

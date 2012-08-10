package cz.filmtit.client.callables;
import cz.filmtit.client.Callable;
import cz.filmtit.client.dialogs.Dialog;
import cz.filmtit.client.dialogs.LoginDialog;
import cz.filmtit.share.SessionResponse;

    public class SimpleLogin extends Callable<SessionResponse> {
    	
    	// parameters
    	String username;
    	String password;
    	Dialog loginDialog;
    
        @Override
        public String getName() {
            return "simleLogin("+username+")";
        }

        @Override
        public void onSuccessAfterLog(SessionResponse response) {
        	if (response == null) {
        		gui.log("ERROR: simple login didn't succeed - incorrect username or password.");
        		if (loginDialog != null) {
            		loginDialog.reactivateWithErrorMessage("Incorrect username or password - please try again.");            			
        		} else {
        			// this is weird, means that the password was set just before calling that, this shouldn't happen
        			new LoginDialog(username);
        		}
        	} else {
        		gui.setSessionID(response.sessionID);
        		gui.log("logged in as " + username + " with session id " + response.sessionID);
        		if (loginDialog != null) {
        			loginDialog.close();
        		}            		
        		gui.logged_in(response.userWithoutDocs);
        	}
        }

        @Override
        public void onFailureAfterLog(Throwable returned) {
            String message = "There was an error with logging in. " +
	    		"Please try again. " +
	            "If problems persist, try contacting the administrators. " +
	            "Error message from the server: " + returned;
        	if (loginDialog != null) {
        		loginDialog.reactivateWithErrorMessage(message);
        	} else {
        		
        	}
        }
            
        // constructor
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


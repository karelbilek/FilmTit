package cz.filmtit.client.callables;
import cz.filmtit.client.Callable;

    public class SimpleLogin extends Callable<String> {
    	
    	// parameters
    	String username;
    	String password;
    
        @Override
        public String getName() {
            return "simleLogin("+username+")";
        }

            @Override
            public void onSuccessAfterLog(String SessionID) {
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

            
		
        // constructor
        public SimpleLogin(String username, String password) {
			super();
			
			this.username = username;
			this.password = password;

	        enqueue();
		}

		@Override
		public void call() {
	        filmTitService.simpleLogin(username, password, this);
		}
    }


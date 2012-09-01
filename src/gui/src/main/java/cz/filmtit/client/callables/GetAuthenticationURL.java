package cz.filmtit.client.callables;

import com.github.gwtbootstrap.client.ui.Modal;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Window;
import cz.filmtit.client.Callable;
import cz.filmtit.client.Gui;
import cz.filmtit.client.dialogs.Dialog;
import cz.filmtit.share.AuthenticationServiceType;
import cz.filmtit.share.LoginSessionResponse;


	public class GetAuthenticationURL extends Callable<LoginSessionResponse> {
		
		// parameters
		AuthenticationServiceType serviceType;
		Dialog loginDialog;

        @Override
		public String getName() {
            return getNameWithParameters(serviceType);
        }
        
        @Override
		public void onSuccessAfterLog(LoginSessionResponse response) {
	    	String url = response.getOpenIDURL();
	    	int authID = response.getAuthID();
			Gui.log("Authentication URL and authID arrived: " + authID + ", " + url);
			loginDialog.close();
			
            // open the authentication window
			Window.open(url, "AuthenticationWindow", "width=600,height=500");
			
			// start polling for SessionID
			new SessionIDPolling(authID);
        }
			
        @Override
        protected void onFinalError(String message) {
            loginDialog.reactivateWithErrorMessage("There was an error with opening the authentiaction page. " +
            		"Please try again. " +
                    "If problems persist, try contacting the administrators. " +
                    "Error message: " + message);
        }
					
		// constructor
		public GetAuthenticationURL(AuthenticationServiceType serviceType,
				Dialog loginDialog) {
			super();
			
			this.serviceType = serviceType;
			this.loginDialog = loginDialog;
			
			enqueue();
		}

		@Override protected void call() {
			filmTitService.getAuthenticationURL(serviceType, this);
		}
	}


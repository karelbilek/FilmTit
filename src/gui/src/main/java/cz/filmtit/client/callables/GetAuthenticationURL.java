package cz.filmtit.client.callables;

import com.github.gwtbootstrap.client.ui.Modal;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Window;
import cz.filmtit.client.Callable;
import cz.filmtit.client.FilmTitServiceHandler;
import cz.filmtit.client.dialogs.Dialog;
import cz.filmtit.share.AuthenticationServiceType;


	public class GetAuthenticationURL extends Callable<String> {
		
		// parameters
		AuthenticationServiceType serviceType;
		Dialog loginDialog;
		/**
		 * temporary ID for authentication
		 */
		private int authID;
        FilmTitServiceHandler handler;


        @Override
		public String getName() {
            return "GetAuthenticationURL";
        }
        
        // TODO: remove
        @Override
		public void onSuccessAfterLog(final String url) {
			gui.log("Authentication URL arrived: " + url);
			loginDialog.close();
			
            // open the authenticationwindow
			Window.open(url, "AuthenticationWindow", "width=400,height=500");
			
			// start polling for SessionID
			new SessionIDPolling(authID, handler);				
        }
			
//        // TODO: uncomment and finalize
//        @Override
//		public void onSuccessAfterLog(Object response) {
//	    	String url = response.getURL();
//	    	authID = response.getAuthID();
//			gui.log("Authentication URL and authID arrived: " + authID + ", " + url);
//			loginDialog.close();
//			
//            // open the authenticationwindow
//			Window.open(url, "AuthenticationWindow", "width=400,height=500");
//			
//			// start polling for SessionID
//			new SessionIDPolling(authID, handler);				
//        }
			
        @Override
        public void onFailureAfterLog(Throwable returned) {
            loginDialog.reactivateWithErrorMessage("There was an error with opening the authentiaction page. " +
            		"Please try again. " +
                    "If problems persist, try contacting the administrators. " +
                    "Error message from the server: " + returned);
        }
					
		// constructor
		public GetAuthenticationURL(AuthenticationServiceType serviceType,
				Dialog loginDialog, FilmTitServiceHandler handler) {
			super();
			
            this.handler = handler;
			this.serviceType = serviceType;
			this.loginDialog = loginDialog;
			
			enqueue();
		}

		@Override protected void call() {
			authID = Random.nextInt();
			filmTitService.getAuthenticationURL(authID, serviceType, this);
		}
	}


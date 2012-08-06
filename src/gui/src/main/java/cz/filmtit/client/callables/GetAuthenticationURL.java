package cz.filmtit.client.callables;

import com.github.gwtbootstrap.client.ui.Modal;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Window;
import cz.filmtit.client.Callable;
import cz.filmtit.client.FilmTitServiceHandler;
import cz.filmtit.share.AuthenticationServiceType;


	public class GetAuthenticationURL extends Callable<String> {
		
		// parameters
		AuthenticationServiceType serviceType;
		Modal loginDialogBox;
		/**
		 * temporary ID for authentication
		 */
		private int authID;
        FilmTitServiceHandler handler;


        @Override
		public String getName() {
            return "GetAuthenticationURL";
        }
            @Override
			public void onSuccessAfterLog(final String url) {
				gui.log("Authentication URL arrived: " + url);
				loginDialogBox.hide();
				
                // open the authenticationwindow
				Window.open(url, "AuthenticationWindow", "width=400,height=500");
				
				// start polling for SessionID
				new SessionIDPolling(authID, handler);				
            }
			
					
		// constructor
		public GetAuthenticationURL(AuthenticationServiceType serviceType,
				Modal loginDialogBox, FilmTitServiceHandler handler) {
			super();
			
            this.handler = handler;
			this.serviceType = serviceType;
			this.loginDialogBox = loginDialogBox;
			
			enqueue();
		}

		@Override
		public void call() {
			authID = Random.nextInt();
			filmTitService.getAuthenticationURL(authID, serviceType, this);
		}
	}


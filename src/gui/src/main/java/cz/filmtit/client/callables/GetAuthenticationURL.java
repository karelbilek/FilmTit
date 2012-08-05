package cz.filmtit.client.callables;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;

import cz.filmtit.client.*;

import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.core.client.*;
import cz.filmtit.share.*;
import java.util.Map;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.SortedMap;


	public class GetAuthenticationURL extends Callable<String> {
		
		// parameters
		AuthenticationServiceType serviceType;
		DialogBox loginDialogBox;
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
				DialogBox loginDialogBox, FilmTitServiceHandler handler) {
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


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
import java.util.*;

	public class ValidateAuthentication extends Callable<Boolean> {

            @Override
			public void onFailureAfterLog(Throwable caught) {
				authenticationValidationWindow.logInFailed(caught);
			}

		// parameters
		String responseURL;	
		long authID;
		AuthenticationValidationWindow authenticationValidationWindow;

        @Override
		public String getName() {
            return "ValidateAuthentication";
        }
            
            @Override
			public void onSuccessAfterLog(Boolean result) {
				if (result) {
					authenticationValidationWindow.loggedIn();
				}
				else {
					authenticationValidationWindow.logInFailed();
				}
			}
			
            
		
		public ValidateAuthentication(String responseURL, long authID,
				AuthenticationValidationWindow authenticationValidationWindow) {
			super();
			
			this.responseURL = responseURL;
			this.authID = authID;
			this.authenticationValidationWindow = authenticationValidationWindow;
			
			enqueue();
		}
				
		@Override
		public void call() {
			filmTitService.validateAuthentication(authID, responseURL, this);		
		}
	}

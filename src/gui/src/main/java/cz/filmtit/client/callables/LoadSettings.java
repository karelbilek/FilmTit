package cz.filmtit.client.callables;
import com.google.gwt.user.client.ui.*;

import cz.filmtit.client.*;

import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.core.client.*;
import cz.filmtit.share.*;
import java.util.*;
import com.google.gwt.user.client.*;

/**
 * Retrieves the User object with the user settings.
 * Passes them to the settingsReceiver on success.
 */
public class LoadSettings extends Callable<SessionResponse> {
    	
    	// parameters
    	private ReceivesSettings settingsReceiver;
    	
        @Override
        public void onSuccessAfterLog(SessionResponse response) {
            if (response != null) {
        		Gui.resetUser(response.userWithoutDocs);
            	settingsReceiver.onSettingsReceived(response.userWithoutDocs);
            } else {
                Gui.log("Warning: sessionID invalid.");
                Gui.logged_out();
            }
        }
        
        @Override
        protected void onInvalidSession() {
            Gui.logged_out();
        }
        
        @Override
        protected void onFinalError(String message) {
            Gui.logged_out();
        }

        /**
		 * Retrieves the User object with the user settings.
		 * Passes them to the settingsReceiver on success.
         */
    	public LoadSettings(ReceivesSettings settingsReceiver) {
    		super();
    		
    		this.settingsReceiver = settingsReceiver;
    		
        	enqueue();
    	}
    	
		@Override protected void call() {
            filmTitService.checkSessionID(Gui.getSessionID(), this);
		}
}

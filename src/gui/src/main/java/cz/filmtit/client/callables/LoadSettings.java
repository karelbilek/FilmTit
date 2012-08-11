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

public class LoadSettings extends Callable<SessionResponse> {
    	
    	// parameters
    	String sessionID;
    	ReceivesSettings settingsReceiver;
    	
        @Override
        public String getName() {
            return "checkSessionID ("+sessionID+")";
        }

        @Override
        public void onSuccessAfterLog(SessionResponse response) {
            if (response != null) {
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

        // constructor
    	public LoadSettings(ReceivesSettings settingsReceiver) {
    		super();
    		
    		this.settingsReceiver = settingsReceiver;
    		
        	enqueue();
    	}
    	
		@Override protected void call() {
            filmTitService.checkSessionID(Gui.getSessionID(), this);
		}
}

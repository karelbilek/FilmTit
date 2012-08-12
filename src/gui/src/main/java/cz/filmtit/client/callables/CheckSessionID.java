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

public class CheckSessionID extends Callable<SessionResponse> {
    	
    	// parameters
    	String sessionID;
    	
        @Override
        public String getName() {
            return getNameWithParameters(sessionID);
        }

        @Override
        public void onSuccessAfterLog(SessionResponse response) {
            if (response != null) {
                Gui.log("logged in as " + response.userWithoutDocs.getName() + " with session id " + response.sessionID);
                Gui.logged_in(response.userWithoutDocs);
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
    	public CheckSessionID() {
    		super();
    		
    		sessionID = Gui.getSessionID();
    		if (sessionID == null) {
                Gui.logged_out();
        		return;
        	}
    		else {
            	enqueue();
            }
    	}
    	
		@Override protected void call() {
			Gui.log("Checking sessionID " + sessionID);
            filmTitService.checkSessionID(sessionID, this);
		}
}

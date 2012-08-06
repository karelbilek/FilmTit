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

 // TODO will probably return the whole Session object - now returns username or null
public class CheckSessionID extends Callable<String> {
    	
    	// parameters
    	String sessionID;
    	
        @Override
        public String getName() {
            return "checkSessionID ("+sessionID+")";
        }

        @Override
        public void onSuccessAfterLog(String username) {
            if (username != null && username!="") {
                gui.log("logged in as " + username + " with session id " + sessionID);
                gui.logged_in(username);
            } else {
                gui.log("Warning: sessionID invalid.");
                gui.logged_out();
            }
        }

        @Override
        public void onFailureAfterLog(Throwable caught) {
            gui.logged_out();
        }

        // constructor
    	public CheckSessionID() {
    		super();
    		
    		sessionID = gui.getSessionID();
    		if (sessionID == null) {
                gui.logged_out();
        		return;
        	}
    		else {
            	enqueue();
            }
    	}
    	
		@Override protected void call() {
			gui.log("Checking sessionID " + sessionID);
            filmTitService.checkSessionID(sessionID, this);
		}
}

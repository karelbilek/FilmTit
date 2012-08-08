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
import cz.filmtit.share.exceptions.*;
import java.util.*;

    public class Logout extends Callable<Void> {
    	
        @Override
        public String getName() {
            return "Logout";     //  :-)
        }
        
        @Override
        public void onSuccessAfterLog(Void o) {
            gui.logged_out(true);
        }

        @Override
        public void onFailureAfterLog(Throwable caught) {
            if (caught.getClass().equals(InvalidSessionIdException.class)) {
                gui.log("already logged out");
                gui.logged_out(true);
            } else {
                gui.log("ERROR: logout didn't succeed! Forcing local logout... " + caught.getLocalizedMessage());
                gui.logged_out(true);
            }
        }
        
        @Override
        protected void onProbablyOffline(Throwable returned) {
            gui.log("ERROR: logout didn't succeed - probably offline! Forcing local logout... " + caught.getLocalizedMessage());
            gui.logged_out(true);
        }
    
        // constructor
		public Logout() {
			super();			
			enqueue();
		}

		@Override protected void call() {
	        filmTitService.logout(gui.getSessionID(), this);
		}
	}


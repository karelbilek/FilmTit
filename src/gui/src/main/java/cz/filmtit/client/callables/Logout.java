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
        public void onSuccessAfterLog(Void o) {
            Gui.logged_out(true);
        }
        
        // all errors on log out are ignored...
        
        @Override
        protected void onFinalError(String message) {
            Gui.log("ERROR: logout didn't succeed! Forcing local logout... " + message);
            Gui.logged_out(true);
        }
        
        @Override
        protected void onInvalidSession() {
            Gui.log("already logged out");
            Gui.logged_out(true);
        }
        
        // constructor
		public Logout() {
			super();			
			enqueue();
		}

		@Override protected void call() {
	        filmTitService.logout(Gui.getSessionID(), this);
		}
	}


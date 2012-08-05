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

public class SelectSource extends Callable<Void> {
    	
    	// parameters
    	long documentID;	
    	MediaSource selectedMediaSource;
   
        @Override
        public String getName() {
            return "selectSource("+documentID+")";
        }

        @Override
        public void onSuccessAfterLog(Void o) {
        }

        // constructor
		public SelectSource(long documentID, MediaSource selectedMediaSource) {
			super();
			
			this.documentID = documentID;
			this.selectedMediaSource = selectedMediaSource;
			
			enqueue();
		}

		@Override
		public void call() {
	        filmTitService.selectSource( gui.getSessionID(), documentID, selectedMediaSource, this);
		}
	}

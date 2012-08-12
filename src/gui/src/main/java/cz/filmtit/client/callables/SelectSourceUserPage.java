package cz.filmtit.client.callables;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;

import cz.filmtit.client.*;
import cz.filmtit.client.pages.TranslationWorkspace;

import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.core.client.*;
import cz.filmtit.share.*;
import java.util.*;

public class SelectSourceUserPage extends Callable<Void> {
    	
    	// parameters
    	long documentID;	
    	MediaSource selectedMediaSource;
   
        @Override
        public String getName() {
            return getNameWithParameters(documentID, selectedMediaSource);
        }

        @Override
        public void onSuccessAfterLog(Void o) {
            Gui.getPageHandler().refresh();
        }
        
        @Override
        protected void onFinalError(String message) {
            Gui.getPageHandler().refresh();
            super.onFinalError(message);
        }

        // constructor
		public SelectSourceUserPage(long documentID, MediaSource selectedMediaSource) {
			super();
			
			if (selectedMediaSource != null) {
				this.documentID = documentID;
				this.selectedMediaSource = selectedMediaSource;
				enqueue();
			} else {
				// ignore
	            Gui.getPageHandler().refresh();
			}
		}

		@Override protected void call() {
	        filmTitService.selectSource( Gui.getSessionID(), documentID, selectedMediaSource, this);
		}
	}

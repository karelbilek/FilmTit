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

	public class SessionIDPolling extends Callable<String> {

		/**
		 * temporary ID for authentication
		 */
		private int authID;

		/**
		 * dialog polling for session ID
		 */
		private DialogBox sessionIDPollingDialogBox;

		/**
		 * indicates whether polling for session ID is in progress
		 */
		private boolean sessionIDPolling = false;

        FilmTitServiceHandler handler;


        @Override
		public String getName() {
            return "sessionIDPolling("+authID+")";
        }

            @Override
			public void onSuccessAfterLog(String result) {
				if (result != null) {
					gui.log("A session ID received successfully! SessionId = " + result);
					// stop polling
					sessionIDPolling = false;
					sessionIDPollingDialogBox.hide();
					// we now have a session ID
					gui.setSessionID(result);
					// we have to get the username
					handler.checkSessionID();
					// gui.logged_in("");
				}
				else {
					gui.log("no session ID received");
					// and continue polling
				}
			}
			
            @Override
			public void onFailureAfterLog(Throwable caught) {
				if(sessionIDPolling) {
					// stop polling
					sessionIDPolling = false;
					sessionIDPollingDialogBox.hide();
					// say error
					displayWindow(caught.getLocalizedMessage());
					gui.log("failure on requesting session ID!");					
				}
			}
		
		// constructor
		public SessionIDPolling(int authID, FilmTitServiceHandler handler) {
			super();
			
			this.authID = authID;
			this.handler=handler;

			createDialog();
			
            startSessionIDPolling();
		}
		
		/**
		 * open a dialog saying that we are waiting for the user to authenticate
		 */
		private void createDialog() {
            sessionIDPollingDialogBox = new DialogBox(false);
            SessionIDPollingDialog dialog = new SessionIDPollingDialog();
            dialog.addCancelClickHandler( new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					sessionIDPolling = false;
                    gui.log("SessionIDPollingDialog closed by user hitting Cancel button");
                    sessionIDPollingDialogBox.hide();
				}
			});
            // TODO: call() if gets focus
            sessionIDPollingDialogBox.setWidget(dialog);
            sessionIDPollingDialogBox.setGlassEnabled(true);
            sessionIDPollingDialogBox.center();			
		}

		private void startSessionIDPolling() {
			sessionIDPolling = true;
			
			Scheduler.RepeatingCommand poller = new RepeatingCommand() {
				
				@Override
				public boolean execute() {
					if (sessionIDPolling) {
						// enqueue();
						call();			            
						return true;
					} else {
						return false;
					}
				}
			};
			
			Scheduler.get().scheduleFixedDelay(poller, 500);
		}

		@Override
		public void call() {
			if (sessionIDPolling) {
				gui.log("asking for session ID with authID=" + authID);
				filmTitService.getSessionID(authID, this);			
			}
		}
	}

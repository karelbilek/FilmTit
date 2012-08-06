package cz.filmtit.client.callables;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;

import cz.filmtit.client.*;
import cz.filmtit.client.dialogs.Dialog;
import cz.filmtit.client.dialogs.SessionIDPollingDialog;

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
		private Dialog sessionIDPollingDialog;

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
					sessionIDPollingDialog.close();
					// we now have a session ID
					gui.setSessionID(result);
					// we have to get the username
					handler.checkSessionID();
					// gui.logged_in("");
				}
				else {
					gui.log("no session ID received");
					// continue polling
					new EnqueueTimer(300);
				}
			}
            
            @Override
			public void onFailureAfterLog(Throwable caught) {
				if(sessionIDPolling) {
					// stop polling
					sessionIDPolling = false;
					sessionIDPollingDialog.close();
					// say error
					displayWindow("There was an error with your authentication. Message from the server: " + caught);
					gui.log("failure on requesting session ID! " + caught);					
				}
			}
		
		// constructor
		public SessionIDPolling(int authID, FilmTitServiceHandler handler) {
			super();
			
			this.authID = authID;
			this.handler=handler;

			// 20s
			callTimeOut = 20000;
			
			createDialog();
			
			sessionIDPolling = true;
            enqueue();
		}
		
		/**
		 * open a dialog saying that we are waiting for the user to authenticate
		 */
		private void createDialog() {
			sessionIDPollingDialog = new SessionIDPollingDialog(this);
		}

//		private void startSessionIDPolling() {
//			sessionIDPolling = true;
//			
//			enqueue();
//			
//			Scheduler.RepeatingCommand poller = new RepeatingCommand() {
//				
//				@Override
//				public boolean execute() {
//					if (sessionIDPolling) {
//						enqueue();
//						// call();
//						return true;
//					} else {
//						return false;
//					}
//				}
//			};
//			
//			Scheduler.get().scheduleFixedDelay(poller, 500);
//		}

		public void stopSessionIDPolling() {
			sessionIDPolling = false;
		}
		
		@Override protected void call() {
			if (sessionIDPolling) {
				gui.log("asking for session ID with authID=" + authID);
				filmTitService.getSessionID(authID, this);			
			}
		}
	}

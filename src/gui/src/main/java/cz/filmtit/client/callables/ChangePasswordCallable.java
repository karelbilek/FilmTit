package cz.filmtit.client.callables;

import cz.filmtit.client.*;
import com.google.gwt.user.client.ui.*;
import cz.filmtit.client.PageHandler.Page;

import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.core.client.*;
import cz.filmtit.share.*;
import java.util.*;
import com.google.gwt.user.client.*;

  /**
     * change password in case of forgotten password;
     * user chooses a new password,
     * user authentication is done by the token sent to user's email
     */
    public class ChangePasswordCallable extends Callable<Boolean> {
    	
    	// parameters
    	String username;
    	String password;
    	String token;
        FilmTitServiceHandler handler;

        @Override
        public String getName() {
            return "ChangePassword("+username+","+password+")";
        }

        public void onSuccessAfterLog(Boolean result) {
            if (result) {
                gui.log("changed password for user " + username);
                gui.getPageHandler().loadBlankPage();
                gui.getPageHandler().setPageUrl(Page.UserPage);
                handler.simpleLogin(username, password);
                displayWindow("You successfully changed the password for your username '" + username + "'!");
            } else {
                gui.log("ERROR: password change didn't succeed - token invalid");
                gui.showLoginDialog(username);
                displayWindow("ERROR: password change didn't succeed - the token is invalid, probably expired. " +
                        "Please try requesting a new password change token" +
                        "(by clicking the button labelled 'Forgotten password').");
            }
        }


        // constructor
		public ChangePasswordCallable(String username, String password, String token, FilmTitServiceHandler handler) {
			super();
			this.handler = handler;
			this.username = username;
			this.password = password;
			this.token = token;
			
			enqueue();
		}

		@Override
		public void call() {
	        filmTitService.changePassword(username, password, token, this);
		}
    }

package cz.filmtit.client.callables;
import com.google.gwt.user.client.*;

import cz.filmtit.client.*;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.core.client.*;
import cz.filmtit.share.*;
import java.util.*;

public class RegisterUser extends Callable<Boolean> {
    	
    	// parameters
    	String username;
    	String password;
    	String email;
    	DialogBox registrationForm;
    	String openid = null;
        FilmTitServiceHandler handler;

        @Override
        public String getName() {
            return "RegisterUser("+username+","+password+","+email+")";
        }


        @Override
        public void onSuccessAfterLog(Boolean result) {
            if (result) {
                registrationForm.hide();
                gui.log("registered as " + username);
                handler.simpleLogin(username, password);
                displayWindow("You successfully registered with the username '" + username + "'!");
            } else {
                // TODO: bool means unavailable username, right? Or are there other reasons for failing?
                gui.log("ERROR: registration didn't succeed, username already taken.");
                displayWindow("The username '" + username + "' is not available. Please choose a different username.");
                //registrationForm.txtUsername.focus();
            }
        }


        // constructor
		public RegisterUser(String username, String password, String email,
				DialogBox registrationForm, FilmTitServiceHandler handler) {
			super();
			
			this.username = username;
			this.password = password;
			this.email = email;
			this.registrationForm = registrationForm;
			this.handler= handler;
			enqueue();
		}

		@Override
		public void call() {
	        filmTitService.registration(username, password, email, openid, this);
		}
    }

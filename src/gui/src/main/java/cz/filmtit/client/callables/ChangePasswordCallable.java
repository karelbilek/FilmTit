package cz.filmtit.client.callables;

import cz.filmtit.client.*;
import com.google.gwt.user.client.ui.*;
import cz.filmtit.client.PageHandler.Page;
import cz.filmtit.client.dialogs.LoginDialog;
import cz.filmtit.client.dialogs.LoginDialog.Tab;

import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.core.client.*;
import cz.filmtit.share.*;
import java.util.*;
import com.google.gwt.user.client.*;

/**
 * Set a new password in case of forgotten password.
 * User authentication is done by a token sent to user's email.
 * The temporary password change token must be still valid,
 * and identical to one sent in the password reset e-mail
 * to the user with the given username.
 */
public class ChangePasswordCallable extends Callable<Boolean> {
	
	// parameters
	private String username;
	private String password;
	private String token;

    @Override
    public String getName() {
        return getNameWithParameters(username);
    }

    @Override
    public void onSuccessAfterLog(Boolean result) {
        if (result) {
            Gui.log("changed password for user " + username);
            Gui.getPageHandler().loadBlankPage();
            Gui.getPageHandler().setPageUrl(Page.UserPage);
            new SimpleLogin(username, password, null);
            displayWindow("You successfully changed the password for your username '" + username + "'!");
        } else {
            Gui.log("ERROR: password change didn't succeed - token invalid");
            Gui.getPageHandler().loadPage(Page.WelcomeScreen);
            new LoginDialog(username, Tab.ForgottenPassword,
        			"Password change didn't succeed - the token is invalid, probably expired. " +
                    "Please try requesting a new password change token"
    			);
        }
    }
    
    @Override
    protected void onFinalError(String message) {
        Gui.getPageHandler().loadPage(Page.WelcomeScreen);
        new LoginDialog(username, Tab.ForgottenPassword, message);
    }


    /**
	 * Set a new password in case of forgotten password.
     * User authentication is done by a token sent to user's email.
	 * The temporary password change token must be still valid,
	 * and identical to one sent in the password reset e-mail
	 * to the user with the given username.
     */
	public ChangePasswordCallable(String username, String password, String token) {
		super();
		this.username = username;
		this.password = password;
		this.token = token;
		
		enqueue();
	}

	@Override protected void call() {
        filmTitService.changePassword(username, password, token, this);
	}
}

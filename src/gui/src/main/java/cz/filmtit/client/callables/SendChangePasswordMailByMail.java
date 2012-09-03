/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/

package cz.filmtit.client.callables;

import com.github.gwtbootstrap.client.ui.Modal;
import cz.filmtit.client.Callable;
import cz.filmtit.client.Gui;
import cz.filmtit.client.dialogs.Dialog;

/**
 * Send a password reset e-mail to the given e-mail address.
 * The password reset link is bound to a username;
 * therefore, if there are multiple user accounts with the given e-mail address,
 * multiple password reset e-mail are sent to the e-mail address.
 * Informs the user about success or error.
 * @author rur
 *
 */

public class SendChangePasswordMailByMail extends Callable<Boolean> {
    	
	// parameters
	private String email;
	private String username;
	private Dialog loginDialog;

    @Override
    public String getName() {
        return getNameWithParameters(email);
    }


    @Override
    public void onSuccessAfterLog(Boolean result) {
        if (result) {
            loginDialog.close();
            displayWindow("A link to password change page has been sent to your e-mail address.");
        } else {
            // false = bad email
            Gui.log("ERROR: sendChangePasswordMail didn't succeed, bad email.");
            if (username == null) {
            	// did not try with username
                loginDialog.reactivateWithErrorMessage("There was an error sending password change email to you. " +
                        "There is no user with the e-mail address '" + email + "' registered. " +
                        "Please check the e-mail address or make a new registration. " +
            			"You can also try filling in you username instead.");
            }
            else {
            	// already failed with the username
                loginDialog.reactivateWithErrorMessage("There was an error sending password change email to you. " +
                        "There is no user with the e-mail address '" + email + "' registered, " +
                        "and either the username '" + username + "' is not registered " +
                        "or there is no e-mail address associated with it. " +
                        "Please check the username and e-mail address, or make a new registration. ");
            }
        }
    }

    @Override
    protected void onFinalError(String message) {
        loginDialog.reactivateWithErrorMessage("There was an error sending password change email to you. " +
        		"Please try again. " +
                "If problems persist, try contacting the administrators. " +
                "Error message: " + message);
    }
    	
    /**
	 * Send a password reset e-mail to the given e-mail address.
	 * The password reset link is bound to a username;
	 * therefore, if there are multiple user accounts with the given e-mail address,
	 * multiple password reset e-mail are sent to the e-mail address.
	 * Informs the user about success or error.
     * @param email An email to which to try to send the password change link.
     * @param username A username with which {@link SendChangePasswordMail} already failed,
     * or null if user did not give any username.
     */
	public SendChangePasswordMailByMail(String email, String username, Dialog loginDialog) {
		super();
		
		this.email = email;
		this.username = username;
		this.loginDialog = loginDialog;
		
		enqueue();
	}

	@Override protected void call() {
		filmTitService.sendChangePasswordMailByMail(email, this);
	}    	
}


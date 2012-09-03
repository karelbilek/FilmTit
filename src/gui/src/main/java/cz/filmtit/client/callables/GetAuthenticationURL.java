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
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Window;
import cz.filmtit.client.Callable;
import cz.filmtit.client.Gui;
import cz.filmtit.client.dialogs.Dialog;
import cz.filmtit.share.AuthenticationServiceType;
import cz.filmtit.share.LoginSessionResponse;

/**
 * Get the URL of a window to show to the user to log in using his OpenID account at an OpenID provider specified by serviceType. It leads to a web page of the OpenID provider, with the return page set to the FilmTit application, to the AuthenticationValidationWindow page.
 * A generated temporary one-time identifier, authID, is also included in the response. It is used to pair the authentication process, which takes place in the newly opened window, with the main GUI window.
 * On success opens a new window with the given URL
 * and starts polling User Space with the authID, using {@link SessionIDPolling}.
 * @author rur
 *
 */
public class GetAuthenticationURL extends Callable<LoginSessionResponse> {
	
	// parameters
	private AuthenticationServiceType serviceType;
	private Dialog loginDialog;

    @Override
	public String getName() {
        return getNameWithParameters(serviceType);
    }
    
    @Override
	public void onSuccessAfterLog(LoginSessionResponse response) {
    	String url = response.getOpenIDURL();
    	int authID = response.getAuthID();
		Gui.log("Authentication URL and authID arrived: " + authID + ", " + url);
		loginDialog.close();
		
        // open the authentication window
		Window.open(url, "AuthenticationWindow", "width=600,height=500");
		
		// start polling for SessionID
		new SessionIDPolling(authID);
    }
		
    @Override
    protected void onFinalError(String message) {
        loginDialog.reactivateWithErrorMessage("There was an error with opening the authentiaction page. " +
        		"Please try again. " +
                "If problems persist, try contacting the administrators. " +
                "Error message: " + message);
    }
				
	/**
	 * Get the URL of a window to show to the user to log in using his OpenID account at an OpenID provider specified by serviceType. It leads to a web page of the OpenID provider, with the return page set to the FilmTit application, to the AuthenticationValidationWindow page.
	 * A generated temporary one-time identifier, authID, is also included in the response. It is used to pair the authentication process, which takes place in the newly opened window, with the main GUI window.
	 * On success opens a new window with the given URL
	 * and starts polling User Space with the authID, using {@link SessionIDPolling}.
	 */
	public GetAuthenticationURL(AuthenticationServiceType serviceType,
			Dialog loginDialog) {
		super();
		
		this.serviceType = serviceType;
		this.loginDialog = loginDialog;
		
		enqueue();
	}

	@Override protected void call() {
		filmTitService.getAuthenticationURL(serviceType, this);
	}
}


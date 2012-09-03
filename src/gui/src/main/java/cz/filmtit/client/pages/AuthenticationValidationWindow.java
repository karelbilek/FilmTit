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

package cz.filmtit.client.pages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Paragraph;

import cz.filmtit.client.callables.ValidateAuthentication;

/**
 * A special page that receives OpenID authentication data from an OpenID provider and passes them to User Space for validation.
 * @author rur
 *
 */
public class AuthenticationValidationWindow extends Composite {

	private static AuthenticationValidationWindowUiBinder uiBinder = GWT
			.create(AuthenticationValidationWindowUiBinder.class);

	interface AuthenticationValidationWindowUiBinder extends
			UiBinder<Widget, AuthenticationValidationWindow> {
	}

	/**
	 * Creates the page and sends the URL to User Space for validation.
	 */
	public AuthenticationValidationWindow() {
		initWidget(uiBinder.createAndBindUi(this));

		RootPanel rootPanel = RootPanel.get();
		rootPanel.add(this, 0, 0);
		
      btnCancel.addClickHandler( new ClickHandler() {
             @Override
             public void onClick(ClickEvent event) {
                  // TODO: say to the UserSpace that I am closing the window
              close();
             }
        });

    	paraValidation.setText("Processing authentication data...");
    	
        // get authentication data
        
      	// response URL
        String responseURL = Window.Location.getHref();
        
        // auhID
        int authID = 0;
        String authIDstring = Window.Location.getParameter("authID");
        try {
             authID = Integer.parseInt(authIDstring);
        }
        catch (Exception e) {
             paraValidation.setText("Cannot parse authID '" + authIDstring + "' as a number! " + e);
             Window.alert("Cannot parse authID '" + authIDstring + "' as a number!");
             return;
        }

        // send RPC
        paraValidation.setText("Validating authentication data for authID '" + authID + "'...");
        new ValidateAuthentication (responseURL, authID, this);
    }
	
	/**
	 * strangely enough, there is no Window.close() in GWT
	 */
	native public void close()/*-{
		$wnd.close();
		// self.close();
	}-*/;

    @UiField
	Paragraph paraValidation;
	
    @UiField
	Button btnCancel;

    /**
     * Called when validation is successful.
     */
	public void loggedIn() {
		paraValidation.setText("Logged in successfully! You can now close this window.");
		// TODO: If user logged in for the first time, we need to create a username for him.
		// We can ask him to choose one, or we can just generate one from the data we get from the openid.
		btnCancel.setText("Close");
		close();
	}

	/**
	 * Called when validation fails.
	 */
	public void logInFailed() {
		paraValidation.setText("Not logged in! Authentication validation failed.");
		btnCancel.setText("Close");
	}

	/**
	 * Called when validation fails.
	 */
	public void logInFailed(String message) {
		paraValidation.setText("Not logged in! Authentication validation failed: " + message);
		btnCancel.setText("Close");
	}
	
	
}

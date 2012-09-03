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
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.github.gwtbootstrap.client.ui.Form;
import com.github.gwtbootstrap.client.ui.PasswordTextBox;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.SubmitButton;

import cz.filmtit.client.Gui;
import cz.filmtit.client.PageHandler.Page;
import cz.filmtit.client.callables.ChangePasswordCallable;

/**
 * This page is used as the target of the password change link sent by e-mail to users who forget their password, enabling them to set a new password.
 * @author rur
 *
 */
public class ChangePassword extends Composite {

	private static ChangePasswordUiBinder uiBinder = GWT
			.create(ChangePasswordUiBinder.class);

	interface ChangePasswordUiBinder extends UiBinder<Widget, ChangePassword> {
	}
	
	private String username;
	private String token;
	
	/**
	 * Shows the page and checks whether the URL contains all required parameters.
	 * If not, switches to Welcome Screen.
	 */
	public ChangePassword() {
		initWidget(uiBinder.createAndBindUi(this));
		
		username = Window.Location.getParameter("username");
		token = Window.Location.getParameter("token");
		
		if (username == null || token == null) {
			Gui.log("ERROR: username or token is null");
			Window.alert("Invalid parameters, cannot proceed!");
			
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				@Override
				public void execute() {
					Gui.getPageHandler().loadPage(Page.WelcomeScreen);
				}
			});
		}
		else {
	        Gui.getGuiStructure().contentPanel.setStyleName("changePassword");
	        Gui.getGuiStructure().contentPanel.setWidget(this);	
		}
		
	}

	@UiField
	Form changePasswordForm;
	
	@UiField
    PasswordTextBox txtPassword;

	@UiField
    PasswordTextBox txtPasswordRepeat;

	@UiField
	SubmitButton btnChangePassword;
	
	@UiHandler("changePasswordForm")
	void formSubmit(Form.SubmitEvent e) {
		if (activated) {
			if (checkForm()) {
				deactivate();
				new ChangePasswordCallable(username, getPassword(), token);
				Gui.log("trying to change password for user " + username);
			} else {
				// TODO tell user what errors there are
				Window.alert("Your password must have at least 3 characters " +
						"and must be repeated correctly!");
			}
		}
	}
	
	boolean activated = true;
	
	/**
	 * Disable the controls of the page.
	 */
	public void deactivate() {
		activated = false;
		txtPassword.setEnabled(false);
		txtPasswordRepeat.setEnabled(false);
		btnChangePassword.setEnabled(false);
	}
	
	private String getPassword() {
        return txtPassword.getText();
    }
        
    private boolean checkForm() {
        return checkPassword();
    }
    
    private boolean checkPassword() {
        return checkPasswordsMatch() && checkPasswordStrength();
    }
    
    private boolean checkPasswordsMatch() {
        String password = txtPassword.getText();
        String passwordRepeat = txtPasswordRepeat.getText();
    	if (password.equals(passwordRepeat)) {
    		return true;
    	} else {
    		return false;
    	}
	}
    
    private boolean checkPasswordStrength() {
        String password = getPassword();
        // TODO (but maybe keep that way)
        if (password.length() >= 3) {
        	return true;
        } else {
        	return false;
        }
    }
}

package cz.filmtit.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Widget;
import com.github.gwtbootstrap.client.ui.PasswordTextBox;
import com.github.gwtbootstrap.client.ui.Button;

import cz.filmtit.client.PageHandler.Page;
import cz.filmtit.share.AuthenticationServiceType;

public class ChangePassword extends Composite {

	private static ChangePasswordUiBinder uiBinder = GWT
			.create(ChangePasswordUiBinder.class);

	interface ChangePasswordUiBinder extends UiBinder<Widget, ChangePassword> {
	}
	
	private Gui gui;
	
	private String username;
	
	private String token;
	
	public ChangePassword(Gui gui) {
		initWidget(uiBinder.createAndBindUi(this));
		
		this.gui = gui;
		username = Window.Location.getParameter("username");
		token = Window.Location.getParameter("token");
		
		if (username == null || token == null) {
			Window.alert("Invalid parameters, cannot proceed!");
			gui.log("ERROR: username or token is null");
			// gui.pageHandler.loadPage(false, Page.WelcomeScreen);
		}
    }

	@UiField
    PasswordTextBox txtPassword;

	@UiField
    PasswordTextBox txtPasswordRepeat;

	@UiField
	Button btnChangePassword;
	
	@UiHandler("btnChangePassword")
	void handleClick(ClickEvent e) {
		if (checkForm()) {
			gui.rpcHandler.changePassword(username, getPassword(), token);
			gui.log("trying to change password for user " + username);
		} else {
			// TODO tell user what errors there are
			Window.alert("Please correct the form!");
		}
	}
	
//	@UiField
//	Button btnCancel;
//	
//	@UiHandler("btnCancel")
//	void handleClick(ClickEvent e) {
//        log("ChangePassword closed by user hitting Cancel button");
//        dialogBox.hide();	    
//	}

    public String getPassword() {
        return txtPassword.getText();
    }
        
    public boolean checkForm() {
        return checkPassword();
    }
    
    public boolean checkPassword() {
        return checkPasswordsMatch() && checkPasswordStrength();
    }
    
    public boolean checkPasswordsMatch() {
        String password = txtPassword.getText();
        String passwordRepeat = txtPasswordRepeat.getText();
    	if (password.equals(passwordRepeat)) {
    		return true;
    	} else {
    		return false;
    	}
	}
    
    public boolean checkPasswordStrength() {
        String password = getPassword();
        // TODO (but maybe keep that way)
        if (password.length() >= 3) {
        	return true;
        } else {
        	return false;
        }
    }
}

package cz.filmtit.client.dialogs;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Modal;
import com.github.gwtbootstrap.client.ui.PasswordTextBox;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import cz.filmtit.client.Dialog;
import cz.filmtit.client.Gui;

public class RegistrationForm extends Composite implements Dialog {

	private static RegistrationFormUiBinder uiBinder = GWT
			.create(RegistrationFormUiBinder.class);

	interface RegistrationFormUiBinder extends
			UiBinder<Widget, RegistrationForm> {
	}
	
	private Gui gui = Gui.getGui();

	public RegistrationForm() {
		initWidget(uiBinder.createAndBindUi(this));

        btnRegister.addClickHandler( new ClickHandler() {
               @Override
               public void onClick(ClickEvent event) {
            	   gui.log("Trying to register user...");
                // check data entered
                if (checkForm()) {
                    // invoke the registration
                	gui.rpcHandler.registerUser(getUsername(), getPassword(), getEmail(), dialogBox);
                } else {
                	gui.log("errors in registration form");
                     Window.alert("Please correct errors in registration form.");
                     // TODO: tell the user what is wrong
                }
               }
          });

        btnCancel.addClickHandler( new ClickHandler() {
               @Override
               public void onClick(ClickEvent event) {
            	   gui.log("RegistrationForm closed by user hitting Cancel button");
                dialogBox.hide();
               }
          });

        dialogBox.show();
	}

    @UiField
    Modal dialogBox;

	@UiField
	TextBox txtEmail;

	@UiField
	public TextBox txtUsername;

	@UiField
    PasswordTextBox txtPassword;

	@UiField
    PasswordTextBox txtPasswordRepeat;

	@UiField
	Button btnRegister;
	
	@UiField
	Button btnCancel;
	
    public String getEmail() {
        return txtEmail.getText();
    }

    public String getUsername() {
        return txtUsername.getText();
    }

    public String getPassword() {
        return txtPassword.getText();
    }
    
    
    public boolean checkForm() {
        return (!getUsername().isEmpty()) && checkPassword() && checkEmailValidity();
    }
    
    public boolean checkEmailValidity() {
        String email = getEmail();
        // TODO
    	if (email.length() >= 5 && email.contains("@") && email.contains(".")) {
    		return true;
    	} else {
    		return false;
    	}
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

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deactivate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reactivateWithErrorMessage(String message) {
		// TODO Auto-generated method stub
		
	}
}

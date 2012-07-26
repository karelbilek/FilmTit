package cz.filmtit.client;

import com.github.gwtbootstrap.client.ui.PasswordTextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.SubmitButton;
import com.github.gwtbootstrap.client.ui.TextBox;


public class LoginDialog extends Composite {

	private static LoginDialogUiBinder uiBinder = GWT
			.create(LoginDialogUiBinder.class);

	interface LoginDialogUiBinder extends UiBinder<Widget, LoginDialog> {
	}

	public LoginDialog() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiField
	TextBox txtUsername;

	@UiField
    PasswordTextBox txtPassword;

	@UiField
	Button btnLogin;
	
	@UiField
	Button btnLoginGoogle;
	
	@UiField
	Button btnRegister;
	
	@UiField
	Button btnForgottenPassword;
	
	@UiField
	Button btnCancel;
	
    public String getUsername() {
        return txtUsername.getText();
    }

    public String getPassword() {
        return txtPassword.getText();
    }

}

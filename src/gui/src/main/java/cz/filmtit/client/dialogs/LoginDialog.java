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

import cz.filmtit.client.Gui;
import cz.filmtit.share.AuthenticationServiceType;


public class LoginDialog extends Composite {

	private static LoginDialogUiBinder uiBinder = GWT
			.create(LoginDialogUiBinder.class);

	interface LoginDialogUiBinder extends UiBinder<Widget, LoginDialog> {
	}
	
	private Gui gui = Gui.getGui();

	public LoginDialog(String username) {
		initWidget(uiBinder.createAndBindUi(this));

        btnLogin.addClickHandler( new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            	String username = getUsername();
            	String password = getPassword();
            	if (username.isEmpty()) {
            		Window.alert("Please fill in the username!");
            	} else if (password.isEmpty()) {
            		Window.alert("Please fill in the password!");
				} else {
	                dialogBox.hide();
	                gui.log("trying to log in as user " + username);
	                gui.rpcHandler.simpleLogin(username, password);
				}
            }
        } );

        btnForgottenPassword.addClickHandler( new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                 String username = getUsername();
                 if (username.isEmpty()) {
                    Window.alert("You must fill in your username!");
                 } else {
                    gui.log("forgotten password - user " + username);
                    gui.rpcHandler.sendChangePasswordMail(username, dialogBox);
                 }
            }
        } );

        btnLoginGoogle.addClickHandler( new ClickHandler() {
               @Override
               public void onClick(ClickEvent event) {
            	   gui.log("trying to log in through Google account");
            	   gui.rpcHandler.getAuthenticationURL(AuthenticationServiceType.GOOGLE, dialogBox);
               }
          });

        btnRegister.addClickHandler( new ClickHandler() {
               @Override
               public void onClick(ClickEvent event) {
            	   gui.log("User decided to register, showing registration form");
                dialogBox.hide();
                gui.showRegistrationForm();
               }
          });

        btnCancel.addClickHandler( new ClickHandler() {
               @Override
               public void onClick(ClickEvent event) {
            	   gui.log("LoginDialog closed by user hitting Cancel button");
                dialogBox.hide();
               }
          });

        setUsername(username);

        dialogBox.show();
	}

    @UiField
    Modal dialogBox;

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

	public void setUsername(String username) {
		txtUsername.setText(username);
	}

    public String getPassword() {
        return txtPassword.getText();
    }

}

package cz.filmtit.client.dialogs;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Form;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.NavTabs;
import com.github.gwtbootstrap.client.ui.PasswordTextBox;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;

import cz.filmtit.share.AuthenticationServiceType;


public class LoginDialog extends Dialog {

	private static LoginDialogUiBinder uiBinder = GWT
			.create(LoginDialogUiBinder.class);

	interface LoginDialogUiBinder extends UiBinder<Widget, LoginDialog> {
	}
	
	//////////////////////////////////
	//                              //
	//        Constructors          //
	//                              //
	//////////////////////////////////
	
	/**
	 * Open the LoginDialog, showing the Login tab
	 * @param username
	 */
	public LoginDialog() {
		this(null, Tab.Login);
	}
	
	/**
	 * Open the LoginDialog, showing the Login tab and prefilling the given username into the form.
	 * @param username
	 */
	public LoginDialog(String username) {
		this(username, Tab.Login);
	}
	
	/**
	 * Open the LoginDialog, showing the given tab
	 * @param tab
	 */
	public LoginDialog(Tab tab) {
		this(null, tab);
	}
	
	/**
	 * Open the LoginDialog, showing the given tab and prefilling the given username into the form.
	 * @param username
	 * @param tab
	 */
	public LoginDialog(String username, Tab tab) {
		initWidget(uiBinder.createAndBindUi(this));

		propagateUsername(username);
		
        switchTo(tab);
        
        dialogBox.show();
	}
	
	//////////////////////////////////
	//                              //
	//        Tabs switching        //
	//                              //
	//////////////////////////////////
	
	/**
	 * Tabs available in the LoginDialog.
	 */
	public enum Tab  {
		Login,
		OpenidLogin,
		Register,
		ForgottenPassword;
	}
	
	@UiField
	NavTabs tabs;
	
	@UiField
	NavLink tabLogin;
	
	@UiHandler("tabLogin")
	void tabLoginClick(ClickEvent e) {
		switchTo(Tab.Login);
	}
	
	@UiField
	NavLink tabOpenidLogin;
	
	@UiHandler("tabOpenidLogin")
	void tabOpenidLoginClick(ClickEvent e) {
		switchTo(Tab.OpenidLogin);
	}
	
	@UiField
	NavLink tabRegister;
	
	@UiHandler("tabRegister")
	void tabRegisterClick(ClickEvent e) {
		switchTo(Tab.Register);
	}
	
	@UiField
	NavLink tabForgottenPassword;
	
	@UiHandler("tabForgottenPassword")
	void tabForgottenPasswordClick(ClickEvent e) {
		switchTo(Tab.ForgottenPassword);
	}
	
	private Tab activeTab = Tab.Login;
	
	private void switchTo(Tab tab) {
		propagateUsername(username(activeTab));
		activateTab(tab);
		showForm(tab);
		activeTab = tab;
	}
	
	private void activateTab(Tab tab) {
		tab(activeTab).setActive(false);
		tab(tab).setActive(true);
	}
	
	private void showForm(Tab tab) {
		form(activeTab).setVisible(false);
		form(tab).setVisible(true);
	}
	
	/**
	 * converts Tab to NavLink
	 * @param tab
	 * @return the NavLink representing the Tab
	 */
	private NavLink tab(Tab tab) {
		switch (tab) {
		case Login:
			return tabLogin;
		case OpenidLogin:
			return tabOpenidLogin;
		case Register:
			return tabRegister;
		case ForgottenPassword:
			return tabForgottenPassword;
		default:
			return tabLogin;
		}
	}

	/**
	 * converts Tab to Form
	 * @param tab
	 * @return the Form representing the Tab
	 */
	private Form form(Tab tab) {
		switch (tab) {
		case Login:
			return formLogin;
		case OpenidLogin:
			return formOpenidLogin;
		case Register:
			return formRegister;
		case ForgottenPassword:
			return formForgottenPassword;
		default:
			return formLogin;
		}
	}

	/**
	 * converts Tab to username
	 * @param tab
	 * @return the username filled in in the tab, or null if the tab has no username field
	 */
	private String username(Tab tab) {
		switch (tab) {
		case Login:
			return txtLoginUsername.getText();
		case OpenidLogin:
			return null;
		case Register:
			return txtRegUsername.getText();
		case ForgottenPassword:
			return txtFpwdUsername.getText();
		default:
			return null;
		}
	}

	/**
	 * Copies the username into all txt*Username fields
	 * (unless the username is empty or null)
	 * @param username
	 */
	private void propagateUsername(String username) {
		if (username != null && !username.isEmpty()) {
			txtLoginUsername.setText(username);
			txtRegUsername.setText(username);
			txtFpwdUsername.setText(username);
		}
	}
	
	//////////////////////////////////
	//                              //
	//        Login form            //
	//                              //
	//////////////////////////////////
	
	@UiField
	Form formLogin;
	
	@UiField
	TextBox txtLoginUsername;

	@UiField
    PasswordTextBox txtLoginPassword;

	@UiField
	Button btnLogin;
	
	@UiHandler("btnLogin")
	void btnLoginClick(ClickEvent e) {
    	deactivate();
    	String username = txtLoginUsername.getText();
    	String password = txtLoginPassword.getText();
    	
    	if (checkLoginForm(username, password)) {
            gui.log("trying to log in as user " + username);
            gui.rpcHandler.simpleLogin(username, password, LoginDialog.this);
    	}
	}
	
    private boolean checkLoginForm(String username, String password) {
    	boolean result = false;
    	
    	// TODO use some nice Bootstrap things instead of the alerts
    	if (!checkUsername(username)) {
    		reactivateWithErrorMessage("You must fill in your username!");
    	}
    	else if (!checkPasswordStrength(password)) {
    		reactivateWithErrorMessage("You must fill in your password!");
    	}
    	else {
    		result = true;
    	}
    	
    	return result;
    }
    
	//////////////////////////////////
	//                              //
	//        OpenID Login form     //
	//                              //
	//////////////////////////////////
	
	@UiField
	Form formOpenidLogin;
	
	@UiField
	Button btnLoginGoogle;
	
	@UiHandler("btnLoginGoogle")
	void btnLoginGoogleClick(ClickEvent e) {
    	deactivate();
    	
		gui.log("trying to log in through Google account");
		gui.rpcHandler.getAuthenticationURL(AuthenticationServiceType.GOOGLE, LoginDialog.this);
	}
	
	//////////////////////////////////
	//                              //
	//        Registration form     //
	//                              //
	//////////////////////////////////
	
	@UiField
	Form formRegister;
	
	@UiField
	TextBox txtRegEmail;

	@UiField
	TextBox txtRegUsername;

	@UiField
    PasswordTextBox txtRegPassword;

	@UiField
    PasswordTextBox txtRegPasswordRepeat;

	@UiField
	Button btnRegister;
	
	@UiHandler("btnRegister")
	void btnRegisterClick(ClickEvent e) {
    	deactivate();
    	String username = txtRegUsername.getText();
    	String password = txtRegPassword.getText();
    	String passwordRepeat = txtRegPasswordRepeat.getText();
    	String email = txtRegEmail.getText();
    	
    	if (checkRegForm(username, password, passwordRepeat, email)) {
            gui.log("trying to register as user " + username);
           	gui.rpcHandler.registerUser(username, password, email, LoginDialog.this);
    	}
	}
	
    private boolean checkRegForm(String username, String password, String passwordRepeat, String email) {
    	boolean result = false;
    	
    	// TODO use some nice Bootstrap things instead of the alerts
    	if (!checkUsername(username)) {
    		reactivateWithErrorMessage("You cannot use this username!");
    	}
    	else if (!checkPasswordStrength(password)) {
    		reactivateWithErrorMessage("This password is too weak!");
    	}
    	else if (!checkPasswordsMatch(password, passwordRepeat)) {
    		reactivateWithErrorMessage("The passwords don't match!");
    	}
    	else if (!checkEmailValidity(email)) {
    		reactivateWithErrorMessage("The e-mail address is invalid!");
    	}
    	else {
    		result = true;
    	}
    	
    	return result;
    }
    
	//////////////////////////////////
	//                              //
	//   Forgotten password form    //
	//                              //
	//////////////////////////////////
	
	@UiField
	Form formForgottenPassword;

	@UiField
	TextBox txtFpwdUsername;

	// TODO: also enable forgotten username retrieval with email address
	// @UiField
	// TextBox txtFpwdEmail;

	@UiField
	Button btnForgottenPassword;
	
	@UiHandler("btnForgottenPassword")
	void btnForgottenPasswordClick(ClickEvent e) {
    	deactivate();
    	String username = txtFpwdUsername.getText();
    	// String email = txtFpwdEmail.getText();
    	
    	if (checkFpwdForm(username)) {
			gui.log("trying to send forgotten password email to user " + username);
			gui.rpcHandler.sendChangePasswordMail(username, LoginDialog.this);
    	}
	}
	
    private boolean checkFpwdForm(String username /*, String email*/) {
    	boolean result = false;
    	
    	// TODO use some nice Bootstrap things instead of the alerts
    	if (!checkUsername(username)) {
    		reactivateWithErrorMessage("You must fill in your username!");
    	}
//    	else if (!checkEmailValidity(email)) {
//    		reactivateWithErrorMessage("The e-mail address is invalid!");
//    	}
    	else {
    		result = true;
    	}
    	
    	return result;
    }
    
	//////////////////////////////////
	//                              //
	//   Shared methods             //
	//                              //
	//////////////////////////////////
	
    // TODO: these methods should be elsewhere and should be made better
    
    public static boolean checkUsername(String username) {
        // TODO
    	return (!username.isEmpty());
    }
    
    public static boolean checkEmailValidity(String email) {
        // TODO
    	if (email.length() >= 5 && email.contains("@") && email.contains(".")) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public static boolean checkPasswordsMatch(String password1, String password2) {
    	if (password1.equals(password2)) {
    		return true;
    	} else {
    		return false;
    	}
	}
    
    public static boolean checkPasswordStrength(String password) {
        // TODO (but maybe keep that way)
        if (password.length() >= 3) {
        	return true;
        } else {
        	return false;
        }
    }
    
}

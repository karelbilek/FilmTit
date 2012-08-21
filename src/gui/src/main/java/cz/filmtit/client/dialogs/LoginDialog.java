package cz.filmtit.client.dialogs;

import com.github.gwtbootstrap.client.ui.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;

import cz.filmtit.client.Gui;
import cz.filmtit.client.callables.GetAuthenticationURL;
import cz.filmtit.client.callables.RegisterUser;
import cz.filmtit.client.callables.SendChangePasswordMail;
import cz.filmtit.client.callables.SendChangePasswordMailByMail;
import cz.filmtit.client.callables.SimpleLogin;
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
	 */
	public LoginDialog() {
		this(null, Tab.Login, null);
	}
	
	/**
	 * Open the LoginDialog, showing the Login tab and prefilling the given username into the form.
	 * @param username
	 */
	public LoginDialog(String username) {
		this(username, Tab.Login, null);
	}
	
	/**
	 * Open the LoginDialog, showing the given tab
	 * @param tab
	 */
	public LoginDialog(Tab tab) {
		this(null, tab, null);
	}
	
	/**
	 * Open the LoginDialog, showing the given tab and error message.
	 * @param tab
	 */
	public LoginDialog(Tab tab, String errorMessage) {
		this(null, tab, errorMessage);
	}
	
	/**
	 * Open the LoginDialog, showing the given tab and prefilling the given username into the form.
	 * @param username
	 */
	public LoginDialog(String username, Tab tab) {
		this(username, tab, null);
	}
	
	/**
	 * Open the LoginDialog, showing the Login tab and error message and prefilling the given username into the form.
	 * @param username
	 */
	public LoginDialog(String username, String errorMessage) {
		this(username, Tab.Login, errorMessage);
	}
	
	/**
	 * Open the LoginDialog, showing the given tab and prefilling the given username into the form.
	 * @param username The username to prefill.
	 * @param tab The tab to show.
	 * @param errorMessage An optional error message to show to the user.
	 */
	public LoginDialog(String username, Tab tab, String errorMessage) {
		super();
		initWidget(uiBinder.createAndBindUi(this));

		propagateUsername(username);
		
        switchTo(tab);
        
        if (errorMessage != null && !errorMessage.isEmpty()) {
        	showErrorMessage(errorMessage);
        }
        
        enablableElements = new HasEnabled[] {
        		txtLoginPassword, txtLoginUsername, btnLogin,
        		btnLoginGoogle, btnLoginSeznam, btnLoginYahoo,
        		txtRegEmail, txtRegPassword, txtRegPasswordRepeat, txtRegUsername, btnRegister,
        		txtFpwdEmail, txtFpwdUsername, btnForgottenPassword,
    		};
        focusElement = focusElement(tab);
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
		if (isActivated()) {
			hideAlert();
			propagateUsername(username(activeTab));
			activateTab(tab);
			showForm(tab);
			activeTab = tab;
			focusElement = focusElement(tab);
			focusFocusElement();
		}
		// do not switch tabs in deactivated mode
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

	/**
	 * converts Tab to Focusable element to be focused on the tab
	 * @param tab
	 * @return the Focusable element in the tab which should get focus on opening the tab, or null if the tab has no such Focusable element
	 */
	private Focusable focusElement(Tab tab) {
		switch (tab) {
		case Login:
			return txtLoginUsername;
		case OpenidLogin:
			return null;
		case Register:
			return txtRegUsername;
		case ForgottenPassword:
			return txtFpwdUsername;
		default:
			return null;
		}
	}

	//////////////////////////////////
	//                              //
	//        Error alerts          //
	//                              //
	//////////////////////////////////
	
	@Override
	public void showErrorMessage(String message) {
		alert(activeTab).setText(message);
		alert(activeTab).setVisible(true);
	}
	
	/**
	 * Hides the active alert if there is one.
	 */
	private void hideAlert() {
		alert(activeTab).setVisible(false);
	}
	
	/**
	 * converts Tab to alert
	 * @param tab
	 * @return the Alert for the tab
	 */
	private Alert alert(Tab tab) {
		switch (tab) {
		case Login:
			return alertLogin;
		case OpenidLogin:
			return alertOpenidLogin;
		case Register:
			return alertRegister;
		case ForgottenPassword:
			return alertForgottenPassword;
		default:
			return alertLogin;
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
	Alert alertLogin;
	
	@UiField
	TextBox txtLoginUsername;

	@UiField
    PasswordTextBox txtLoginPassword;

	@UiField
    SubmitButton btnLogin;

    @UiHandler("formLogin")
    void formLoginSubmit(Form.SubmitEvent e) {
/*
    }
	
	@UiHandler("btnLogin")
	void btnLoginClick(ClickEvent e) {
*/
        deactivate();
    	String username = txtLoginUsername.getText();
    	String password = txtLoginPassword.getText();
    	
    	if (checkLoginForm(username, password)) {
            Gui.log("trying to log in as user " + username);
            new SimpleLogin(username, password, LoginDialog.this);
    	}
	}
	
    private boolean checkLoginForm(String username, String password) {
    	boolean result = false;
    	
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
	Alert alertOpenidLogin;
	
	@UiField
	Button btnLoginGoogle;

    @UiHandler("btnLoginGoogle")
    void loginGoogle(ClickEvent e) {
        deactivate();
		new GetAuthenticationURL(AuthenticationServiceType.GOOGLE, LoginDialog.this);
	}
	
	@UiField
	Button btnLoginSeznam;

    @UiHandler("btnLoginSeznam")
    void loginSeznam(ClickEvent e) {
        deactivate();
		new GetAuthenticationURL(AuthenticationServiceType.SEZNAM, LoginDialog.this);
	}
	
	@UiField
	Button btnLoginYahoo;

    @UiHandler("btnLoginYahoo")
    void loginYahoo(ClickEvent e) {
        deactivate();
		new GetAuthenticationURL(AuthenticationServiceType.YAHOO, LoginDialog.this);
	}
	
	//////////////////////////////////
	//                              //
	//        Registration form     //
	//                              //
	//////////////////////////////////
	
	@UiField
	Form formRegister;
	
	@UiField
	Alert alertRegister;
	
	@UiField
	TextBox txtRegEmail;

	@UiField
	TextBox txtRegUsername;

	@UiField
    PasswordTextBox txtRegPassword;

	@UiField
    PasswordTextBox txtRegPasswordRepeat;

	@UiField
	SubmitButton btnRegister;

    @UiHandler("formRegister")
    void formRegisterSubmit(Form.SubmitEvent e) {
        deactivate();
    	String username = txtRegUsername.getText();
    	String password = txtRegPassword.getText();
    	String passwordRepeat = txtRegPasswordRepeat.getText();
    	String email = txtRegEmail.getText();
    	
    	if (checkRegForm(username, password, passwordRepeat, email)) {
            Gui.log("trying to register as user " + username);
           	new RegisterUser(username, password, email, LoginDialog.this);
    	}
	}
	
    private boolean checkRegForm(String username, String password, String passwordRepeat, String email) {
    	boolean result = false;
    	
    	if (!checkUsername(username)) {
    		reactivateWithErrorMessage("You cannot use this username!");
    	}
    	else if (!checkPasswordStrength(password)) {
    		reactivateWithErrorMessage("This password is too weak!");
    	}
    	else if (!checkPasswordsMatch(password, passwordRepeat)) {
    		reactivateWithErrorMessage("The passwords don't match!");
    	}
    	else if (!email.isEmpty() && !checkEmailValidity(email)) {
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
	Alert alertForgottenPassword;
	
	@UiField
	TextBox txtFpwdUsername;

	@UiField
	TextBox txtFpwdEmail;

	@UiField
	SubmitButton btnForgottenPassword;

    @UiHandler("formForgottenPassword")
    void formForgottenPasswordSubmit(Form.SubmitEvent e) {
        deactivate();
    	String username = txtFpwdUsername.getText();
    	String email = txtFpwdEmail.getText();
    	
    	if (checkEmailValidity(email)) {
        	if (checkUsername(username)) {
    			Gui.log("trying to send forgotten password email to user " + username);
    			new SendChangePasswordMail(username, email, LoginDialog.this);
        	}
        	else {
    			Gui.log("trying to send forgotten password email to " + email);
    			new SendChangePasswordMailByMail(email, null, LoginDialog.this);
        	}
    	}
    	else if (checkUsername(username)) {
			Gui.log("trying to send forgotten password email to user " + username);
			new SendChangePasswordMail(username, null, LoginDialog.this);
    	}
    	else {
    		reactivateWithErrorMessage("You must fill at least one of the fields!");
    	}
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

package cz.filmtit.client.pages;

import java.util.LinkedList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.AlertBlock;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.Form;
import com.github.gwtbootstrap.client.ui.IntegerBox;
import com.github.gwtbootstrap.client.ui.PageHeader;
import com.github.gwtbootstrap.client.ui.PasswordTextBox;
import com.github.gwtbootstrap.client.ui.SubmitButton;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import cz.filmtit.client.Gui;
import cz.filmtit.client.ReceivesSettings;
import cz.filmtit.client.callables.*;
import cz.filmtit.client.dialogs.LoginDialog;
import cz.filmtit.share.User;

/**
 *  Enables the user to change several settings,
 *  such as his password or the maximum number of translation suggestions to show
 */
public class Settings extends Composite implements ReceivesSettings {

	private static SettingsUiBinder uiBinder = GWT
			.create(SettingsUiBinder.class);

	interface SettingsUiBinder extends UiBinder<Widget, Settings> {
	}

	private User user;
	
	public Settings() {
		initWidget(uiBinder.createAndBindUi(this));
		
		Gui.getGuiStructure().contentPanel.setStyleName("settings");
        Gui.getGuiStructure().contentPanel.setWidget(this);
        
        // load settings
		deactivate();
		new LoadSettings(this);
	}
	
	@Override
	public void onSettingsReceived(User user) {
		this.user = user;
        header.setSubtext(user.getName());
        setDefault();
        reactivate();
	}
	
	private void setDefault () {
		setUsername.setText(user.getName());
		setPassword.setText("");
		setPasswordRepeat.setText("");
		setEmail.setText(user.getEmail());
		setPermalogin.setValue(user.isPermanentlyLoggedIn());
		setMaxSuggestions.setValue(user.getMaximumNumberOfSuggestions());
		setUseMT.setValue(user.getUseMoses());
	}
	
	private int waitingFor = 0;
	
	private void decrementWaitingFor() {
		waitingFor--;
		if (waitingFor == 0) {
			allReturned();
		}
	}
	
	private int success;
	
	private int error;
	
	private StringBuilder errors;
	
	private void save() {
		
		// init
		alertInfo.setVisible(false);
		alertError.setVisible(false);
		success = 0;
		error = 0;
		errors = new StringBuilder();
		List<SetSetting<?>> calls = new LinkedList<SetSetting<?>>();
		
		// username
		String newUsername = setUsername.getValue();
		if (!newUsername.equals(user.getName())) {
			if (LoginDialog.checkUsername(newUsername)) {
				calls.add(
						new SetUsername(newUsername, Settings.this)
					);
			}
			else {
				error++;
				errors.append("You cannot use this username!");
				errors.append(' ');
			}
		}
		
		// passwort
		String newPassword = setPassword.getValue();
		String newPasswordRepeat = setPasswordRepeat.getValue();
		if (newPassword != null && !newPassword.isEmpty()) {
			if (LoginDialog.checkPasswordsMatch(newPassword, newPasswordRepeat)) {
				if (LoginDialog.checkPasswordStrength(newPassword)) {
					calls.add(
							new SetPassword(newPassword, Settings.this)
						);
				}
				else {
					error++;
					errors.append("This password is too weak!");
					errors.append(' ');
				}
			}
			else {
				error++;
				errors.append("The repeated password must match the new password!");
				errors.append(' ');
			}
		}
		
		// email
		String newEmail = setEmail.getValue();
		if (!newEmail.equals(user.getEmail())) {
			if (newEmail.isEmpty() || LoginDialog.checkEmailValidity(newEmail)) {
				calls.add(
					new SetEmail(newEmail, Settings.this)
				);
			}
			else {
				error++;
				errors.append("The e-mail address is invalid!");
				errors.append(' ');
			}
		}
		
		// permalog
		if (user.isPermanentlyLoggedIn() != setPermalogin.getValue()) {
			calls.add(
				new SetPermanentlyLoggedIn(setPermalogin.getValue(), Settings.this)
			);
		}
		
		// max suggestions
		if (setMaxSuggestions.getValue() == null) {
			setMaxSuggestions.setValue(user.getMaximumNumberOfSuggestions());
		}
		else if (user.getMaximumNumberOfSuggestions() != setMaxSuggestions.getValue()) {
			calls.add(
				new SetMaximumNumberOfSuggestions(setMaxSuggestions.getValue(), Settings.this)
			);
		}
		
		// moses
		if (user.getUseMoses() != setUseMT.getValue()) {
			calls.add(
				new SetUseMT(setUseMT.getValue(), Settings.this)
			);
		}
		
		if (error > 0) {
			alertError.setText(errors.toString());
			alertError.setVisible(true);
		}
		else {
			// invoke the calls
			if (!calls.isEmpty()) {
				if (error > 0) {
					alertError.setText(errors.toString());
					alertError.setVisible(true);
				}
				deactivate();
				waitingFor = calls.size();
				for (SetSetting<?> setSetting : calls) {
					setSetting.enqueue();
				}
			}
			else {
				alertInfo.setText("Nothing to be saved!");
				alertInfo.setVisible(true);
			}
		}
	}
	
	private void deactivate() {
		setUsername.setEnabled(false);
		setPassword.setEnabled(false);
		setPasswordRepeat.setEnabled(false);
		setEmail.setEnabled(false);
		setPermalogin.setEnabled(false);
		setMaxSuggestions.setEnabled(false);
		setUseMT.setEnabled(false);
		btnSave.setEnabled(false);
		btnReset.setEnabled(false);
	}
	
	@Override
	public void settingSuccess() {
		success++;
		decrementWaitingFor();
	}
	
	@Override
	public void settingError(String message) {
		errors.append(message);
		errors.append(' ');
		error++;
		decrementWaitingFor();
	}
	
	private void allReturned() {
		if (error == 0) {
			// reactivate
			reactivate();
			// say OK
			alertInfo.setText("Settings successfully saved!");
			alertInfo.setVisible(true);
			// reload currently valid settings (good especially for resetting User in Gui)
			new LoadSettings(this);
		}
		else {
			// keep deactivated, activate when currently valid settings have been loaded
			// deactivate();
			// say error
			alertError.setText(errors.toString());
			alertError.setVisible(true);
			// reload currently valid settings (to see what they actually are after these errors)
			new LoadSettings(this);
		}
	}
	
	private void reactivate() {
		Window.scrollTo(Window.getScrollLeft(), 0);
		setUsername.setEnabled(true);
		setPassword.setEnabled(true);
		setPasswordRepeat.setEnabled(true);
		setEmail.setEnabled(true);
		setPermalogin.setEnabled(true);
		setMaxSuggestions.setEnabled(true);
		setUseMT.setEnabled(true);
		btnSave.setEnabled(true);
		btnReset.setEnabled(true);
	}
	
	@UiField
	PageHeader header;
	
	@UiField
	AlertBlock alertInfo;
	
	@UiField
	AlertBlock alertError;
	
	@UiField
	TextBox setUsername;
	
	@UiField
	PasswordTextBox setPassword;
	
	@UiField
	PasswordTextBox setPasswordRepeat;
	
	@UiField
	TextBox setEmail;
	
	@UiField
	CheckBox setPermalogin;
	
	@UiField
	IntegerBox setMaxSuggestions;
	
	@UiField
	CheckBox setUseMT;
	
	@UiField
	SubmitButton btnSave;
	
	@UiField
	Form settingsForm;
	@UiHandler("settingsForm")
	void submit(Form.SubmitEvent e) {
		save();
	}
	
	@UiField
	Button btnReset;
	@UiHandler("btnReset")
	void reset(ClickEvent e) {
		setDefault();
	}

}

package cz.filmtit.client.pages;

import com.github.gwtbootstrap.client.ui.AlertBlock;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.Form;
import com.github.gwtbootstrap.client.ui.IntegerBox;
import com.github.gwtbootstrap.client.ui.PageHeader;
import com.github.gwtbootstrap.client.ui.SubmitButton;
import com.github.gwtbootstrap.client.ui.TextBox;
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

import cz.filmtit.client.Gui;
import cz.filmtit.client.ReceivesSettings;
import cz.filmtit.client.callables.*;
import cz.filmtit.share.User;

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
		setEmail.setText(user.getEmail());
		setPermalogin.setValue(user.isPermanentlyLoggedIn());
		setMaxSuggestions.setValue(user.getMaximumNumberOfSuggestions());
		setMoses.setValue(user.getUseMoses());
	}
	
	private int changedValues = 0;
	
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
		
		// save
		if (!user.getEmail().equals(setEmail.getValue())) {
			changedValues++;
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				@Override
				public void execute() {
					new SetEmail(setEmail.getValue(), Settings.this);
				}
			});
		}
		if (user.isPermanentlyLoggedIn() != setPermalogin.getValue()) {
			changedValues++;
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				@Override
				public void execute() {
					new SetPermanentlyLoggedIn(setPermalogin.getValue(), Settings.this);
				}
			});
		}
		if (setMaxSuggestions.getValue() != null && user.getMaximumNumberOfSuggestions() != setMaxSuggestions.getValue()) {
			changedValues++;
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				@Override
				public void execute() {
					new SetMaximumNumberOfSuggestions(setMaxSuggestions.getValue(), Settings.this);
				}
			});
		}
		if (user.getUseMoses() != setMoses.getValue()) {
			changedValues++;
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				@Override
				public void execute() {
					new SetUseMoses(setMoses.getValue(), Settings.this);
				}
			});
		}
		
		if (changedValues > 0) {
			waitingFor = changedValues;
			deactivate();
		}
		else {
			alertInfo.setText("Nothing to be saved!");
			alertInfo.setVisible(true);
		}
	}
	
	private void deactivate() {
		setEmail.setEnabled(false);
		setPermalogin.setEnabled(false);
		setMaxSuggestions.setEnabled(false);
		setMoses.setEnabled(false);
		btnSave.setEnabled(false);
		btnReset.setEnabled(false);
	}
	
	public void success() {
		success++;
		decrementWaitingFor();
	}
	
	public void error(String message) {
		errors.append(message);
		errors.append(' ');
		error++;
		decrementWaitingFor();
	}
	
	private void allReturned() {
		// TODO: request fresh User
		reactivate();
		if (error == 0) {
			alertInfo.setText("Settings successfully saved!");
			alertInfo.setVisible(true);
		}
		else {
			alertError.setText(errors.toString());
			alertError.setVisible(true);
		}
	}
	
	private void reactivate() {
		Window.scrollTo(Window.getScrollLeft(), 0);
		setEmail.setEnabled(true);
		setPermalogin.setEnabled(true);
		setMaxSuggestions.setEnabled(true);
		setMoses.setEnabled(true);
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
	TextBox setEmail;
	
	@UiField
	CheckBox setPermalogin;
	
	@UiField
	IntegerBox setMaxSuggestions;
	
	@UiField
	CheckBox setMoses;
	
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

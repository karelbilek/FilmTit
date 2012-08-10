package cz.filmtit.client.dialogs;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.AlertBlock;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Form;
import com.github.gwtbootstrap.client.ui.Paragraph;
import com.github.gwtbootstrap.client.ui.SubmitButton;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;

import cz.filmtit.client.LocalStorageHandler;

public class GoingOnlineDialog extends Dialog {

	private static GoingOnlineDialogUiBinder uiBinder = GWT
			.create(GoingOnlineDialogUiBinder.class);

	interface GoingOnlineDialogUiBinder extends
			UiBinder<Widget, GoingOnlineDialog> {
	}

	public GoingOnlineDialog(int count) {
		initWidget(uiBinder.createAndBindUi(this));

		paragraph.setText(
				"There are " + count + " Offline Mode items stored in your browser. " +
				"Let's upload them to the server now!"
			);
		
		dialogBox.show();
	}
	
	private enum State { Uploading, Retrying, Deleting };
	
	private State state;
	
	@UiField
	Form goingOnlineForm;
    @UiHandler("goingOnlineForm")
    void goingOnlineFormSubmit(Form.SubmitEvent e) {
    	state = State.Uploading;
    	deactivate();
    	LocalStorageHandler.uploadUserObjects();
	}
	
	@UiField
	Paragraph paragraph;
	
	@UiField
	AlertBlock info;
	
	@UiField
	AlertBlock error;
	
	@UiField
	SubmitButton uploadButton;
	
	@UiField
	Button retryButton;
	@UiHandler("retryButton")
	void retryButtonClick (ClickEvent e) {
		state = State.Retrying;
		deactivate();
		LocalStorageHandler.retryUploadUserObjects();
	}
	
	@UiField
	Button deleteButton;
	@UiHandler("deleteButton")
	void deleteButtonClick (ClickEvent e) {
		state = State.Deleting;
		deactivate();
		LocalStorageHandler.deleteFailedObjects();
	}
	
	@UiField
	Button closeButton;
	@UiHandler("closeButton")
	void closeButtonClick (ClickEvent e) {
		close();
	}
	
	@Override
	public void showInfoMessage(String message) {
		// hide irrelevant texts
		paragraph.setVisible(false);
		error.setVisible(false);
		// info
		info.setVisible(true);
		switch (state) {
		case Uploading:
			info.setHeading("Upload successful");
			info.setText(message);
			break;
		case Retrying:
			info.setHeading("Retry successful");
			info.setText(message);
			break;
		case Deleting:
			info.setHeading("Delete successful");
			info.setText(message);
			break;
		default:
			assert false;
			break;
		}
		// toggle buttons
		uploadButton.setVisible(false);
		retryButton.setVisible(false);
		deleteButton.setVisible(false);
		closeButton.setType(ButtonType.SUCCESS);
		closeButton.setVisible(true);
	}
	
	@Override
	public void showErrorMessage(String message) {
		// hide irrelevant texts
		paragraph.setVisible(false);
		info.setVisible(false);
		// error
		error.setVisible(true);
		switch (state) {
		case Uploading:
			error.setHeading("Upload error");
			error.setText(message);
			break;
		case Retrying:
			error.setHeading("Retry error");
			error.setText(message);
			break;
		case Deleting:
			error.setHeading("Delete error");
			error.setText(message);
			break;
		default:
			assert false;
			break;
		}
		// toggle buttons
		uploadButton.setVisible(false);
		retryButton.setVisible(true);
		deleteButton.setVisible(true);
		closeButton.setVisible(true);
	}
	
	
}

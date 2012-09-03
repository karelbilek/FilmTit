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

package cz.filmtit.client.dialogs;

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
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;

import cz.filmtit.client.LocalStorageHandler;

/**
 * Informs the user that there are stored items from Offline Mode
 * and offers to upload them.
 * Then informs the user about the result,
 * offering to retry the upload or to delete the data in case of error.
 * @author rur
 *
 */
public class GoingOnlineDialog extends Dialog {

	private static GoingOnlineDialogUiBinder uiBinder = GWT
			.create(GoingOnlineDialogUiBinder.class);

	interface GoingOnlineDialogUiBinder extends
			UiBinder<Widget, GoingOnlineDialog> {
	}

	/**
	 * Shows the dialog.
	 * @param count number of items found in Local Storage
	 */
	public GoingOnlineDialog(int count) {
		super();
		initWidget(uiBinder.createAndBindUi(this));

		paragraph.setText(
				"There are " + count + " Offline Mode items stored in your browser. " +
				"Let's upload them to the server now!"
			);
		
		enablableElements = new HasEnabled[] {uploadButton, retryButton, deleteButton, closeButton};
		focusElement = uploadButton;
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
		focusElement = closeButton;
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
		focusElement = retryButton;
	}
	
}

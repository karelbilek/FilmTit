package cz.filmtit.client.dialogs;

import com.github.gwtbootstrap.client.ui.Form;
import com.github.gwtbootstrap.client.ui.SubmitButton;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;

import cz.filmtit.client.LocalStorageHandler;

public class GoingOfflineDialog extends Dialog {

	private static GoingOfflineDialogUiBinder uiBinder = GWT
			.create(GoingOfflineDialogUiBinder.class);

	interface GoingOfflineDialogUiBinder extends
			UiBinder<Widget, GoingOfflineDialog> {
	}

	public GoingOfflineDialog() {
		super();
		initWidget(uiBinder.createAndBindUi(this));
		
		enablableElements = new HasEnabled[] { offlineButton };
		focusElement = offlineButton;
	}
	
	@UiField
	SubmitButton offlineButton;

	@UiField
	Form goingOfflineForm;
	
    @UiHandler("goingOfflineForm")
    void goingOfflineFormSubmit(Form.SubmitEvent e) {
    	close();
    	LocalStorageHandler.setOnline(false);
	}
    
    @Override
    protected void onHide() {
    	LocalStorageHandler.cancelledOfflineStorageOffer();
    }
	
}

package cz.filmtit.client.dialogs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.github.gwtbootstrap.client.ui.Button;

import cz.filmtit.client.Dialog;

public class SessionIDPollingDialog extends Composite implements Dialog {

	private static SessionIDPollingDialogUiBinder uiBinder = GWT
			.create(SessionIDPollingDialogUiBinder.class);

	interface SessionIDPollingDialogUiBinder extends
			UiBinder<Widget, SessionIDPollingDialog> {
	}

	public SessionIDPollingDialog() {
		initWidget(uiBinder.createAndBindUi(this));
		
		// TODO: start polling with getSessionID()

	}
	
    @UiField
    Button btnCancel;

    public void addCancelClickHandler(com.google.gwt.event.dom.client.ClickHandler h) {
        btnCancel.addClickHandler(h);
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

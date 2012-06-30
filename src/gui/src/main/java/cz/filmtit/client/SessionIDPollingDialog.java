package cz.filmtit.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.github.gwtbootstrap.client.ui.Button;

public class SessionIDPollingDialog extends Composite {

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
	
}

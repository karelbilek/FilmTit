package cz.filmtit.client.dialogs;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;

import cz.filmtit.client.callables.SessionIDPolling;


public class SessionIDPollingDialog extends Dialog {

	private static SessionIDPollingDialogUiBinder uiBinder = GWT
			.create(SessionIDPollingDialogUiBinder.class);

	interface SessionIDPollingDialogUiBinder extends
			UiBinder<Widget, SessionIDPollingDialog> {
	}

	SessionIDPolling sessionIDPolling;
	
	public SessionIDPollingDialog(SessionIDPolling sessionIDPolling) {
		initWidget(uiBinder.createAndBindUi(this));
		
		this.sessionIDPolling = sessionIDPolling;
		
		dialogBox.show();
	}
	
    @UiField
    Button btnCancel;

    @UiHandler("btnCancel")
    void btnCancelClick(ClickEvent e) {
    	sessionIDPolling.stopSessionIDPolling();
        close();
        gui.log("SessionIDPollingDialog closed by user hitting Cancel button");
    }
        
    // TODO: call() if gets focus

}

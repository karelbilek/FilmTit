package cz.filmtit.client.dialogs;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;

import cz.filmtit.client.Gui;
import cz.filmtit.client.callables.SessionIDPolling;

/**
 * Shown when waiting for OpenID login process to complete in GUI.
 * @author rur
 *
 */
public class SessionIDPollingDialog extends Dialog {

	private static SessionIDPollingDialogUiBinder uiBinder = GWT
			.create(SessionIDPollingDialogUiBinder.class);

	interface SessionIDPollingDialogUiBinder extends
			UiBinder<Widget, SessionIDPollingDialog> {
	}

	private SessionIDPolling sessionIDPolling;
	
	/**
	 * Shows the dialog.
	 * @param sessionIDPolling The callable polling the User Space for session ID.
	 * Passed to be able to cancel the polling from within the dialog by clicking the Cancel button.
	 */
	public SessionIDPollingDialog(SessionIDPolling sessionIDPolling) {
		super();
		initWidget(uiBinder.createAndBindUi(this));
		
		this.sessionIDPolling = sessionIDPolling;
	}
	
    @UiField
    Button btnCancel;

    @UiHandler("btnCancel")
    void btnCancelClick(ClickEvent e) {
    	sessionIDPolling.stopSessionIDPolling();
        close();
        Gui.log("SessionIDPollingDialog closed by user hitting Cancel button");
    }
        
    // TODO: call() if gets focus

}

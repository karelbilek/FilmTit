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

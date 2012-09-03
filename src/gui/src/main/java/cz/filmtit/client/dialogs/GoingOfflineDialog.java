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

import com.github.gwtbootstrap.client.ui.Form;
import com.github.gwtbootstrap.client.ui.SubmitButton;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;

import cz.filmtit.client.LocalStorageHandler;

/**
 * Informs the user that there is no connection to the server
 * and offers them to turn on the Offline Mode.
 * @author rur
 *
 */
public class GoingOfflineDialog extends Dialog {

	private static GoingOfflineDialogUiBinder uiBinder = GWT
			.create(GoingOfflineDialogUiBinder.class);

	interface GoingOfflineDialogUiBinder extends
			UiBinder<Widget, GoingOfflineDialog> {
	}

	/**
	 * Shows the dialog.
	 */
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

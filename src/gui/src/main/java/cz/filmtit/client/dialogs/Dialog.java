package cz.filmtit.client.dialogs;

import com.github.gwtbootstrap.client.ui.Modal;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;

import cz.filmtit.client.Gui;

/**
 * Common ancestor to dialogs in FilmTit.
 * Each dialog should be extended from this class.
 * All dialogs should be accessed only through this class methods.
 * Must be placed in the dialogs subpackage because of visibility of dialogBox to GuiBinder.
 * @author rur
 *
 */
public abstract class Dialog extends Composite {
	
	/**
	 * The enclosing dialog box.
	 */
    @UiField
	Modal dialogBox;
    
	/**
	 * Reference to the gui.
	 */
	protected Gui gui = Gui.getGui();
	
	// The methods have the simplest possible implementations and should be overridden whenever applicable.
	
	/**
	 * Temporarily prevent the user from using the dialog,
	 * but do not hide it.
	 * Used to block the user from doing anything
	 * e.g. while waiting for an RPC call to complete.
	 */
	public void deactivate() {
		dialogBox.setVisible(false);
	}
	
	/**
	 * Activate the dialog again
	 * @param message
	 */
	public void reactivate() {
		dialogBox.setVisible(true);
	}
	
	/**
	 * Activate the dialog again, showing an error message to the user.
	 * @param message
	 */
	public void reactivateWithErrorMessage(String message) {
		reactivate();
		Window.alert(message);		
	}
	
	/**
	 * Hide the dialog.
	 */
	public void close() {
		dialogBox.hide();
	}
	
}

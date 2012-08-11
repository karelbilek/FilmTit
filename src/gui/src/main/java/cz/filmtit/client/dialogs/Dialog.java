package cz.filmtit.client.dialogs;

import com.github.gwtbootstrap.client.ui.event.HiddenEvent;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
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
    CustomModal dialogBox;
    
    @UiHandler("dialogBox")
    final void modalClosed(HiddenEvent e) {
    	if (!closedByClose) {
    		onHide();
    	}    	
    }

    /**
     * set to true right before the close() method closes the dialog
     */
    boolean closedByClose = false;
    
    /**
     * Called when the dialog is closed directly by dialog.hide(), i.e. NOT with the close() method.
     * Intended to handle the user clicking on the close cross or pressing Esc.
     * @see Dialog.onClosing()
     */
    protected void onHide() {
    	// nothing to do by default
    }
    
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
	 * Show an error message to the user.
	 * @param message
	 */
	public void showErrorMessage(String message) {
		Window.alert(message);		
	}

	/**
	 * Show an info message to the user.
	 * @param message
	 */
	public void showInfoMessage(String message) {
		Window.alert(message);		
	}

	/**
	 * Activate the dialog again, showing an error message to the user.
	 * @param message
	 */
	final public void reactivateWithErrorMessage(String message) {
		reactivate();
		showErrorMessage(message);		
	}
	
	/**
	 * Activate the dialog again, showing an info message to the user.
	 * @param message
	 */
	final public void reactivateWithInfoMessage(String message) {
		reactivate();
		showInfoMessage(message);		
	}
	
	/**
	 * Hide the dialog, if not prevented by onClosing().
	 */
	final public void close() {
		if (onClosing()) {
			closedByClose = true;
			dialogBox.hide();
		}		
	}
	
    /**
     * Called when the dialog is closed with the close() method, i.e. NOT when the user presses Esc etc.
     * @see Dialog.onHide()
     * @return true if the closing should continue, false to stop it
     */
    protected boolean onClosing() {
    	// nothing to do by default
    	return true;
    }
	
	
}

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

import com.github.gwtbootstrap.client.ui.event.HiddenEvent;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasEnabled;

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
	 * Should not be accessed in subclasses
	 * (but must be visible to UiBinder).
	 */
    @UiField
    CustomModal dialogBox;
    
    /**
     * Calls {@link onHide} if the modal is closed by user.
     */
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
    
    private boolean activated = true;
    
    /**
     * true by default,
     * false after calling deactivate(),
     * true after calling reactivate().
     */
    public boolean isActivated() {
		return activated;
	}

	/**
     * Elements enabled and disabled on calling reactivate() and deactivate() respectively.
     */
    protected HasEnabled[] enablableElements;
    
    /**
     * The element to be focused on opening the dialog.
     */
    protected Focusable focusElement;
    
    /**
     * Shows the dialog and focuses focusElement
     */
    public Dialog() {
    	
    	// show the Dialog when everything is prepared
    	Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			public void execute() {
				dialogBox.show();
		    	// and focus the set focusable element
				focusFocusElement();
			}
		});
		
	}
    
    /**
     * Focus the element set as focusElement, using a deferred call.
     */
    protected final void focusFocusElement() {
    	Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			public void execute() {
				if (focusElement != null) {
					focusElement.setFocus(true);
				}
			}
		});
    }
    
	/**
	 * Disable all enablable elements
	 * (this.enablableElements must be set correctly).
	 * Used to block the user from doing anything
	 * e.g. while waiting for an RPC call to complete.
	 */
	public final void deactivate() {
		activated = false;
		if (enablableElements == null) {
			dialogBox.setVisible(false);
		}
		else {
			for (HasEnabled element : enablableElements) {
				element.setEnabled(false);
			}
		}
	}
	
	/**
	 * Activate the dialog again,
	 * opposite of deactivate().
	 * @see deactivate()
	 * @param message
	 */
	public final void reactivate() {
		activated = true;
		if (enablableElements == null) {
			dialogBox.setVisible(true);
		}
		else {
			for (HasEnabled element : enablableElements) {
				element.setEnabled(true);
			}
		}
		focusFocusElement();
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

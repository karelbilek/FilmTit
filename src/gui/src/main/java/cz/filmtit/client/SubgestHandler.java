package cz.filmtit.client;

import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Universal event-handler for SubgestBoxes.
 * 
 * @author Honza Václ
 *
 */
public class SubgestHandler implements FocusHandler, KeyDownHandler, ValueChangeHandler<String> {
	Gui gui;
	
	/**
	 * Creates a new SubgestHandler.
	 * @param gui - reference to the main Gui class (for more global possibilities)
	 */
	public SubgestHandler(Gui gui) {
		this.gui = gui;
	}
	
	@Override
	public void onFocus(FocusEvent event) {
		if (event.getSource() instanceof SubgestBox) { // should be
			// hide the suggestion widget corresponding to the SubgestBox
			//   which previously had focus
			//deactivateSuggestionWidget(gui.getActiveSuggestionWidget());
			// and show a new one for the current SubgestBox
			SubgestBox subbox = (SubgestBox) event.getSource();
			subbox.showSuggestions();
			gui.setActiveSuggestionWidget(subbox.getSuggestionWidget());
		}
	}
	
	
	@Override
	public void onKeyDown(KeyDownEvent event) {
		if (event.getSource() instanceof SubgestBox) { // should be
			// pressing the Down arrow - setting focus to the suggestions:
			if ( isThisKeyEvent(event, KeyCodes.KEY_DOWN) ) {
				SubgestBox subbox = (SubgestBox) event.getSource();
				Focusable suggestionsList = ((Focusable) ((SimplePanel)subbox.getSuggestionWidget()).getWidget());
				suggestionsList.setFocus(true);
				event.preventDefault(); // default is to scroll down the page
			}
			// pressing Esc or Tab:
			if (     isThisKeyEvent(event, KeyCodes.KEY_ESCAPE)
				||   isThisKeyEvent(event, KeyCodes.KEY_TAB)   ) {
				// hide the suggestion widget corresponding to the SubgestBox
				//   which previously had focus (PopupPanel does not hide on keyboard events)
				deactivateSuggestionWidget();
			}
			// pressing Enter:
			if ( isThisKeyEvent(event, KeyCodes.KEY_ENTER)
				||	event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
				gui.log("enter pressed...");
				SubgestBox subbox = (SubgestBox) event.getSource();
				subbox.getTranslationResult().setUserTranslation(subbox.getText());
				gui.submitUserTranslation(subbox.getTranslationResult());
				
				deactivateSuggestionWidget();
				gui.goToNextBox(subbox);
			}
		}
	}
	
	private boolean isThisKeyEvent(KeyDownEvent event, int keycode) {
		return ( (event.getNativeEvent().getCharCode() == keycode)
			||   (event.getNativeKeyCode() == keycode) );
	}
	
	
	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		/*
		gui.log("valuechange handled: " + event.getValue());
		
		// all this should probably proceed only when explicitly submitted - e.g. by hitting Enter
		SubgestBox subbox = (SubgestBox) event.getSource();
		subbox.getTranslationResult().setUserTranslation(event.getValue());
		gui.submitUserTranslation(subbox.getTranslationResult());
		*/
	}
	
	
	private void deactivateSuggestionWidget() {
		Widget w = gui.getActiveSuggestionWidget();
		if (w != null) {
			if (w instanceof PopupPanel) {
				((PopupPanel)w).hide();
			}
			else {
				((Panel)(w.getParent())).remove(w);
			}
			gui.setActiveSuggestionWidget(null);
		}
	}

}

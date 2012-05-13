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
import com.google.gwt.user.client.ui.ScrollPanel;
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
			deactivateSuggestionWidget(gui.getActiveSuggestionWidget());
			// and show a new one for the current SubgestBox
			SubgestBox subbox = (SubgestBox) event.getSource();
			subbox.showSuggestions();
			gui.setActiveSuggestionWidget(subbox.getSuggestionWidget());
			
			/*
			// TODO: if the textbox is empty yet, direct the user to the suggestions:
			// - not working right now (why?)
			if (subbox.getText().isEmpty()) {
				gui.log("setting focus to the suggestions...");
				((Focusable)subbox.getSuggestionWidget()).setFocus(true);
			}
			*/
		}
	}
	
	
	@Override
	public void onKeyDown(KeyDownEvent event) {
		// pressing Esc:
		if ( (event.getNativeEvent().getCharCode() == KeyCodes.KEY_ESCAPE)
				||       (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) ) {
			// hide the suggestion widget corresponding to the SubgestBox
			//   which previously had focus
			deactivateSuggestionWidget(gui.getActiveSuggestionWidget());
		}
		// pressing Enter:
		if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
			SubgestBox subbox = (SubgestBox) event.getSource();
			subbox.getTranslationResult().setUserTranslation(subbox.getText());
			gui.submitUserTranslation(subbox.getTranslationResult());
			gui.goToNextBox(subbox);
		}
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
	
	
	private void deactivateSuggestionWidget(Widget w) {
		if (w != null) {
			((Panel)(w.getParent())).remove(w);
			w = null;
		}
	}

}

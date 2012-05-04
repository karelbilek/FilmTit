package cz.filmtit.client;

import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Universal event-handler for SubgestBoxes.
 * 
 * @author omikronn
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
	}
	
	
	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		gui.log("valuechange handled: " + event.getValue());
	}
	
	
	private void deactivateSuggestionWidget(Widget w) {
		if (w != null) {
			((Panel)(w.getParent())).remove(w);
			w = null;
		}
	}
	
}

package cz.filmtit.client;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Universal event-handler for SubgestBoxes.
 * 
 * @author Honza Václ
 *
 */
public class SubgestHandler implements FocusHandler, KeyDownHandler, ValueChangeHandler<String>, BlurHandler {
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
			final SubgestBox subbox = (SubgestBox) event.getSource();
            subbox.loadSuggestions();
			// hide the suggestion widget corresponding to the SubgestBox
			//   which previously had focus (if any)
			gui.deactivateSuggestionWidget();
			//gui.scrolledAutomatically = true;
			gui.guiStructure.scrollPanel.ensureVisible(subbox);
			//gui.scrolledAutomatically = false;
			// and show a new one for the current SubgestBox
			Scheduler.get().scheduleDeferred( new ScheduledCommand() {
				@Override
				public void execute() {
					subbox.showSuggestions();
					gui.setActiveSuggestionWidget(subbox.getSuggestionWidget());
				}
			} );
			gui.log("tabindex of this: " + subbox.getTabIndex());
		}
	}
	
	
	@Override
	public void onKeyDown(KeyDownEvent event) {
		if (event.getSource() instanceof SubgestBox) { // should be
			// pressing the Down arrow - setting focus to the suggestions:
			if     ( isThisKeyEvent(event, KeyCodes.KEY_DOWN) ) {
				event.preventDefault(); // default is to scroll down the page or to move to the next line in the textarea
				SubgestBox subbox = (SubgestBox) event.getSource();
				Focusable suggestionsList = ((Focusable) ((SimplePanel)subbox.getSuggestionWidget()).getWidget());
				gui.log("setting focus to suggestions");
				suggestionsList.setFocus(true);
			}
			// pressing Esc:
			else if ( isThisKeyEvent(event, KeyCodes.KEY_ESCAPE) ) {
				// hide the suggestion widget corresponding to the SubgestBox
				//   which previously had focus (PopupPanel does not hide on keyboard events)
				gui.deactivateSuggestionWidget();
			}
			// pressing Tab:
			else if ( isThisKeyEvent(event, KeyCodes.KEY_TAB) ) {
				event.preventDefault(); // e.g. in Chrome, default is to insert TAB character in the textarea
				gui.deactivateSuggestionWidget();
				SubgestBox subbox = (SubgestBox) event.getSource();
				boolean moved;
				if (event.isShiftKeyDown()) {
					moved = gui.goToPreviousBox(subbox);
				}
				else {
					moved = gui.goToNextBox(subbox);
				}
				/*
				if (!moved) {
					//gui.log("just setting visible");
					//subbox.getSuggestionWidget().setVisible(true);
					//subbox.showSuggestions();
				}
				*/
			}
			// pressing Enter:
			else if ( isThisKeyEvent(event, KeyCodes.KEY_ENTER) ) {
				//gui.log("enter pressed...");
				/*
				 * all this should happen on Blur (or something like that):
				SubgestBox subbox = (SubgestBox) event.getSource();
				subbox.getTranslationResult().setUserTranslation(subbox.getText());
				gui.submitUserTranslation(subbox.getTranslationResult());
				
				gui.deactivateSuggestionWidget();
				gui.goToNextBox(subbox);
				*/
			}
			
		}
	}
	
	
	/**
	 * Tell if the given event's key corresponds to the given keycode - in a various ways, hopefully
	 * compliant with all the major browsers...
	 * @param event
	 * @param keycode
	 * @return true if this KeyDownEvent's key has the given keycode, false otherwise
	 */
	private boolean isThisKeyEvent(KeyDownEvent event, int keycode) {
		return ( (event.getNativeEvent().getCharCode() == keycode)
			||   (event.getNativeKeyCode() == keycode)
			||   (event.getNativeEvent().getKeyCode() == keycode) );
	}
	
	
	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		/*
		if (event.getSource() instanceof SubgestBox) { // should be
			gui.log("valuechange handled: " + event.getValue());
			
			// all this should probably proceed only when explicitly submitted - e.g. by hitting Enter (or Tab)
			SubgestBox subbox = (SubgestBox) event.getSource();
			subbox.getTranslationResult().setUserTranslation(event.getValue());
			gui.submitUserTranslation(subbox.getTranslationResult());
		}
		*/
	}
	
	
	@Override
	public void onBlur(BlurEvent event) {
		if (event.getSource() instanceof SubgestBox) { // should be
			SubgestBox subbox = (SubgestBox) event.getSource();
			//gui.log("pseudo-valuechange handled - new value:" + subbox.getText());
			subbox.getTranslationResult().setUserTranslation(subbox.getText());
			gui.submitUserTranslation(subbox.getTranslationResult());
			
			//gui.deactivateSuggestionWidget();
		}
	}
	

}

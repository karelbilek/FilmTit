package cz.filmtit.client.subgestbox;

import com.google.gwt.event.dom.client.*;

import cz.filmtit.client.Gui;
import cz.filmtit.client.pages.TranslationWorkspace;
import cz.filmtit.client.widgets.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.SimplePanel;
import cz.filmtit.share.TimedChunk;

/**
 * Universal event-handler for SubgestBoxes.
 * 
 * @author Honza Václ
 *
 */
public class SubgestHandler implements FocusHandler, KeyDownHandler, KeyUpHandler, ValueChangeHandler<String>, BlurHandler {
	
    TranslationWorkspace workspace;
    //VLCWidget vlcPlayer;


	/**
	 * Creates a new SubgestHandler.
	 */
	public SubgestHandler(TranslationWorkspace workspace) {
		this.workspace = workspace;
        //this.vlcPlayer = vlcPlayer;
	}



	@Override
	public void onFocus(FocusEvent event) {
		if (event.getSource() instanceof SubgestBox) { // should be
            final SubgestBox subbox = (SubgestBox) event.getSource();
            long time = subbox.getChunk().getStartTimeLongNonZero();
            if (workspace.getVlcPlayer() != null) {
                workspace.getVlcPlayer().maybePlayWindow(time);
            }

            subbox.loadSuggestions();
            // hide the suggestion widget corresponding to the SubgestBox
			//   which previously had focus (if any)
            if (workspace == null) {
                Gui.log("workspace for handler is null when focusing!!!");
                final Throwable throwable = new IllegalArgumentException("Hello");
            } else {
                workspace.deactivateSuggestionWidget();
                workspace.ensureVisible(subbox);
                // and show a new one for the current SubgestBox
                Scheduler.get().scheduleDeferred( new ScheduledCommand() {
                    @Override
                    public void execute() {
                        subbox.showSuggestions();
                        workspace.setActiveSuggestionWidget(subbox.getSuggestionWidget());
                    }
                } );
            }
			Gui.log("tabindex of this: " + subbox.getTabIndex());

            if (Window.Navigator.getUserAgent().matches(".*Firefox.*")) {
                //Gui.log("in firefox - scheduling resetting focus");
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        subbox.setFocus(true);
                    }
                });
            }

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
				Gui.log("setting focus to suggestions");
				suggestionsList.setFocus(true);
			}
			// pressing Esc:
			else if ( isThisKeyEvent(event, KeyCodes.KEY_ESCAPE) ) {
				// hide the suggestion widget corresponding to the SubgestBox
				//   which previously had focus (PopupPanel does not hide on keyboard events)
				workspace.deactivateSuggestionWidget();
			}
			// pressing Tab:
			else if ( isThisKeyEvent(event, KeyCodes.KEY_TAB) ) {
				event.preventDefault(); // e.g. in Chrome, default is to insert TAB character in the textarea
                workspace.deactivateSuggestionWidget();
				SubgestBox subbox = (SubgestBox) event.getSource();
				boolean moved;
				if (event.isShiftKeyDown()) {
					moved = workspace.goToPreviousBox(subbox);
				}
				else {
					moved = workspace.goToNextBox(subbox);
				}
				/*
				if (!moved) {
					subbox.showSuggestions();
				}
				*/
			}
			// pressing Enter:
			else if ( isThisKeyEvent(event, KeyCodes.KEY_ENTER) ) {
//				Gui.log("enter pressed...");
//				// all this should happen on Blur (or something like that):
//				SubgestBox subbox = (SubgestBox) event.getSource();
//				subbox.getTranslationResult().setUserTranslation(subbox.getText());
//				gui.submitUserTranslation(subbox.getTranslationResult());
//				
//				gui.deactivateSuggestionWidget();
//				gui.goToNextBox(subbox);
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
//        // all this should proceed only when explicitly submitted - e.g. by hitting Enter (or Tab)
//        if (event.getSource() instanceof SubgestBox) { // should be
//          Gui.log("valuechange handled: " + event.getValue());
//
//          SubgestBox subbox = (SubgestBox) event.getSource();
//          subbox.getTranslationResult().setUserTranslation(event.getValue());
//          gui.submitUserTranslation(subbox.getTranslationResult());
//        }
	}
	
	
	@Override
	public void onBlur(BlurEvent event) {
		if (event.getSource() instanceof SubgestBox) { // should be
			SubgestBox subbox = (SubgestBox) event.getSource();
			//Gui.log("pseudo-valuechange handled - new value:" + subbox.getText());
			subbox.getTranslationResult().setUserTranslation(subbox.getTextWithNewlines());

            // submitting only when the contents have changed
            if (subbox.textChanged()) {
                workspace.submitUserTranslation(subbox.getTranslationResult());
                subbox.updateLastText();
            }

			//gui.deactivateSuggestionWidget();
		}
	}


    @Override
    public void onKeyUp(KeyUpEvent event) {
        if (event.getSource() instanceof SubgestBox) { // should be
            // Gui.log("keyup caught on subgestbox");
            final SubgestBox subbox = (SubgestBox) event.getSource();
            // recalculating number of lines and auto-resize:
            Scheduler.get().scheduleDeferred( new ScheduledCommand() {
                @Override
                public void execute() {
                    subbox.updateVerticalSize();
                }
            } );
        }
    }
}

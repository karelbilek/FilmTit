package cz.filmtit.client;

import com.github.gwtbootstrap.client.ui.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import cz.filmtit.share.*;

import java.util.List;

public class FilmTitServiceHandler {
	// FilmTitServiceAsync should be created automatically by Maven
	// from FilmTitService during compilation (or generated as a QuickFix in Eclipse)
	private FilmTitServiceAsync filmTitSvc;
	private Gui gui;

    int windowsDisplayed = 0;
    public  void displayWindow(String message) {
        if (windowsDisplayed < 10) {
            windowsDisplayed++;
            Window.alert(message);
            if (windowsDisplayed==10) {
                Window.alert("Last window displayed.");
            }
        } else {
      //      gui.log("ERROR - message");
        }
    }
	
	public FilmTitServiceHandler(Gui gui) {
		filmTitSvc = GWT.create(FilmTitService.class);
		this.gui = gui;
	}
	
	public void createDocument(String movieTitle, String year, String language, final String subtext) {

		AsyncCallback<DocumentResponse> callback = new AsyncCallback<DocumentResponse>() {
			
			public void onSuccess(final DocumentResponse result) {
				gui.log("DocumentResponse arrived, showing dialog with MediaSource suggestions...");
                gui.setCurrentDocument(result.document);

                final DialogBox dialogBox = new DialogBox(false);
                final MediaSelector mediaSelector = new MediaSelector(result.mediaSourceSuggestions);
                mediaSelector.submitButton.addClickHandler( new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        dialogBox.hide();
                        selectSource(result.document.getId(), mediaSelector.getSelected());
                        gui.log("document created successfully.");

                        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                            public void execute() {
                                gui.processText(subtext);
                            }
                        });
                    }
                } );
                dialogBox.setWidget(mediaSelector);
                dialogBox.setGlassEnabled(true);
                dialogBox.center();
            }
			
			public void onFailure(Throwable caught) {
				// TODO: repeat sending a few times, then ask user
				displayWindow(caught.getLocalizedMessage());
				gui.log("failure on creating document!");
			}

		};
		
		filmTitSvc.createNewDocument(movieTitle, year, language, callback);
	}
	
	public void getTranslationResults(List<TimedChunk> chunks) {
		
		AsyncCallback<List<TranslationResult>> callback = new AsyncCallback<List<TranslationResult>>() {
			
			public void onSuccess(List<TranslationResult> newresults) {
				// add to trlist to the correct position:
				List<TranslationResult> translist = gui.getCurrentDocument().translationResults;
			
                for (TranslationResult newresult:newresults){

                    int index = newresult.getSourceChunk().getIndex();
                    translist.set(index, newresult);
                    
                    gui.showResult(newresult, index);
                }
			}
			
			public void onFailure(Throwable caught) {
				// TODO: repeat sending a few times, then ask user
				displayWindow(caught.getLocalizedMessage());
				gui.log("failure on receiving some chunk!");
				gui.log(caught.toString());				
				StackTraceElement[] st = caught.getStackTrace();
				for (StackTraceElement stackTraceElement : st) {
					gui.log(stackTraceElement.toString());
				}
			}
		};
		
		filmTitSvc.getTranslationResults(chunks, callback);
	}
	
	public void setUserTranslation(int chunkId, long documentId, String userTranslation, long chosenTranslationPair) {
		
		AsyncCallback<Void> callback = new AsyncCallback<Void>() {
			
			public void onSuccess(Void o) {
				gui.log("setUserTranslation() succeeded");
			}
			
			public void onFailure(Throwable caught) {
				gui.log("ERROR: setUserTranslation() didn't succeed!");
				// TODO: repeat sending a few times, then ask user
			}
		};
		
		filmTitSvc.setUserTranslation(chunkId, documentId, userTranslation, chosenTranslationPair, callback);
	}



    public void selectSource(long documentID, MediaSource selectedMediaSource) {
        AsyncCallback<Void> callback = new AsyncCallback<Void>() {

            public void onSuccess(Void o) {
                gui.log("selectSource() succeeded");
            }

            public void onFailure(Throwable caught) {
                gui.log("ERROR: selectSource() didn't succeed!");
                // TODO: repeat sending a few times, then ask user
            }
        };

        filmTitSvc.selectSource(documentID, selectedMediaSource, callback);
    }

}

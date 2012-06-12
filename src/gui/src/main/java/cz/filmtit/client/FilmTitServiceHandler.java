package cz.filmtit.client;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

import cz.filmtit.share.Document;
import cz.filmtit.share.FilmTitService;
import cz.filmtit.share.FilmTitServiceAsync;
import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.TranslationResult;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

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
	
	public void createDocument(String movieTitle, String year, String language) {
		
		AsyncCallback<Document> callback = new AsyncCallback<Document>() {
			
			public void onSuccess(Document result) {
				gui.setCurrentDocument(result);
				gui.log( "succesfully created document: " + result.getId());
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					public void execute() {
						gui.processText();
					}
				});
			}
			
			public void onFailure(Throwable caught) {
				// TODO: repeat sending a few times, then ask user
				displayWindow(caught.getLocalizedMessage());
				gui.log("failure on creating document!");
			}

		};
		
		filmTitSvc.createDocument(movieTitle, year, language, callback);
	}
	
	public void getTranslationResults(TimedChunk chunk) {
		
		// assert(filmTitSvc != null);
		// most probably not necessary
		// if (filmTitSvc == null) {
		//	filmTitSvc = GWT.create(FilmTitService.class);
		// }
		
		AsyncCallback<TranslationResult> callback = new AsyncCallback<TranslationResult>() {
			
			public void onSuccess(TranslationResult newresult) {
				//gui.getCurrentDocument().translationResults.add(result);
				// add to trlist to the correct position:
				List<TranslationResult> translist = gui.getCurrentDocument().translationResults;
				/*
				int index;
				for (index = 0; index < translist.size(); index++) {
					if (newresult.compareTo(translist.get(index)) < 0) {
						break;
					}
				}
				*/
				
				int index = newresult.getSourceChunk().getId();
				//gui.log("inserting chunk on position " + index);
				translist.set(index, newresult);
				
				gui.showResult(newresult, index);
				//gui.log("succesfully received result of chunk: " + newresult.getSourceChunk().getSurfaceForm());
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
		
		filmTitSvc.getTranslationResults(chunk, callback);
	}
	
	public void setUserTranslation(int chunkId, long documentId, String userTranslation, long chosenTranslationPair) {
		
		AsyncCallback<Void> callback = new AsyncCallback<Void>() {
			
			public void onSuccess(Void o) {
				//TODO: do something?
				gui.log("setUserTranslation() succeeded");
			}
			
			public void onFailure(Throwable caught) {
				displayWindow(caught.getLocalizedMessage());
				gui.log("setUserTranslation() didn't succeed");
				// TODO: repeat sending a few times, then ask user
			}
		};
		
		filmTitSvc.setUserTranslation(chunkId, documentId, userTranslation, chosenTranslationPair, callback);
	}
		
}

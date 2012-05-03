package cz.filmtit.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

import cz.filmtit.share.FilmTitServiceAsync;
import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.TranslationResult;

public class FilmTitServiceHandler {
	// FilmTitServiceAsync should be created automatically by Maven
	// from FilmTitService during compilation (or generated as a QuickFix in Eclipse)
	private FilmTitServiceAsync filmTitSvc;
	private Gui gui;
	
	public FilmTitServiceHandler(Gui gui) {
		filmTitSvc = GWT.create(FilmTitService.class);
		this.gui = gui;
	}
	
	public void getTranslationResults(TimedChunk chunk) {
		
		// assert(filmTitSvc != null);
		// most probably not necessary
		// if (filmTitSvc == null) {
		//	filmTitSvc = GWT.create(FilmTitService.class);
		// }
		
		AsyncCallback<TranslationResult> callback = new AsyncCallback<TranslationResult>() {
			
			public void onSuccess(TranslationResult result) {
				// TODO: add to trlist to the correct position
				gui.getCurrentDocument().translationResults.add(result);
				gui.log( "succesfully received result of chunk: " + result.getSourceChunk().getSurfaceForm());
			}
			
			public void onFailure(Throwable caught) {
				// TODO: repeat sending a few times, then ask user
				Window.alert(caught.getLocalizedMessage());
				gui.log("failure on receiving some chunk!");
			}
		};
		
		filmTitSvc.getTranslationResults(chunk, callback);
	}
	
	public void setUserTranslation(long translationResultId, String userTranslation, long chosenTranslationPair) {
		
		AsyncCallback<Void> callback = new AsyncCallback<Void>() {
			
			public void onSuccess(Void o) {
				//TODO: do something?
			}
			
			public void onFailure(Throwable caught) {
				Window.alert(caught.getLocalizedMessage());
				// TODO: repeat sending a few times, then ask user
			}
		};
		
		filmTitSvc.setUserTranslation(translationResultId, userTranslation, chosenTranslationPair, callback);
	}
		
}

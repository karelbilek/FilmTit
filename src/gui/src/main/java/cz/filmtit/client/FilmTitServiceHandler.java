package cz.filmtit.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.TranslationResult;

public class FilmTitServiceHandler {
	// FilmTitServiceAsync should be created automatically
	// from FilmTitService during compilation...?
	private FilmTitServiceAsync filmTitSvc;
	private Gui gui;
	
	public FilmTitServiceHandler(Gui gui) {
		filmTitSvc = GWT.create(FilmTitService.class);
		this.gui = gui;
	}
	
	public void suggestions(TimedChunk chunk) {
		
		// assert(filmTitSvc != null);
		// most probably not necessary
		// if (filmTitSvc == null) {
		//	filmTitSvc = GWT.create(FilmTitService.class);
		// }
		
		AsyncCallback<TranslationResult> callback = new AsyncCallback<TranslationResult>() {
			
			public void onSuccess(TranslationResult result) {
				// TODO: add to trlist to the correct position
				gui.trlist.addTranslationResult(result);
			}
			
			public void onFailure(Throwable caught) {
				// TODO: repeat sending a few times, then ask user
				Window.alert(caught.getLocalizedMessage());
			}
		};
		
		filmTitSvc.suggestions(chunk, callback);
	}
	
	public void feedback (long translationResultId, long chosenTranslationPair, String userTranslation) {
		
		AsyncCallback<Object> callback = new AsyncCallback<Object>() {
			
			public void onSuccess(Object o) {
				//TODO: do something?
			}
			
			public void onFailure(Throwable caught) {
				Window.alert(caught.getLocalizedMessage());
				// TODO: repeat sending a few times, then ask user
			}
		};
		
		filmTitSvc.feedback(translationResultId, chosenTranslationPair, userTranslation, callback);
	}
		
}

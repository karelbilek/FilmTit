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
	private GUITranslationResultList trlist;
	
	public FilmTitServiceHandler(GUITranslationResultList trlist) {
		filmTitSvc = GWT.create(FilmTitService.class);
		this.trlist = trlist;
	}
	
	public void suggestions(TimedChunk chunk) {
		
		// assert(filmTitSvc != null);
		// most probably not necessary
		// if (filmTitSvc == null) {
		//	filmTitSvc = GWT.create(FilmTitService.class);
		// }
		
		AsyncCallback<TranslationResult> callback = new AsyncCallback<TranslationResult>() {
			
			public void onSuccess(TranslationResult result) {
				// TODO
				trlist.addTranslationResult(result);
			}
			
			public void onFailure(Throwable caught) {
				Window.alert(caught.getLocalizedMessage());
			}
		};
		
		filmTitSvc.suggestions(chunk, callback);
	}
		
}

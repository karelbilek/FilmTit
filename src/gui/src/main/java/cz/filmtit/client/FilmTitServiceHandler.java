package cz.filmtit.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;

import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.TranslationResult;

public class FilmTitServiceHandler {
	// FilmTitServiceAsync should be created automatically
	// from FilmTitService during compilation...?
	private FilmTitServiceAsync filmTitSvc = GWT.create(FilmTitService.class);
	
	// TODO
	Button btnTranslate = new Button("Translate");
	translation = new Label("");

	public void suggestions(TimedChunk chunk) {
		btnTranslate.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				// handling todleto
				if (filmTitSvc == null) {
					filmTitSvc = GWT.create(FilmTitService.class);
				}
				
				AsyncCallback<TranslationResult> callback = new AsyncCallback<TranslationResult>() {
					
					public void onSuccess(TranslationResult result) {
						translation.setText(result.getUserTranslation());
					}
					
					public void onFailure(Throwable caught) {
						Window.alert(caught.getLocalizedMessage());
					}
				};
				
				TimedChunk chunk = new TimedChunk(txtbxText.getText());
				filmTitSvc.suggestions(chunk, callback);
				
			}
		});
	}
	
	
}

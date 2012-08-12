package cz.filmtit.client.callables;

import java.util.List;

import cz.filmtit.client.Callable;
import cz.filmtit.client.Gui;
import cz.filmtit.client.ReceivesSelectSource;
import cz.filmtit.client.dialogs.MediaSelector;
import cz.filmtit.share.MediaSource;

public class ChangeMovieTitle extends Callable<List<MediaSource>> implements
		ReceivesSelectSource {

	private long documentID;
	private String newMovieTitle;
	
	@Override
	public String getName() {
		return getNameWithParameters(documentID, newMovieTitle);
	}
	
	public ChangeMovieTitle(long documentID, String newMovieTitle) {
		super();
		
		if (newMovieTitle != null && !newMovieTitle.isEmpty()) {
			this.documentID = documentID;
			this.newMovieTitle = newMovieTitle;
			enqueue();
		}
		else {
			Gui.getPageHandler().refresh();
		}
		
	}

	@Override
	protected void call() {
		filmTitService.changeMovieTitle(Gui.getSessionID(), documentID, newMovieTitle, this);
	}

	@Override
	public void onSuccessAfterLog(List<MediaSource> mediaSourceSuggestions) {
        new MediaSelector(mediaSourceSuggestions, this);
	}
	
	@Override
	protected void onFinalError(String message) {
		Gui.getPageHandler().refresh();
		super.onFinalError(message);
	}

	@Override
	public void selectSource(MediaSource selectedMediaSource) {
		new SelectSourceUserPage(documentID, selectedMediaSource);
	}

}

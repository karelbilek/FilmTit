package cz.filmtit.client.callables;

import java.util.List;

import cz.filmtit.client.Callable;
import cz.filmtit.client.Gui;
import cz.filmtit.client.ReceivesSelectSource;
import cz.filmtit.client.PageHandler.Page;
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
		
		if (newMovieTitle == null || newMovieTitle.isEmpty()) {
			// we don't accept the new title - refresh to load the old title
			Gui.getPageHandler().refresh();
		}
		else {
			this.documentID = documentID;
			this.newMovieTitle = newMovieTitle;
			enqueue();
		}
		
	}

	@Override
	protected void call() {
		filmTitService.changeMovieTitle(Gui.getSessionID(), documentID, newMovieTitle, this);
	}

	@Override
	public void onSuccessAfterLog(List<MediaSource> mediaSourceSuggestions) {
		if (mediaSourceSuggestions == null || mediaSourceSuggestions.isEmpty()) {
			// no choice at all
			new SelectSourceUserPage(documentID, null, false);
			// TODO: assert false?
		}
		else if (mediaSourceSuggestions.size() == 1) {
			// no real choice -> just use the one result we got without asking
			new SelectSourceUserPage(documentID, mediaSourceSuggestions.get(0), false);
		}
		else {
			// multiple choices, user must decide
	        new MediaSelector(mediaSourceSuggestions, this);
		}
	}
	
	@Override
	protected void onFinalError(String message) {
		Gui.getPageHandler().refreshIf(Page.UserPage);
		super.onFinalError(message);
	}

	@Override
	public void selectSource(MediaSource selectedMediaSource) {
		new SelectSourceUserPage(documentID, selectedMediaSource, true);
	}

}

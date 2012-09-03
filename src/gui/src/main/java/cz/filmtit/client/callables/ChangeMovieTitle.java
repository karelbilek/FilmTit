/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/

package cz.filmtit.client.callables;

import java.util.List;

import cz.filmtit.client.Callable;
import cz.filmtit.client.Gui;
import cz.filmtit.client.ReceivesSelectSource;
import cz.filmtit.client.PageHandler.Page;
import cz.filmtit.client.dialogs.MediaSelector;
import cz.filmtit.share.MediaSource;

/**
 * Returns media source suggestions based on newMovieTitle, and opens a MediaSelector with them.
 * The movie title is not changed yet:
 * this is only done on calling selectSource.
 * If there is only one suggestion, selects the media source automatically without opening a MediaSelector.
 * @author rur
 *
 */
public class ChangeMovieTitle extends Callable<List<MediaSource>> implements
		ReceivesSelectSource {

	private long documentID;
	private String newMovieTitle;
	
	@Override
	public String getName() {
		return getNameWithParameters(documentID, newMovieTitle);
	}
	
	/**
	 * Returns media source suggestions based on newMovieTitle, and opens a MediaSelector with them.
	 * The movie title is not changed yet:
	 * this is only done on calling selectSource.
	 * If there is only one suggestion, selects the media source automatically without opening a MediaSelector.
	 */
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

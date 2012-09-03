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

import cz.filmtit.client.Callable;
import cz.filmtit.client.Gui;
import cz.filmtit.client.PageHandler;
import cz.filmtit.client.PageHandler.Page;
import cz.filmtit.share.Document;

/**
 * Sets a different title for the document.
 * @author rur
 *
 */
public class ChangeDocumentTitle extends Callable<Void> {

	private long documentID;
	private String newTitle;
	
	@Override
	public String getName() {
		return getNameWithParameters(documentID, newTitle);
	}
	
	/**
	 * Sets a different title for the document.
	 */
	public ChangeDocumentTitle(long documentID, String newTitle) {
		super();
		
		if (newTitle == null || newTitle.isEmpty()) {
			// we don't accept the new title - refresh to load the old title
			Gui.getPageHandler().refresh();
		}
		else {
			this.newTitle = newTitle;
			this.documentID = documentID;
			enqueue();
		}
	}

	@Override
	protected void call() {
		filmTitService.changeDocumentTitle(Gui.getSessionID(), documentID, newTitle, this);
	}

	@Override
	protected void onFinalError(String message) {
		// we don't accept the new title - refresh to load the old title
		Gui.getPageHandler().refreshIf(Page.UserPage);
		super.onFinalError(message);
	}

}

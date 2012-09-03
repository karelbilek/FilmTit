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
import cz.filmtit.client.pages.TranslationWorkspace;
import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.TranslationResult;

/**
 * Change the source text of the chunk,
 * resulting in new translation suggestions
 * which are sent as the result
 * and shown in the Translation Workspace.
 * @author rur
 *
 */
public class ChangeSourceChunk extends Callable<TranslationResult> {

	private TimedChunk chunk;
	private String newText;
	private TranslationWorkspace workspace;
	
	@Override
	public String getName() {
		return getNameWithParameters(chunk, newText);
	}
	
	/**
	 * Change the source text of the chunk,
	 * resulting in new translation suggestions
	 * which are sent as the result
	 * and shown in the Translation Workspace.
	 */
	public ChangeSourceChunk(TimedChunk chunk, String newText, TranslationWorkspace workspace) {
		super();
		
		this.chunk = chunk;
		this.newText = newText;
		this.workspace = workspace;
		
		enqueue();
	}

	@Override
	protected void call() {
		filmTitService.changeText(Gui.getSessionID(), chunk, newText, this);
	}

	@Override
	public void onSuccessAfterLog(TranslationResult result) {
		workspace.showResult(result);
	}

}

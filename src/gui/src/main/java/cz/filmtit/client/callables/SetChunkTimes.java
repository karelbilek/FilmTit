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

import com.google.gwt.user.client.Window;

import cz.filmtit.client.Callable;
import cz.filmtit.client.Gui;
import cz.filmtit.share.ChunkIndex;
import cz.filmtit.share.TimedChunk;

/**
 * Updated the chunk's start time and end time in the UserSpace database.
 * @author rur
 */
public class SetChunkTimes extends Callable<Void> {

	private TimedChunk chunk;
	
	@Override
	public String getName() {
		return getNameWithParameters(chunk.getChunkIndex(), chunk.getStartTime(), chunk.getEndTime());
	}
	
	
	/**
	 * Updated the chunk's start time and end time in the UserSpace database.
	 */
	public SetChunkTimes(TimedChunk chunk) {
		// TODO: change to SetChunkTimes(TimedChunk... chunks)
		super();
		
		this.chunk = chunk;
		
		enqueue();
	}

	@Override
	protected void call() {
		filmTitService.setChunkTimes(Gui.getSessionID(), chunk.getChunkIndex(), chunk.getDocumentId(), chunk.getStartTime(), chunk.getEndTime(), this);
	}

}

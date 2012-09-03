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
import com.google.gwt.user.client.*;

import cz.filmtit.client.*;

import cz.filmtit.client.pages.TranslationWorkspace;
import cz.filmtit.client.pages.TranslationWorkspace.SendChunksCommand;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.core.client.*;
import cz.filmtit.share.*;
import java.util.*;

/**
 * Stop generating translation results for the given chunks
 * (to be called after getTranslationResults has been called
 * with the given chunks but the results are suddenly not needed anymore).
 * @author rur
 *
 */
public class StopTranslationResults extends Callable<Void> {
	
	// parameters
	private List<TimedChunk> chunks;

    @Override
    public String getName() {
        return "StopTranslationResults (chunks size: "+chunks.size()+")";
    }

    // ignore the errors, it does not matter that much if this one fails
    
	@Override
	protected void onInvalidSession() {
		// nothing
	}
	
	@Override
	protected void onFinalError(String message) {
		// nothing
	}
    		
	/**
	 * Stop generating translation results for the given chunks
	 * (to be called after getTranslationResults has been called
	 * with the given chunks but the results are suddenly not needed anymore).
	 */
	public StopTranslationResults(List<TimedChunk> chunks) {
		super();
		
		this.chunks = chunks;
		
		// + 0.1s for each chunk
		callTimeOut += 100 * chunks.size();
		
		enqueue();
	}

	@Override protected void call() {
        filmTitService.stopTranslationResults(Gui.getSessionID(), chunks, this);
	}
}


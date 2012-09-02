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


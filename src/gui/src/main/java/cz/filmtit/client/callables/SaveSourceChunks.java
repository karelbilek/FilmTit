package cz.filmtit.client.callables;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;

import cz.filmtit.client.*;
import cz.filmtit.client.PageHandler.Page;
import cz.filmtit.client.pages.TranslationWorkspace;

import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.core.client.*;
import cz.filmtit.share.*;
import java.util.*;

public class SaveSourceChunks extends Callable<Void> {
		
		// parameters
		List<TimedChunk> chunks;
        TranslationWorkspace workspace;
	
        @Override
	    public String getName() {
            return "SaveSourceChunks (chunks size: "+chunks.size()+")";
        }

        @Override
        public void onSuccessAfterLog(Void o) {
            workspace.showSources(chunks);
        }
        
        @Override
        protected void onFinalError(String message) {
	        Gui.getPageHandler().loadPage(Page.DocumentCreator, true);
	        // TODO remember at least document title
	        super.onFinalError(message);
        }
		
		// constructor
		public SaveSourceChunks(List<TimedChunk> chunks, TranslationWorkspace workspace) {
			super();
			
			this.chunks = chunks;
            this.workspace = workspace;

			// 10s + 0.1s for each chunk
			callTimeOut = 10000 + 100 * chunks.size();
			
			enqueue();
		}

		@Override protected void call() {
            filmTitService.saveSourceChunks(Gui.getSessionID(), chunks, this);
		}
}


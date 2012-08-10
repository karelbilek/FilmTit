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

	public class GetTranslationResults extends Callable<List<TranslationResult>> {
		
		// parameters
		List<TimedChunk> chunks;
		SendChunksCommand command;
		TranslationWorkspace workspace;
		
		int id;
		static int nextId = 0;
	
        @Override
        public String getName() {
            return "GetTranslationResults (chunks size: "+chunks.size()+")";
        }

		@Override	
        public void onSuccessAfterLog(List<TranslationResult> newresults) {
			
            if (workspace.getStopLoading()) {
            	return;
            }
            
            workspace.removeGetTranslationsResultsCall(id);

            for (TranslationResult newresult:newresults) {

                ChunkIndex poi = newresult.getSourceChunk().getChunkIndex();
                workspace.showResult(newresult);                	
            
            }
            command.execute();
        }
		
		@Override
		public void onFailureAfterLog(Throwable returned) {
			super.onFailureAfterLog(returned);
            workspace.removeGetTranslationsResultsCall(id);
		}
		
		@Override
		protected void onProbablyOffline(Throwable returned) {
			super.onProbablyOffline(returned);
            workspace.removeGetTranslationsResultsCall(id);
		}
		
		// constructor
		public GetTranslationResults(List<TimedChunk> chunks,
				SendChunksCommand command, TranslationWorkspace workspace) {
			super();
			
			this.chunks = chunks;
			this.command = command;
			this.workspace = workspace;
			
			// 20s + 2s for each chunk
			callTimeOut = 20000 + 2000 * chunks.size();
			
			enqueue();
		}

		@Override protected void call() {
			id = nextId++;
			workspace.addGetTranslationsResultsCall(id, this);
            filmTitService.getTranslationResults(gui.getSessionID(), chunks, this);
		}
		
		public void stop() {
			new StopTranslationResults(chunks);
		}
	}


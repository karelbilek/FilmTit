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
import cz.filmtit.share.exceptions.InvalidValueException;

import java.util.*;

/**
 * Save the given source chunks as the contents of the given document
 * (which was already created by calling createNewDocument).
 * Shows the sources in TranslationWorkspace on success.
 * @author rur
 *
 */
public class SaveSourceChunks extends Callable<Void> {
		
		// parameters
		private List<TimedChunk> chunks;
		private TranslationWorkspace workspace;
		private CreateDocument createDocumentCall;
	
        @Override
	    public String getName() {
            return "SaveSourceChunks (chunks size: "+chunks.size()+")";
        }

        @Override
        public void onSuccessAfterLog(Void o) {
        	if (workspace != null) {
                workspace.showSources(chunks);
        	}
        }
        
        @Override
        public void onFailureAfterLog(Throwable returned) {
	        if (returned instanceof InvalidValueException) {
	        	// the file format is invalid, lets delete the document
	        	onFinalError(returned.getLocalizedMessage());
	        }
	        else {
		        super.onFailureAfterLog(returned);
	        }
        }
        
        @Override
        protected void onFinalError(String message) {
        	if (createDocumentCall != null) {
    			createDocumentCall.hideMediaSelector();
        	}
        	new DeleteDocumentSilently(chunks.get(0).getDocumentId());
	        Gui.getPageHandler().loadPage(Page.DocumentCreator, true);
	        // TODO remember at least document title
	        super.onFinalError(message);
        }
		
		/**
		 * Save the given source chunks as the contents of the given document
		 * (which was already created by calling createNewDocument).
		 * Shows the sources in TranslationWorkspace on success.
	     * @param createDocument reference to the call that created the document
	     * and now probably holds a reference to an open MediaSelector
         */
		public SaveSourceChunks(List<TimedChunk> chunks, TranslationWorkspace workspace, CreateDocument createDocumentCall) {
			super();
			
			if (chunks == null || chunks.isEmpty()) {
				return;
			}
			else {
				this.chunks = chunks;
	            this.workspace = workspace;
	            this.createDocumentCall = createDocumentCall;

				// + 0.1s for each chunk
				callTimeOut += 100 * chunks.size();
				
				enqueue();
			}
		}

		@Override protected void call() {
            filmTitService.saveSourceChunks(Gui.getSessionID(), chunks, this);
		}
}


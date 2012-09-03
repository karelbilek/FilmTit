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
import java.util.*;

/**
 * Sets the media source of the document when changing the movie name on UserPage.
 * The media source represents the movie or series which the subtitles come from.
 * Refreshes the UserPage on success if refreshOnSuccess set to true.
 * Refreshes it always on failure.
 * @author rur
 *
 */
public class SelectSourceUserPage extends Callable<Void> {
    	
    	// parameters
    	private long documentID;	
    	private MediaSource selectedMediaSource;
		private boolean refreshOnSuccess;
   
        @Override
        public String getName() {
            return getNameWithParameters(documentID, selectedMediaSource);
        }

        @Override
        public void onSuccessAfterLog(Void o) {
        	if (refreshOnSuccess) {
                Gui.getPageHandler().refreshIf(Page.UserPage);
        	}
        }
        
        @Override
        protected void onFinalError(String message) {
            Gui.getPageHandler().refreshIf(Page.UserPage);
            super.onFinalError(message);
        }

        /**
		 * Sets the media source of the document when changing the movie name on UserPage.
		 * The media source represents the movie or series which the subtitles come from.
		 * Refreshes the UserPage on success if refreshOnSuccess set to true.
		 * Refreshes it always on failure.
         */
		public SelectSourceUserPage(long documentID, MediaSource selectedMediaSource, boolean refreshOnSuccess) {
			super();
			
			this.documentID = documentID;
			this.refreshOnSuccess = refreshOnSuccess;
			
			if (selectedMediaSource != null) {
				this.selectedMediaSource = selectedMediaSource;
				enqueue();
			} else {
				// ignore
				onSuccessAfterLog(null);
			}
		}

		@Override protected void call() {
	        filmTitService.selectSource( Gui.getSessionID(), documentID, selectedMediaSource, this);
		}
	}

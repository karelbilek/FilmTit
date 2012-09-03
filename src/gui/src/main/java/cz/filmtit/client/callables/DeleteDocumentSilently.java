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
import cz.filmtit.client.pages.TranslationWorkspace.DocumentOrigin;

import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.core.client.*;
import cz.filmtit.share.*;
import java.util.*;

/**
 * Tries to delete the given document but does not display any errors to the user.
 * To be used if the document deletion command is sent by the application,
 * e.g. if there is an exception while parsing its subtitles.
 */
public class DeleteDocumentSilently extends cz.filmtit.client.Callable<Void> {
    private long documentId;
    
    @Override
    public String getName() {
        return getNameWithParameters(documentId);
    }

    @Override
    public void onSuccessAfterLog(Void o) {
        // good
    	Gui.getPageHandler().refreshIf(Page.UserPage);
    }
    
    @Override
    protected void onFinalError(String message) {
    	// bad but ignore
    }
    
    @Override
    protected void onInvalidSession() {
    	// try to call again when logged in again
    	addCallToBeCalled();
    	// do not display the login dialog
    }
        
    /**
     * Tries to delete the given document but does not display any errors to the user.
     * To be used if the document deletion command is sent by the application,
     * e.g. if there is an exception while parsing its subtitles.
     */
    public DeleteDocumentSilently(long id) {
        super();
        
        documentId = id;
        
        enqueue();
    }

    @Override protected void call() {
        filmTitService.deleteDocument(Gui.getSessionID(), documentId, this);
    }

}


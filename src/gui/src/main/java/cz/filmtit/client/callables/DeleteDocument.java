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
import cz.filmtit.share.exceptions.InvalidDocumentIdException;
import java.util.*;

/**
 * Remove the given document from the list of user's documents.
 * @author rur
 *
 */
public class DeleteDocument extends cz.filmtit.client.Callable<Void> {
	private long documentId;
    
    @Override
    public String getName() {
        return getNameWithParameters(documentId);
    }

    @Override
    public void onSuccessAfterLog(Void o) {
        Gui.getPageHandler().refreshIf(Page.UserPage);
    }
    
    @Override
    public void onFailureAfterLog(Throwable returned) {
    	if (returned instanceof InvalidDocumentIdException) {
    		Gui.log("Actually deletion succeeded because the document does not exist.");
    		onSuccessAfterLog(null);
    	}
    	else {
        	super.onFailureAfterLog(returned);
    	}
    }
    
    @Override
    protected void onFinalError(String message) {
        Gui.getPageHandler().refreshIf(Page.UserPage);
    	super.onFinalError(message);
    }
     
    /**
     * Remove the given document from the list of user's documents.
     */
    public DeleteDocument(long id) {
        super();
        
        documentId = id;
        
        enqueue();
    }

    @Override protected void call() {
        filmTitService.deleteDocument(Gui.getSessionID(), documentId, this);
    }

}


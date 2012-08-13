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

public class DeleteDocument extends cz.filmtit.client.Callable<Void> {
    long documentId;
    
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
        
    public DeleteDocument(long id) {
        super();
        
        documentId = id;
        
        enqueue();
    }

    @Override protected void call() {
        filmTitService.deleteDocument(Gui.getSessionID(), documentId, this);
    }

}


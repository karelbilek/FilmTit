package cz.filmtit.client.callables;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;

import cz.filmtit.client.*;
import cz.filmtit.client.pages.TranslationWorkspace;
import cz.filmtit.client.pages.TranslationWorkspace.DocumentOrigin;

import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.core.client.*;
import cz.filmtit.share.*;
import java.util.*;

public class DeleteDocument extends cz.filmtit.client.Callable<Void> {
    long documentId;
    
    @Override
    public String getName() {
        return "DeleteDocument("+documentId+")";
    }

    @Override
    public void onSuccessAfterLog(Void o) {
        gui.pageHandler.refresh();
    }
        
    public DeleteDocument(long id) {
        super();
        
        documentId = id;
        
        enqueue();
    }

    @Override protected void call() {
        filmTitService.deleteDocument(gui.getSessionID(), documentId, this);
    }

}


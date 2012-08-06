package cz.filmtit.client.callables;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;

import cz.filmtit.client.*;
import cz.filmtit.client.pages.TranslationWorkspace;

import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.core.client.*;
import cz.filmtit.share.*;
import java.util.*;

public class LoadDocumentFromDB extends cz.filmtit.client.Callable<Document> {
    long documentId;
    
    @Override
    public String getName() {
        return "LoadDocumentFromDB("+documentId+")";
    }

    @Override
    public void onSuccessAfterLog(final Document doc) {
        
        // prepare empty TranslationWorkspace
        String moviePath = null; //TODO: player
        final TranslationWorkspace workspace = new TranslationWorkspace(doc, moviePath);
        
        // prepare the TranslationResults
        final List<TranslationResult> results  = doc.getSortedTranslationResults();
        gui.log("There are " + results.size() + " TranslationResults in the document");
        //int i = 0;
        //for (TranslationResult t: results) {t.getSourceChunk().setIndex(i);}
        
        // show the TranslationResults
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    public void execute() {
                        workspace.processTranslationResultList(results);
                    }
                });

    }
        
    public LoadDocumentFromDB(long id) {
        super();
        
        documentId = id;
        
        enqueue();
    }

    @Override
    public void call() {
        filmTitService.loadDocument(gui.getSessionID(), documentId, this);
    }

}


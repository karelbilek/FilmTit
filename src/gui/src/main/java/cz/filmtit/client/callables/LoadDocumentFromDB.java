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


import cz.filmtit.client.*;
import cz.filmtit.client.PageHandler.Page;
import cz.filmtit.client.pages.TranslationWorkspace;
import cz.filmtit.client.pages.TranslationWorkspace.DocumentOrigin;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.client.*;
import cz.filmtit.share.*;
import cz.filmtit.share.exceptions.InvalidDocumentIdException;
import java.util.*;

/**
 * Loads the document with the given documentID, with source chunks but without
 * translation suggestions (these have to be explicitly requested by
 * getTranslationResults). Shows the document in Translation Workspace after
 * successfully loading it.
 *
 * @author rur
 *
 */
public class LoadDocumentFromDB extends cz.filmtit.client.Callable<Document> {

    private long documentId;

    @Override
    public String getName() {
        return getNameWithParameters(documentId);
    }

    @Override
    public void onSuccessAfterLog(final Document doc) {

        // prepare empty TranslationWorkspace
        String moviePath = doc.getMoviePath();
        final TranslationWorkspace workspace = new TranslationWorkspace(doc, DocumentOrigin.FROM_DB);

        // prepare the TranslationResults
        final List<TranslationResult> results = doc.getSortedTranslationResults();
        Gui.log("There are " + results.size() + " TranslationResults in the document");
        //int i = 0;
        //for (TranslationResult t: results) {t.getSourceChunk().setIndex(i);}

        // show the TranslationResults
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            public void execute() {
                workspace.processTranslationResultList(results);
            }
        });

    }

    @Override
    public void onFailureAfterLog(Throwable returned) {
        if (returned instanceof InvalidDocumentIdException) {
            // ignore this one
            Gui.getPageHandler().loadPage(Page.UserPage);
        } else {
            super.onFailure(returned);
        }
    }

    @Override
    protected void onFinalError(String message) {
        Gui.getPageHandler().loadPage(Page.UserPage);
        super.onFinalError(message);
    }

    /**
     * Loads the document with the given documentID, with source chunks but
     * without translation suggestions (these have to be explicitly requested by
     * getTranslationResults). Shows the document in Translation Workspace after
     * successfully loading it.
     */
    public LoadDocumentFromDB(long documentID) {
        super();

        this.documentId = documentID;

        callTimeOut *= 2;

        enqueue();
    }

    @Override
    protected void call() {
        filmTitService.loadDocument(Gui.getSessionID(), documentId, this);
    }

}

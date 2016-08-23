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

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import cz.filmtit.client.Callable;
import cz.filmtit.client.Gui;
import cz.filmtit.client.ReceivesSelectSource;
import cz.filmtit.client.dialogs.Dialog;
import cz.filmtit.client.dialogs.MediaSelector;
import cz.filmtit.client.pages.DocumentCreator;
import cz.filmtit.client.pages.TranslationWorkspace;
import cz.filmtit.client.pages.TranslationWorkspace.DocumentOrigin;
import cz.filmtit.share.DocumentResponse;
import cz.filmtit.share.MediaSource;
import java.util.List;

/**
 * Creates the document (without source chunks, which have to be added by
 * calling SaveSourceChunks). On return creates a Translation Workspace where it
 * starts parsing the subtitle text and lets the user select the media source
 * opening a MediaSelector.
 *
 * @author rur
 *
 */
public class CreateDocument extends Callable<DocumentResponse> implements ReceivesSelectSource {

    // parameters
    private String documentTitle;
    private String movieTitle;
    private String language;
    private String subtext;
    private String subformat;
    private String moviePath;

    // results to store before MediaSelector returns
    private Dialog mediaSelector;
    private long documentId;
    private TranslationWorkspace workspace;
    private DocumentCreator documentCreator;

    @Override
    public String getName() {
        return getNameWithParameters(documentTitle, movieTitle, language, subtext.length(), subformat, moviePath);
    }

    @Override
    public void onSuccessAfterLog(DocumentResponse result) {

        workspace = new TranslationWorkspace(result.document, DocumentOrigin.NEW);
        documentCreator.reactivate();
        documentId = result.document.getId();
        mediaSelector = new MediaSelector(result.mediaSourceSuggestions, this);

        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            public void execute() {
                workspace.processText(subtext, subformat, CreateDocument.this);
            }
        });
    }

    @Override
    protected void onFinalError(String message) {
        // TODO: keep the document title
        Gui.getPageHandler().refresh();
        super.onFinalError(message);
    }

    @Override
    protected void onTimedOutReturnAfterLog(Object returned) {
        if (returned instanceof DocumentResponse) {
            new DeleteDocumentSilently(((DocumentResponse) returned).document.getId());
        }
    }

    /**
     * Called when MediaSelector returns
     */
    public void selectSource(MediaSource selectedMediaSource) {
        new SelectSource(documentId, selectedMediaSource, workspace);
        mediaSelector = null;
    }

    /**
     * Called from the workspace if parsing of the subtitle file fails.
     */
    public void hideMediaSelector() {
        if (mediaSelector != null) {
            mediaSelector.close();
        }
    }

    /**
     * Creates the document (without source chunks, which have to be added by
     * calling SaveSourceChunks). On return creates a Translation Workspace
     * where it starts parsing the subtitle text and lets the user select the
     * media source opening a MediaSelector.
     *
     * @param documentCreator the document creator used to create the document
     */
    public CreateDocument(String documentTitle, String movieTitle, String language,
            String subtext, String subformat, String moviePath, DocumentCreator documentCreator) {
        super();

        this.documentTitle = documentTitle;
        this.movieTitle = movieTitle;
        this.language = language;
        this.subtext = subtext;
        this.subformat = subformat;
        this.moviePath = moviePath;
        this.documentCreator = documentCreator;

        enqueue();
    }

    @Override
    protected void call() {
        Gui.log("Creating document " + documentTitle + "; its language is " + language);
        filmTitService.createNewDocument(Gui.getSessionID(), documentTitle, movieTitle, language, moviePath, this);
    }

}

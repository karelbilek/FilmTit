package cz.filmtit.client.callables;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.DialogBox;
import cz.filmtit.client.Callable;
import cz.filmtit.client.FilmTitServiceHandler;
import cz.filmtit.client.Gui;
import cz.filmtit.client.ReceivesSelectSource;
import cz.filmtit.client.PageHandler.Page;
import cz.filmtit.client.dialogs.MediaSelector;
import cz.filmtit.client.pages.TranslationWorkspace;
import cz.filmtit.client.pages.TranslationWorkspace.DocumentOrigin;
import cz.filmtit.share.DocumentResponse;
import cz.filmtit.share.MediaSource;

public class CreateDocument extends Callable<DocumentResponse> implements ReceivesSelectSource {

		// parameters
		String documentTitle;
        String movieTitle;
		String language;
		String subtext;
        String subformat;
		String moviePath;	
		
		// results to store before MediaSelector returns
		long documentId;
		TranslationWorkspace workspace;
		
        @Override
        public String getName() {
        	return getNameWithParameters(documentTitle, movieTitle, language, subtext.length(), subformat, moviePath);
        }

		@Override	
        public void onSuccessAfterLog(DocumentResponse result) {

            workspace = new TranslationWorkspace(result.document, moviePath, DocumentOrigin.NEW);
            documentId = result.document.getId();
            
            new MediaSelector(result.mediaSourceSuggestions, this);
            
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                public void execute() {
                    workspace.processText(subtext, subformat);
                }
            });
        }
		
		@Override
		protected void onFinalError(String message) {
			// TODO: keep the document title
			Gui.getPageHandler().refresh();
			super.onFinalError(message);
		}
		
		public void selectSource(MediaSource selectedMediaSource) {
            FilmTitServiceHandler.selectSource(documentId, selectedMediaSource, workspace);
		}
       
		
		// constructor
		public CreateDocument(String documentTitle, String movieTitle, String language,
				String subtext, String subformat, String moviePath) {
			super();
			
			this.documentTitle = documentTitle;
            this.movieTitle = movieTitle;
			this.language = language;
			this.subtext = subtext;
			this.subformat = subformat;
			this.moviePath = moviePath;
			
			enqueue();
		}

		@Override protected void call() {
			Gui.log("Creating document " + documentTitle + "; its language is " + language);
			filmTitService.createNewDocument(Gui.getSessionID(), documentTitle, movieTitle, language, this);
		}
	}


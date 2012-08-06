package cz.filmtit.client.callables;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.DialogBox;
import cz.filmtit.client.Callable;
import cz.filmtit.client.FilmTitServiceHandler;
import cz.filmtit.client.PageHandler.Page;
import cz.filmtit.client.dialogs.MediaSelector;
import cz.filmtit.client.pages.TranslationWorkspace;
import cz.filmtit.share.DocumentResponse;
import cz.filmtit.share.MediaSource;

public class CreateDocument extends Callable<DocumentResponse> {

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
		
        FilmTitServiceHandler handler;

        @Override
        public String getName() {
            return "CreateDocument("+documentTitle+","+movieTitle+","+
                        language+","+subtext+","+subformat+","+moviePath+")";
        }

		@Override	
        public void onSuccessAfterLog(DocumentResponse result) {

            gui.getPageHandler().setPageUrl(Page.TranslationWorkspace);				
            workspace = new TranslationWorkspace(result.document, moviePath);
            documentId = result.document.getId();
            
            new MediaSelector(result.mediaSourceSuggestions, this);
            
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                public void execute() {
                    workspace.processText(subtext, subformat);
                }
            });
        }
		
		/**
		 * Called by MediaSelector when MediaSource is selected
		 * @param documentId
		 * @param selectedMediaSource
		 */
		public void selectSource(MediaSource selectedMediaSource) {
            handler.selectSource(documentId, selectedMediaSource, workspace);
		}
       
		
		// constructor
		public CreateDocument(String documentTitle, String movieTitle, String language,
				String subtext, String subformat, String moviePath, FilmTitServiceHandler handler) {
			super();
			
            this.handler = handler;
			this.documentTitle = documentTitle;
            this.movieTitle = movieTitle;
			this.language = language;
			this.subtext = subtext;
			this.subformat = subformat;
			this.moviePath = moviePath;
			
			enqueue();
		}

		@Override protected void call() {
			gui.log("Creating document " + documentTitle + "; its language is " + language);
			filmTitService.createNewDocument(gui.getSessionID(), documentTitle, movieTitle, language, this);
		}
	}


package cz.filmtit.client.callables;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.DialogBox;
import cz.filmtit.client.Callable;
import cz.filmtit.client.FilmTitServiceHandler;
import cz.filmtit.client.MediaSelector;
import cz.filmtit.client.PageHandler.Page;
import cz.filmtit.client.TranslationWorkspace;
import cz.filmtit.share.DocumentResponse;

public class CreateDocument extends Callable<DocumentResponse> {

		// parameters
		String documentTitle;
        String movieTitle;
		String language;
		String subtext;
        String subformat;
		String moviePath;	
		
        FilmTitServiceHandler handler;

        @Override
        public String getName() {
            return "CreateDocument("+documentTitle+","+movieTitle+","+
                        language+","+subtext+","+subformat+","+moviePath+")";
        }

		@Override	
        public void onSuccessAfterLog(final DocumentResponse result) {

            gui.getPageHandler().setPageUrl(Page.TranslationWorkspace);				
            final TranslationWorkspace workspace = new TranslationWorkspace(result.document, moviePath);
            
            final DialogBox dialogBox = new DialogBox(false);
            final MediaSelector mediaSelector = new MediaSelector(result.mediaSourceSuggestions);
            mediaSelector.addSubmitButtonHandler( new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    dialogBox.hide();
                    handler.selectSource(result.document.getId(), mediaSelector.getSelected());
                    gui.log("document created successfully.");

                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                        public void execute() {
                            workspace.processText(subtext, subformat);
                        }
                    });
                }
            } );
            dialogBox.setWidget(mediaSelector);
            dialogBox.setGlassEnabled(true);
            dialogBox.center();
            dialogBox.setPopupPosition(dialogBox.getPopupLeft(), 100);
            
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

		@Override
		public void call() {
			gui.log("Creating document " + documentTitle + "; its language is " + language);
			filmTitService.createNewDocument(gui.getSessionID(), documentTitle, movieTitle, language, this);
		}
	}


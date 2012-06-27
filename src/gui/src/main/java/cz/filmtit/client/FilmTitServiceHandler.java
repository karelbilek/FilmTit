package cz.filmtit.client;

import com.github.gwtbootstrap.client.ui.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import cz.filmtit.share.*;
import cz.filmtit.share.exceptions.InvalidSessionIdException;

import java.util.List;

public class FilmTitServiceHandler {
	// FilmTitServiceAsync should be created automatically by Maven
	// from FilmTitService during compilation (or generated as a QuickFix in Eclipse)
	private FilmTitServiceAsync filmTitSvc;
	private Gui gui;

    int windowsDisplayed = 0;
    public  void displayWindow(String message) {
        if (windowsDisplayed < 10) {
            windowsDisplayed++;
            Window.alert(message);
            if (windowsDisplayed==10) {
                Window.alert("Last window displayed.");
            }
        } else {
      //      gui.log("ERROR - message");
        }
    }
	
	public FilmTitServiceHandler(Gui gui) {
		filmTitSvc = GWT.create(FilmTitService.class);
		this.gui = gui;
	}
	
	public void createDocument(String movieTitle, String year, String language, final String subtext) {

		AsyncCallback<DocumentResponse> callback = new AsyncCallback<DocumentResponse>() {
			
			public void onSuccess(final DocumentResponse result) {
				gui.log("DocumentResponse arrived, showing dialog with MediaSource suggestions...");
                gui.setCurrentDocument(result.document);

                gui.document_created();
                
                final DialogBox dialogBox = new DialogBox(false);
                final MediaSelector mediaSelector = new MediaSelector(result.mediaSourceSuggestions);
                mediaSelector.submitButton.addClickHandler( new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        dialogBox.hide();
                        selectSource(result.document.getId(), mediaSelector.getSelected());
                        gui.log("document created successfully.");

                        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                            public void execute() {
                                gui.processText(subtext);
                            }
                        });
                    }
                } );
                dialogBox.setWidget(mediaSelector);
                dialogBox.setGlassEnabled(true);
                dialogBox.center();
            }
			
			public void onFailure(Throwable caught) {
				if (caught.getClass().equals(InvalidSessionIdException.class)) {
					gui.please_relog_in();
					// TODO: store user input to be used when user logs in
				} else {
					// TODO: repeat sending a few times, then ask user
					displayWindow(caught.getLocalizedMessage());
					gui.log("failure on creating document!");
				}
			}

		};
		
		filmTitSvc.createNewDocument(gui.sessionID, movieTitle, year, language, callback);
	}
	
	public void getTranslationResults(List<TimedChunk> chunks) {
		
		AsyncCallback<List<TranslationResult>> callback = new AsyncCallback<List<TranslationResult>>() {
			
			public void onSuccess(List<TranslationResult> newresults) {
				gui.log("successfully received " + newresults.size() + " TranslationResults!");				
				
				// add to trlist to the correct position:
				List<TranslationResult> translist = gui.getCurrentDocument().translationResults;
			
                for (TranslationResult newresult:newresults){

                    int index = newresult.getSourceChunk().getIndex();
                    translist.set(index, newresult);
                    
                    gui.showResult(newresult, index);
                }
			}
			
			public void onFailure(Throwable caught) {
				if (caught.getClass().equals(InvalidSessionIdException.class)) {
					gui.please_relog_in();
					// TODO: store user input to be used when user logs in
				} else {
					// TODO: repeat sending a few times, then ask user
					displayWindow(caught.getLocalizedMessage());
					gui.log("failure on receiving some chunk!");
					gui.log(caught.toString());				
					StackTraceElement[] st = caught.getStackTrace();
					for (StackTraceElement stackTraceElement : st) {
						gui.log(stackTraceElement.toString());
					}
				}
			}
		};
		
		filmTitSvc.getTranslationResults(gui.sessionID, chunks, callback);
	}
	
	public void setUserTranslation(int chunkId, long documentId, String userTranslation, long chosenTranslationPair) {
		
		AsyncCallback<Void> callback = new AsyncCallback<Void>() {
			
			public void onSuccess(Void o) {
				gui.log("setUserTranslation() succeeded");
			}
			
			public void onFailure(Throwable caught) {
				if (caught.getClass().equals(InvalidSessionIdException.class)) {
					gui.please_relog_in();
					// TODO: store user input to be used when user logs in
				} else {
					gui.log("ERROR: setUserTranslation() didn't succeed!");
					// TODO: repeat sending a few times, then ask user
				}
			}
		};
		
		filmTitSvc.setUserTranslation(gui.sessionID, chunkId, documentId, userTranslation, chosenTranslationPair, callback);
	}



    public void selectSource(long documentID, MediaSource selectedMediaSource) {
        AsyncCallback<Void> callback = new AsyncCallback<Void>() {

            public void onSuccess(Void o) {
                gui.log("selectSource() succeeded");
            }

            public void onFailure(Throwable caught) {
				if (caught.getClass().equals(InvalidSessionIdException.class)) {
					gui.please_relog_in();
					// TODO: store user input to be used when user logs in
				} else {
	                gui.log("ERROR: selectSource() didn't succeed!");
	                // TODO: repeat sending a few times, then ask user
				}
            }
        };

        filmTitSvc.selectSource( gui.sessionID, documentID, selectedMediaSource, callback);
    }

    public void simple_login(final String username, String password) {
        AsyncCallback<String> callback = new AsyncCallback<String>() {

            public void onSuccess(String SessionID) {
            	if (SessionID != null) {
	                gui.log("logged in as " + username + " with session id " + SessionID);
	                gui.sessionID = SessionID;
	                gui.logged_in(username);
            	} else {
                    gui.log("ERROR: simple login didn't succeed - incorrect username or password.");
                    displayWindow("ERROR: simple login didn't succeed - incorrect username or password.");
            		gui.showLoginDialog();
            	}
            }

            public void onFailure(Throwable caught) {
                gui.log("ERROR: simple login didn't succeed!");
            }
        };

        filmTitSvc.simple_login(username, password, callback);
    }

    public void logout() {
        AsyncCallback<Void> callback = new AsyncCallback<Void>() {

            public void onSuccess(Void o) {
                gui.log("logged out");
                gui.sessionID = null;
                gui.logged_out();
            }

            public void onFailure(Throwable caught) {
                gui.log("ERROR: logout didn't succeed!");
            }
        };

        filmTitSvc.logout(gui.sessionID, callback);
    }

    
	public void getAuthenticationURL(AuthenticationServiceType serviceType, final DialogBox loginDialogBox) {
		
		final long authID = Random.nextInt();

		AsyncCallback<String> callback = new AsyncCallback<String>() {
			
			public void onSuccess(final String url) {
				gui.log("Authentication URL arrived: " + url);

				loginDialogBox.hide();
				
				Window.open(url, "AuthenticationWindow", "width=200,height=200");

				// open a dialog saying that we are waiting for the user to authenticate
                final DialogBox dialogBox = new DialogBox(false);
                final SessionIDPollingDialog sessionIDPollingDialog = new SessionIDPollingDialog(authID);
                sessionIDPollingDialog.btnCancel.addClickHandler( new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
                        gui.log("SessionIDPollingDialog closed by user hitting Cancel button");
                        dialogBox.hide();
					}
				});
                dialogBox.setGlassEnabled(true);
                dialogBox.center();
            }
			
			public void onFailure(Throwable caught) {
				if (caught.getClass().equals(InvalidSessionIdException.class)) {
					gui.please_relog_in();
					// TODO: store user input to be used when user logs in
				} else {
					// TODO: repeat sending a few times, then ask user
					// displayWindow(caught.getLocalizedMessage());
					gui.log("failure on requesting authentication url!");
				}
			}

		};
		
		filmTitSvc.getAuthenticationURL(authID, serviceType, callback);
	}
	
	public void validateAuthentication (String responseURL, long authID) {

		// create callback
		AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
			
			public void onSuccess(Boolean result) {
				// TODO say OK and close the window
			}
			
			public void onFailure(Throwable caught) {
				// TODO say error
			}
		};
		
		// RPC
		filmTitSvc.validateAuthentication(authID, responseURL, callback);		
	}
	    
}

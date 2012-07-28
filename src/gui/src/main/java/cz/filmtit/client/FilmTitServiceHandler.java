package cz.filmtit.client;

import com.github.gwtbootstrap.client.ui.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.VerticalPanel;
import cz.filmtit.share.*;
import cz.filmtit.share.exceptions.InvalidSessionIdException;

import java.util.List;

public class FilmTitServiceHandler {
	// FilmTitServiceAsync should be created automatically by Maven
	// from FilmTitService during compilation (or generated as a QuickFix in Eclipse)
	private FilmTitServiceAsync filmTitSvc;
	/**
	 * reference to the gui for access to its protected and public members
	 */
	private Gui gui;
	/**
	 * indicates whether polling for session ID is in progress
	 */
	private boolean sessionIDPolling = false;
	/**
	 * dialog polling for session ID
	 */
	private DialogBox sessionIDPollingDialogBox;
	/**
	 * temporary ID for authentication
	 */
	private int authID;

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
	
	public void createDocument(String movieTitle, String year, String language, final String subtext, final String moviePath) {

		AsyncCallback<DocumentResponse> callback = new AsyncCallback<DocumentResponse>() {
			
			public void onSuccess(final DocumentResponse result) {
				gui.log("DocumentResponse arrived, showing dialog with MediaSource suggestions...");
                gui.setCurrentDocument(result.document);

                gui.document_created(moviePath);
                
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
		
		gui.log("Creating document " + movieTitle + " (" + year + "); its language is " + language);
		filmTitSvc.createNewDocument(gui.getSessionID(), movieTitle, year, language, callback);
	}
	
	public void getTranslationResults(List<TimedChunk> chunks, final Gui.SendChunksCommand command) {
		
		AsyncCallback<List<TranslationResult>> callback = new AsyncCallback<List<TranslationResult>>() {
			
			public void onSuccess(List<TranslationResult> newresults) {
				gui.log("successfully received " + newresults.size() + " TranslationResults!");				
				
				// add to trlist to the correct position:
				List<TranslationResult> translist = gui.getCurrentDocument().translationResults;
			
                for (TranslationResult newresult:newresults){

                    int index = newresult.getSourceChunk().getIndex();
                    translist.set(index, newresult);
                    
                    gui.getTranslationWorkspace().showResult(newresult, index);
                    command.execute();
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
		
		filmTitSvc.getTranslationResults(gui.getSessionID(), chunks, callback);
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
		
		filmTitSvc.setUserTranslation(gui.getSessionID(), chunkId, documentId, userTranslation, chosenTranslationPair, callback);
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

        filmTitSvc.selectSource( gui.getSessionID(), documentID, selectedMediaSource, callback);
    }

    // TODO will probably return the whole Session object - now returns username or null
    public void checkSessionID() {
    	
    	final String sessionID = gui.getSessionID();
    	
    	if (sessionID == null) {
    		return;
    	}
        else {
          //  gui.log("logged in as -=unknown=- with session id " + sessionID);
          //  gui.logged_in("-=unknown=-");

            AsyncCallback<String> callback = new AsyncCallback<String>() {

                public void onSuccess(String username) {
                    if (username != null && username!="") {
                        gui.log("logged in as " + username + " with session id " + sessionID);
                        gui.logged_in(username);
                    } else {
                        gui.log("Warning: sessionID invalid.");
                        gui.setSessionID(null);
                        // gui.showLoginDialog();
                    }
                }

                public void onFailure(Throwable caught) {
                    gui.log("ERROR: sessionID check didn't succeed!");
                }
            };

            // TODO call something
            filmTitSvc.checkSessionID(sessionID, callback);
           return;
        }

        /*
        AsyncCallback<String> callback = new AsyncCallback<String>() {

            public void onSuccess(String username) {
            	if (username != null) {
	                gui.log("logged in as " + username + " with session id " + sessionID);
	                gui.logged_in(username);
            	} else {
                    gui.log("Warning: sessionID invalid.");
            		gui.setSessionID(null);
                    // gui.showLoginDialog();
            	}
            }

            public void onFailure(Throwable caught) {
                gui.log("ERROR: sessionID check didn't succeed!");
            }
        };

    	// TODO call something
        
        // filmTitSvc.checkSessionID(sessionID, callback);
        */
    }

    public void registerUser(final String username, final String password, final String email, final DialogBox registrationForm) {
        AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

            public void onSuccess(Boolean result) {
            	if (result) {
            		registrationForm.hide();
	                gui.log("registered as " + username);
                    simple_login(username, password);
                    displayWindow("You successfully registered with the username '" + username + "'!");
            	} else {
            		// TODO: bool means unavailable username, right? Or are there other reasons for failing?
                    gui.log("ERROR: registration didn't succeed, username already taken.");
                    displayWindow("The username '" + username + "' is not available. Please choose a different username.");
            		gui.showRegistrationForm();
            	}
            }

            public void onFailure(Throwable caught) {
                gui.log("ERROR: registration didn't succeed!");
                displayWindow("ERROR: registration didn't succeed!");
            }
        };

    	String openid = null;
        filmTitSvc.registration(username, password, email, openid, callback);
    }

    public void simple_login(final String username, final String password) {
        AsyncCallback<String> callback = new AsyncCallback<String>() {

            public void onSuccess(String SessionID) {
            	if (SessionID != null && SessionID!="") {
	                gui.log("logged in as " + username + " with session id " + SessionID);
	                gui.setSessionID(SessionID);
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
                gui.setSessionID(null);
                gui.logged_out();
            }

            public void onFailure(Throwable caught) {
                gui.log("ERROR: logout didn't succeed!");
            }
        };

        filmTitSvc.logout(gui.getSessionID(), callback);
    }

    
	public void getAuthenticationURL(AuthenticationServiceType serviceType, final DialogBox loginDialogBox) {
		
		authID = Random.nextInt();

		AsyncCallback<String> callback = new AsyncCallback<String>() {
			
			public void onSuccess(final String url) {
				gui.log("Authentication URL arrived: " + url);

				loginDialogBox.hide();
				
				// open a dialog saying that we are waiting for the user to authenticate
                final DialogBox dialogBox = new DialogBox(false);
                final SessionIDPollingDialog sessionIDPollingDialog = new SessionIDPollingDialog();
                sessionIDPollingDialog.btnCancel.addClickHandler( new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						sessionIDPolling = false;
                        gui.log("SessionIDPollingDialog closed by user hitting Cancel button");
                        dialogBox.hide();
					}
				});
                dialogBox.setWidget(sessionIDPollingDialog);
                dialogBox.setGlassEnabled(true);
                dialogBox.center();
                
                start_sessionIDPolling(dialogBox);
                
                // open the authenticationwindow
				Window.open(url, "AuthenticationWindow", "width=400,height=500");
            }
			
			private void start_sessionIDPolling(DialogBox dialogBox) {
				sessionIDPolling = true;
				sessionIDPollingDialogBox = dialogBox;
				
				Scheduler.RepeatingCommand poller = new RepeatingCommand() {
					
					@Override
					public boolean execute() {
						if (sessionIDPolling) {
							getSessionID();
							return true;
						} else {
							return false;
						}
					}
				};
				
				Scheduler.get().scheduleFixedDelay(poller, 2000);
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
	
	public void getSessionID () {

		// create callback
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			
			public void onSuccess(String result) {
				if (result != null) {
					// stop polling
					sessionIDPolling = false;
					sessionIDPollingDialogBox.hide();
					// we now have a session ID
					gui.setSessionID(result);
					gui.logged_in("");
				}
				// else continue polling
			}
			
			public void onFailure(Throwable caught) {
				if(sessionIDPolling) {
					// stop polling
					sessionIDPolling = false;
					sessionIDPollingDialogBox.hide();
					// say error
					displayWindow(caught.getLocalizedMessage());
					gui.log("failure on requesting session ID!");					
				}
			}
		};
		
		// RPC
		if (sessionIDPolling) {
			gui.log("asking for session ID with authID=" + authID);
			filmTitSvc.getSessionID(authID, callback);			
		}
	}
	    
	public void validateAuthentication (String responseURL, long authID, final AuthenticationValidationWindow authenticationValidationWindow) {

		// create callback
		AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
			
			public void onSuccess(Boolean result) {
				// TODO say OK and close the window
				if (result) {
					authenticationValidationWindow.paraValidation.setText("Logged in successfully! You can now close this window.");
					Window.alert("Logged in successfully! You can now close this window.");					
				}
				else {
					authenticationValidationWindow.paraValidation.setText("Not logged in! Authentication validation failed.");
					Window.alert("Not logged in! Authentication validation failed.");
				}
			}
			
			public void onFailure(Throwable caught) {
				// TODO say error
				authenticationValidationWindow.paraValidation.setText("Not logged in! Authentication validation failed: " + caught.getLocalizedMessage());
				Window.alert(caught.getLocalizedMessage());
			}
		};
		
		// RPC
		filmTitSvc.validateAuthentication(authID, responseURL, callback);		
	}


    public void getListOfDocuments(final FlexTable doctable) {

        // create callback
        AsyncCallback<List<Document>> callback = new AsyncCallback<List<Document>>() {

            @Override
            public void onSuccess(List<Document> result) {
                gui.log("received " + result.size() + " documents");

                if (result.size() == 0) {
                    doctable.setWidget(0, 0, new Label("(you have no documents)"));
                }
                else {
                    int row = 0;
                    for (Document doc : result) {
                        doctable.setWidget(row++, 0, new Label(doc.getMovie().getTitle() + " (" + doc.getMovie().getYear() + ")") );
                    }
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                // TODO: repeat sending a few times, then ask user
                gui.log("failure on getting list of documents!");
            }

        };

        // RPC
        filmTitSvc.getListOfDocuments(gui.getSessionID(), callback);
    }
	    
}

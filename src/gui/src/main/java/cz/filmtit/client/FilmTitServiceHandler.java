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

import cz.filmtit.client.Gui.SendChunksCommand;
import cz.filmtit.share.*;
import cz.filmtit.share.exceptions.InvalidSessionIdException;

import java.util.Map;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.SortedMap;

public class FilmTitServiceHandler {
	// FilmTitServiceAsync should be created automatically by Maven
	// from FilmTitService during compilation (or generated as a QuickFix in Eclipse)
	private FilmTitServiceAsync filmTitService;
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
    
    /**
     * display a widow with an error message
     * unless maximum nimber of error messages has been reached
     * @param string
     */
    public void displayWindow(String message) {
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
		// TODO: FilmTitServiceHandler fields should eventually become obsolete in favor of Callable
		filmTitService = GWT.create(FilmTitService.class);
		this.gui = gui;
		
		Callable.filmTitService = filmTitService;
		Callable.gui = gui;
		Callable.filmTitServiceHandler = this;
	}

    public void loadDocumentFromDB(Document document) {
        new LoadDocumentFromDB(document.getId());
    }

    public class LoadDocumentFromDB extends Callable {
        long documentId;
        

        AsyncCallback<Document> callback = new AsyncCallback<Document>() {
            public void onSuccess(final Document doc) {
                gui.document_created(null);//TODO: player
                gui.setCurrentDocument(doc);

                final List<TranslationResult> results  = doc.getSortedTranslationResults();
                int i = 0;
                //for (TranslationResult t: results) {t.getSourceChunk().setIndex(i);}
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                            public void execute() {
                                gui.processTranslationResultList(results);
                            }
                        });

            }
            
            
            public void onFailure(Throwable caught) {
				if (caught.getClass().equals(InvalidSessionIdException.class)) {
					gui.please_relog_in();
				} else {
					displayWindow(caught.getLocalizedMessage());
					gui.log("failure on loading document!");
				}
			}
        };

        // constructor
		public LoadDocumentFromDB(long id) {
			super();
			
			documentId = id;
			
			enqueue();
		}

		@Override
		public void call() {
			filmTitService.loadDocument(gui.getSessionID(), documentId, callback);
		}

    }

	
	public void createDocument(String movieTitle, String year, String language, final String subtext, final String moviePath) {
		new CreateDocument(movieTitle, year, language, subtext, moviePath);
	}
	
	public class CreateDocument extends Callable {

		// parameters
		String movieTitle;
		String year;
		String language;
		String subtext;
		String moviePath;
		
		// callback
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
		
		// constructor
		public CreateDocument(String movieTitle, String year, String language,
				String subtext, String moviePath) {
			super();
			
			this.movieTitle = movieTitle;
			this.year = year;
			this.language = language;
			this.subtext = subtext;
			this.moviePath = moviePath;
			
			enqueue();
		}

		@Override
		public void call() {
			gui.log("Creating document " + movieTitle + " (" + year + "); its language is " + language);
			filmTitService.createNewDocument(gui.getSessionID(), movieTitle, year, language, callback);
		}
	}
	
	public void getTranslationResults(List<TimedChunk> chunks, Gui.SendChunksCommand command) {
		new GetTranslationResults(chunks, command);
	}

	public class GetTranslationResults extends Callable {
		
		// parameters
		List<TimedChunk> chunks;
		Gui.SendChunksCommand command;
		
		// callback
		AsyncCallback<List<TranslationResult>> callback = new AsyncCallback<List<TranslationResult>>() {
			
			public void onSuccess(List<TranslationResult> newresults) {
				gui.log("successfully received " + newresults.size() + " TranslationResults!");				
				
				// add to trlist to the correct position:
				//Map<ChunkIndex, TranslationResult> translist = gui.getCurrentDocument().translationResults;
			
                for (TranslationResult newresult:newresults){

                    //int index = newresult.getSourceChunk().getIndex();
                    ChunkIndex poi = newresult.getSourceChunk().getChunkIndex();
                   

                    //not sure if this is needed
                    //translist.put(poi, newresult);
                    
                    gui.getTranslationWorkspace().showResult(newresult);
                }
                command.execute();
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
		
		// constructor
		public GetTranslationResults(List<TimedChunk> chunks,
				SendChunksCommand command) {
			super();
			
			this.chunks = chunks;



			this.command = command;
			
			enqueue();
		}

		@Override
		public void call() {
            filmTitService.getTranslationResults(gui.getSessionID(), chunks, callback);
		}
	}
	
	public void setUserTranslation(ChunkIndex chunkIndex, long documentId, String userTranslation, long chosenTranslationPair) {
		new SetUserTranslation(chunkIndex, documentId, userTranslation, chosenTranslationPair);
	}

	public class SetUserTranslation extends Callable {
		
		// parameters
		ChunkIndex chunkIndex;
		long documentId;
		String userTranslation;
		long chosenTranslationPair;

		// callback
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
		

		// constructor
		public SetUserTranslation(ChunkIndex chunkIndex, long documentId,
				String userTranslation, long chosenTranslationPair) {
						
			super();
			
			this.chunkIndex = chunkIndex;
			this.documentId = documentId;
			this.userTranslation = userTranslation;
			this.chosenTranslationPair = chosenTranslationPair;
			
	        enqueue();
		}

		@Override
		public void call() {
			filmTitService.setUserTranslation(gui.getSessionID(), chunkIndex,
					documentId, userTranslation, chosenTranslationPair,
					callback);
		}
	}

    public void selectSource(long documentID, MediaSource selectedMediaSource) {
    	new SelectSource(documentID, selectedMediaSource);
    }

    public class SelectSource extends Callable {
    	
    	// parameters
    	long documentID;	
    	MediaSource selectedMediaSource;
    	
    	// callback
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

		public SelectSource(long documentID, MediaSource selectedMediaSource) {
			super();
			this.documentID = documentID;
			this.selectedMediaSource = selectedMediaSource;
			enqueue();
		}

		@Override
		public void call() {
	        filmTitService.selectSource( gui.getSessionID(), documentID, selectedMediaSource, callback);
		}
	}

    public void checkSessionID() {
    	new CheckSessionID();
    }
    
    // TODO will probably return the whole Session object - now returns username or null
    public class CheckSessionID extends Callable {
    	
    	// parameters
    	String sessionID;
    	
    	// callback
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
	
        // constructor
    	public CheckSessionID() {
    		sessionID = gui.getSessionID();
    		if (sessionID == null) {
        		return;
        	}
    		else {
            	enqueue();
            }
    	}
    	
		@Override
		public void call() {
            filmTitService.checkSessionID(sessionID, callback);
		}
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
                    //registrationForm.txtUsername.focus();
            	}
            }

			public void onFailure(Throwable caught) {
                gui.log("ERROR: registration didn't succeed!");
                displayWindow("ERROR: registration didn't succeed!");
            }
        };

    	String openid = null;
        filmTitService.registration(username, password, email, openid, callback);
    }

    public void simple_login(String username, String password) {
    	new SimpleLogin(username, password);
	}

    public class SimpleLogin extends Callable {
    	
    	// parameters
    	String username;
    	String password;
    	
    	// callback
		AsyncCallback<String> callback = new AsyncCallback<String>() {

            public void onSuccess(String SessionID) {
            	if (SessionID == null || SessionID.equals("")) {
            		gui.log("ERROR: simple login didn't succeed - incorrect username or password.");
            		displayWindow("ERROR: simple login didn't succeed - incorrect username or password.");
                    gui.showLoginDialog();
            	} else {
            		gui.log("logged in as " + username + " with session id " + SessionID);
            		gui.setSessionID(SessionID);
            		gui.logged_in(username);
            	}
            }

            public void onFailure(Throwable caught) {
            	gui.log("ERROR: simple login didn't succeed!");
            }
        };
		
        // constructor
        public SimpleLogin(String username, String password) {
			super();
			
			this.username = username;
			this.password = password;

	        enqueue();
		}

		@Override
		public void call() {
	        filmTitService.simple_login(username, password, callback);
		}
    }

    public void logout() {
        AsyncCallback<Void> callback = new AsyncCallback<Void>() {

            public void onSuccess(Void o) {
                gui.log("logged out");
                gui.setSessionID(null);
                gui.logged_out();
            }

            public void onFailure(Throwable caught) {
                if (caught.getClass().equals(InvalidSessionIdException.class)) {
                    gui.log("already logged out");
                    gui.setSessionID(null);
                    gui.logged_out();
                } else {
                    gui.log("ERROR: logout didn't succeed! Forcing local logout... " + caught.getLocalizedMessage());
                    gui.setSessionID(null);
                    gui.logged_out();
                }
            }
        };

        filmTitService.logout(gui.getSessionID(), callback);
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
		
		filmTitService.getAuthenticationURL(authID, serviceType, callback);
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
			filmTitService.getSessionID(authID, callback);			
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
		filmTitService.validateAuthentication(authID, responseURL, callback);		
	}


    public void getListOfDocuments(final UserPage userpage) {

        // create callback
        AsyncCallback<List<Document>> callback = new AsyncCallback<List<Document>>() {

            @Override
            public void onSuccess(List<Document> result) {
                gui.log("received " + result.size() + " documents");
                
                userpage.setDocuments(result);
                for (Document d:result) {
                    gui.log("GUI Dalsi document. Ma "+d.getTranslationResults().size()+" prfku.");
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                // TODO: repeat sending a few times, then ask user
                gui.log("failure on getting list of documents!");
            }

        };

        // RPC
        filmTitService.getListOfDocuments(gui.getSessionID(), callback);
    }
	    
}

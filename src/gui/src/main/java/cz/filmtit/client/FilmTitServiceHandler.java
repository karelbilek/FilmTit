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
import cz.filmtit.client.PageHandler.Page;
import cz.filmtit.share.*;
import cz.filmtit.share.exceptions.InvalidSessionIdException;

import java.util.Map;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.SortedMap;

public class FilmTitServiceHandler {
	
	public FilmTitServiceHandler(Gui gui) {
		
		// the async service
		Callable.filmTitService = GWT.create(FilmTitService.class);

		// direct access
		// to package-internal (and public) fields and methods
		// of the active Gui instance
		// is necessary to react to call results
		// (because the RPC calls are asynchronous)
		Callable.gui = gui;
	}

// disabled only to avoid duplicities, can be reenabled by uncommenting anytime if needed
//    public void loadDocumentFromDB(Document document) {
//        new LoadDocumentFromDB(document.getId());
//    }

    public void loadDocumentFromDB(long documentId) {
        new LoadDocumentFromDB(documentId);    		
    }

    public class LoadDocumentFromDB extends Callable {
        long documentId;
        

        AsyncCallback<Document> callback = new AsyncCallback<Document>() {
            public void onSuccess(final Document doc) {
                String moviePath = null; //TODO: player
                gui.workspace = new TranslationWorkspace(gui, moviePath);
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
            	gui.pageHandler.loadPage(Page.UserPage);
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

	
	public void createDocument(String documentTitle, String movieTitle, String language, String subtext, String subformat, String moviePath) {
		new CreateDocument(documentTitle, movieTitle, language, subtext, subformat, moviePath);
	}
	
	public class CreateDocument extends Callable {

		// parameters
		String documentTitle;
        String movieTitle;
		String language;
		String subtext;
        String subformat;
		String moviePath;	
		
		// callback
		AsyncCallback<DocumentResponse> callback = new AsyncCallback<DocumentResponse>() {
			
			public void onSuccess(final DocumentResponse result) {
				gui.log("DocumentResponse arrived, showing dialog with MediaSource suggestions...");
				gui.setCurrentDocument(result.document);

                gui.workspace = new TranslationWorkspace(gui, moviePath);
                
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
                            	gui.processText(subtext, subformat);
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

		@Override
		public void call() {
			gui.log("Creating document " + documentTitle + "; its language is " + language);
			filmTitService.createNewDocument(gui.getSessionID(), documentTitle, movieTitle, language, callback);
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

        // constructor
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
		if (Callable.gui.getSessionID() == null) {
			Callable.gui.logged_out();
    	}
		else {
	    	new CheckSessionID();
        }
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
                    gui.logged_out();
                }
            }

            public void onFailure(Throwable caught) {
                gui.log("ERROR: sessionID check didn't succeed!");
                gui.logged_out();
            }
        };
	
        // constructor
    	public CheckSessionID() {
    		super();
    		
    		sessionID = gui.getSessionID();
    		if (sessionID == null) {
                gui.logged_out();
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

    public void registerUser(String username, String password, String email, DialogBox registrationForm) {
    	new RegisterUser(username, password, email, registrationForm);
    }
    
    public class RegisterUser extends Callable {
    	
    	// parameters
    	String username;
    	String password;
    	String email;
    	DialogBox registrationForm;
    	String openid = null;
    	
    	// callback
        AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

            public void onSuccess(Boolean result) {
            	if (result) {
            		registrationForm.hide();
	                gui.log("registered as " + username);
                    simpleLogin(username, password);
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

        // constructor
		public RegisterUser(String username, String password, String email,
				DialogBox registrationForm) {
			super();
			
			this.username = username;
			this.password = password;
			this.email = email;
			this.registrationForm = registrationForm;
			
			enqueue();
		}

		@Override
		void call() {
	        filmTitService.registration(username, password, email, openid, callback);
		}
    }
    
    public void sendChangePasswordMail (String username, DialogBox dialogBox) {
    	new SendChangePasswordMail(username, dialogBox);
    }
    
    public class SendChangePasswordMail extends Callable {
    	
    	// parameters
    	String username;
    	DialogBox dialogBox;

    	// callback
        AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

            public void onSuccess(Boolean result) {
            	if (result) {
            		dialogBox.hide();
	                gui.log("successful sendChangePasswordMail for " + username);
                    displayWindow("A link to password change page has been sent to your e-mail address.");
            	} else {
            		// false = bad username or no email
                    gui.log("ERROR: sendChangePasswordMail didn't succeed, bad username or no email.");
                    displayWindow("There was an error sending password change email to you. " +
                    		"Either the username '" + username + "' is not registered " +
                    				"or there is no e-mail address associated with it. " +
                    				"Please check the username or register with a new one. " +
                    				"(You can also try to contact the administrators.)");
                    //dialogBox.txtUsername.focus();
            	}
            }

			public void onFailure(Throwable caught) {
                gui.log("ERROR: sendChangePasswordMail didn't succeed!");
                displayWindow("ERROR: sendChangePasswordMail didn't succeed!");
            }
        };
    	
    	// constructor
		public SendChangePasswordMail(String username, DialogBox dialogBox) {
			super();
			
			this.username = username;
			this.dialogBox = dialogBox;
			
			enqueue();
		}

		@Override
		void call() {
			filmTitService.sendChangePasswordMail(username, callback);
		}    	
    }

    /**
     * change password in case of forgotten password;
     * user chooses a new password,
     * user authentication is done by the token sent to user's email
     * @param username
     * @param password
     * @param token
     */
    public void changePassword(String username, String password, String token) {
    	new ChangePassword(username, password, token);
    }
    
    /**
     * change password in case of forgotten password;
     * user chooses a new password,
     * user authentication is done by the token sent to user's email
     */
    public class ChangePassword extends Callable {
    	
    	// parameters
    	String username;
    	String password;
    	String token;
    	
    	// callback
        AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

            public void onSuccess(Boolean result) {
            	if (result) {
	                gui.log("changed password for user " + username);
	                gui.pageHandler.loadBlankPage();
	                gui.pageHandler.setPageUrl(Page.UserPage);
                    simpleLogin(username, password);
                    displayWindow("You successfully changed the password for your username '" + username + "'!");
            	} else {
                    gui.log("ERROR: password change didn't succeed - token invalid");
                    gui.showLoginDialog(username);
                    displayWindow("ERROR: password change didn't succeed - the token is invalid, probably expired. " +
                    		"Please try requesting a new password change token" +
                    		"(by clicking the button labelled 'Forgotten password').");
            	}
            }

			public void onFailure(Throwable caught) {
                gui.log("ERROR: password change didn't succeed!");
                displayWindow("ERROR: password change didn't succeed!");
            }
        };

        // constructor
		public ChangePassword(String username, String password, String token) {
			super();
			
			this.username = username;
			this.password = password;
			this.token = token;
			
			enqueue();
		}

		@Override
		void call() {
	        filmTitService.changePassword(username, password, token, callback);
		}
    }

    public void simpleLogin(String username, String password) {
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
                    gui.showLoginDialog(username);
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
    	new Logout();
    }
    
    public class Logout extends Callable {
    	
    	// callback
        AsyncCallback<Void> callback = new AsyncCallback<Void>() {

            public void onSuccess(Void o) {
                gui.log("logged out");
                gui.logged_out();
            }

            public void onFailure(Throwable caught) {
                if (caught.getClass().equals(InvalidSessionIdException.class)) {
                    gui.log("already logged out");
                    gui.logged_out();
                } else {
                    gui.log("ERROR: logout didn't succeed! Forcing local logout... " + caught.getLocalizedMessage());
                    gui.logged_out();
                }
            }
        };
        
        // constructor
		public Logout() {
			super();			
			enqueue();
		}

		@Override
		void call() {
	        filmTitService.logout(gui.getSessionID(), callback);
		}
	}

	public void getAuthenticationURL(AuthenticationServiceType serviceType, DialogBox loginDialogBox) {
		new GetAuthenticationURL(serviceType, loginDialogBox);
	}
	
	public class GetAuthenticationURL extends Callable {
		
		// parameters
		AuthenticationServiceType serviceType;
		DialogBox loginDialogBox;
		/**
		 * temporary ID for authentication
		 */
		private int authID;
		
		// callback
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			
			public void onSuccess(final String url) {
				gui.log("Authentication URL arrived: " + url);
				loginDialogBox.hide();
				
                // open the authenticationwindow
				Window.open(url, "AuthenticationWindow", "width=400,height=500");
				
				// start polling for SessionID
				new SessionIDPolling(authID);				
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
			
		// constructor
		public GetAuthenticationURL(AuthenticationServiceType serviceType,
				DialogBox loginDialogBox) {
			super();
			
			this.serviceType = serviceType;
			this.loginDialogBox = loginDialogBox;
			
			enqueue();
		}

		@Override
		void call() {
			authID = Random.nextInt();
			filmTitService.getAuthenticationURL(authID, serviceType, callback);
		}
	}
	
	public class SessionIDPolling extends Callable {

		/**
		 * temporary ID for authentication
		 */
		private int authID;

		/**
		 * dialog polling for session ID
		 */
		private DialogBox sessionIDPollingDialogBox;

		/**
		 * indicates whether polling for session ID is in progress
		 */
		private boolean sessionIDPolling = false;

		// callback
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			
			public void onSuccess(String result) {
				if (result != null) {
					// stop polling
					sessionIDPolling = false;
					sessionIDPollingDialogBox.hide();
					// we now have a session ID
					gui.setSessionID(result);
					// TODO: put some username
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
		
		// constructor
		public SessionIDPolling(int authID) {
			super();
			
			this.authID = authID;
			
			createDialog();
			
            startSessionIDPolling();
		}
		
		/**
		 * open a dialog saying that we are waiting for the user to authenticate
		 */
		private void createDialog() {
            sessionIDPollingDialogBox = new DialogBox(false);
            SessionIDPollingDialog dialog = new SessionIDPollingDialog();
            dialog.btnCancel.addClickHandler( new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					sessionIDPolling = false;
                    gui.log("SessionIDPollingDialog closed by user hitting Cancel button");
                    sessionIDPollingDialogBox.hide();
				}
			});
            sessionIDPollingDialogBox.setWidget(dialog);
            sessionIDPollingDialogBox.setGlassEnabled(true);
            sessionIDPollingDialogBox.center();			
		}

		private void startSessionIDPolling() {
			sessionIDPolling = true;
			
			Scheduler.RepeatingCommand poller = new RepeatingCommand() {
				
				@Override
				public boolean execute() {
					if (sessionIDPolling) {
						// enqueue();
						call();			            
						return true;
					} else {
						return false;
					}
				}
			};
			
			Scheduler.get().scheduleFixedDelay(poller, 2000);
		}

		@Override
		void call() {
			if (sessionIDPolling) {
				gui.log("asking for session ID with authID=" + authID);
				filmTitService.getSessionID(authID, callback);			
			}
		}
	}
	    
	public void validateAuthentication (String responseURL, long authID, AuthenticationValidationWindow authenticationValidationWindow) {
		new ValidateAuthentication(responseURL, authID, authenticationValidationWindow);
	}
	
	public class ValidateAuthentication extends Callable {

		// parameters
		String responseURL;	
		long authID;
		AuthenticationValidationWindow authenticationValidationWindow;
		
		// callback
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
		
		// constructor
		public ValidateAuthentication(String responseURL, long authID,
				AuthenticationValidationWindow authenticationValidationWindow) {
			super();
			
			this.responseURL = responseURL;
			this.authID = authID;
			this.authenticationValidationWindow = authenticationValidationWindow;
			
			enqueue();
		}
				
		@Override
		void call() {
			filmTitService.validateAuthentication(authID, responseURL, callback);		
		}
	}


    public void getListOfDocuments(UserPage userpage) {
    	new GetListOfDocuments(userpage);
    }

    public class GetListOfDocuments extends Callable {

    	// parameters
    	UserPage userpage;
    	
        // callback
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

        // constructor
		public GetListOfDocuments(UserPage userpage) {
			super();

			this.userpage = userpage;
			
			enqueue();
		}
        

		@Override
		void call() {
	        filmTitService.getListOfDocuments(gui.getSessionID(), callback);
		}

	}
    
    public void exportSubtitles (long documentID) {
    	new ExportSubtitles(documentID);
    }
    
    public class ExportSubtitles extends Callable {

    	// parameters
    	long documentID;
    	
    	// callback
        AsyncCallback<String> callback = new AsyncCallback<String>() {

            @Override
            public void onSuccess(String result) {
                gui.log("received subtitle file of " + result.length() + " bytes");
                gui.log("SHOWING FILE");
                gui.log(result);
                gui.log("END OF FILE");                
                gui.log("received subtitle file of " + result.length() + " bytes");
            }

            @Override
            public void onFailure(Throwable caught) {
                gui.log("ERROR: failure on getting subtitle file!");
            }

        };
    	
    	
		// constructor
		public ExportSubtitles(long documentID) {
			super();
			
			this.documentID = documentID;
			
			enqueue();
		}
    	    	
		@Override
		void call() {
	        filmTitService.exportSubtitles(gui.getSessionID(), documentID, callback);
		}

    }
	    
}

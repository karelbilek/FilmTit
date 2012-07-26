package cz.filmtit.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

import cz.filmtit.client.SubgestBox.FakeSubgestBox;
import cz.filmtit.share.*;
import cz.filmtit.share.parsing.*;

//lib-gwt-file imports:
import org.vectomatic.file.File;
import org.vectomatic.file.FileList;
import org.vectomatic.file.FileReader;
import org.vectomatic.file.events.LoadEndEvent;
import org.vectomatic.file.events.LoadEndHandler;

import java.util.*;




/**
 * Entry point for the FilmTit GWT web application,
 * including the GUI creation.
 * 
 * @author Honza Václ
 *
 */

public class Gui implements EntryPoint {

	GuiStructure guiStructure;
	
	protected List<TimedChunk> chunklist;
	
	protected RootPanel rootPanel;

	protected int counter = 0;

	private FilmTitServiceHandler rpcHandler;
	protected Document currentDocument;
	
	private String sessionID;
	
	// persistent session ID via cookies (set to null to unset)
	@SuppressWarnings("deprecation")
	protected void setSessionID(String newSessionID) {
		if (newSessionID == null) {
			Cookies.removeCookie("sessionID");
		} else {
			// cookie should be valid for 1 year (GWT does not support anything better than the deprecated things it seems)
			Date in1year = new Date();
			in1year.setYear(in1year.getYear() + 1);
			// set cookie
			Cookies.setCookie("sessionID", newSessionID, in1year);
		}
		sessionID = newSessionID;
	}

	// persistent session ID via cookies (null if not set)	
	protected String getSessionID() {
		if (sessionID == null) {
			sessionID = Cookies.getCookie("sessionID");
		}
		return sessionID;
	}

	private String username;

	/**
	 * Multi-line subtitle text to parse
	 */
	//private String subtext;
	
	private DocumentCreator docCreator = null;
    private TranslationWorkspace workspace = null;
	
	
	
	@Override
	public void onModuleLoad() {

        // RPC:
		// FilmTitServiceHandler has direct access
		// to package-internal (and public) fields and methods
		// of this Gui instance
		// (because the RPC calls are asynchronous)
		rpcHandler = new FilmTitServiceHandler(this);
		
		// Request translation suggestions for a TimedChunk via:
		// rpcHandler.getTranslationResults(timedchunk);
		// Because the calls are asynchronous, the method returns void.

		// Send feedback via:
		// rpcHandler.setUserTranslation(translationResultId, userTranslation, chosenTranslationPair);


		// determine the page to be loaded (GUI is the default and fallback)
		String page = Window.Location.getParameter("page");
		if (page == null) {
			createGui();			
			log("No page parameter set, showing welcome page...");
		}
        else if (page.equals("AuthenticationValidationWindow")) {
            createAuthenticationValidationWindow();
        }
		else {
			createGui();
			log("Fallback to welcome page (page requested: " + page + ")");
		}

        // TODO: check whether user is logged in or not
        rpcHandler.checkSessionID();
        // TODO: we have to show welcome page as if user not logged in)
        // and repaint this if checkSessionID() succeeds

    }	// onModuleLoad()



	private void createGui() {
		
		// -------------------- //
		// --- GUI creation --- //
		// -------------------- //
		
		rootPanel = RootPanel.get();
		//rootPanel.setSize("800", "600");

		// loading the uibinder-defined structure of the page
		guiStructure = new GuiStructure();
		rootPanel.add(guiStructure, 0, 0);

		// top menu handlers		
		guiStructure.login.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (getSessionID() == null) {
		            showLoginDialog();
				} else {
					rpcHandler.logout();
				}
			}        	
		});

        // only after login:
        //createDocumentCreator();

        showWelcomePage();
	}


	/**
	 * show the Start a new subtitle document panel
	 * inside the GUI contentPanel
	 */
	private void createDocumentCreator() {
        UserPage userpage = new UserPage();
        guiStructure.contentPanel.setStyleName("users_page");

        log("getting list of documents...");
        FlexTable doctable = new FlexTable();
        rpcHandler.getListOfDocuments(doctable);

        userpage.tabDocumentList.add(doctable);


        docCreator = new DocumentCreator();

		// --- file reading interface via lib-gwt-file --- //
		final FileReader freader = new FileReader();
		freader.addLoadEndHandler( new LoadEndHandler() {
			@Override
			public void onLoadEnd(LoadEndEvent event) {
				createDocumentFromText( freader.getStringResult() );
				//log(subtext);
			}
		} );

		docCreator.fileUpload.addChangeHandler( new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				//log(fileUpload.getFilename());
				FileList fl = docCreator.fileUpload.getFiles();
				Iterator<File> fit = fl.iterator();
				if (fit.hasNext()) {
					freader.readAsText(fit.next(), docCreator.getChosenEncoding());
				}
				else {
					error("No file chosen.\n");
				}
			}
		} );
		// --- end of file reading interface via lib-gwt-file --- //

		// --- textarea interface for loading whole subtitle file by copy-paste --- //
		docCreator.btnSendToTm.addClickHandler( new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				createDocumentFromText( docCreator.txtFileContentArea.getText() );
			}
		} );
		// --- end of textarea interface --- //

        userpage.tabNewDocument.add(docCreator);


        guiStructure.contentPanel.setWidget(userpage);

    }
	
	private void createAuthenticationValidationWindow() {
		// ----------------------------------------------- //
		// --- AuthenticationValidationWindow creation --- //
		// ----------------------------------------------- //
		
		rootPanel = RootPanel.get();
		//rootPanel.setSize("800", "600");

		// --- loading the uibinder-defined structure of the page --- //
		final AuthenticationValidationWindow authenticationValidationWindow = new AuthenticationValidationWindow();
		rootPanel.add(authenticationValidationWindow, 0, 0);
        authenticationValidationWindow.btnCancel.addClickHandler( new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// TODO: say to the UserSpace that I am closing the window
                authenticationValidationWindow.close();
			}
		});

		// get authentication data
		authenticationValidationWindow.paraValidation.setText("Processing authentication data...");		
		// response URL
		String responseURL = Window.Location.getQueryString();
		// String responseURL = Window.Location.getParameter("responseURL");
		// auhID
		long authID = 0;
		String authIDstring = Window.Location.getParameter("authID");
		try {
			authID = Long.parseLong(authIDstring);
		}
		catch (Exception e) {
			// TODO: handle exception
			Window.alert("Cannot parse authID " + authIDstring + " as a number! " + e.getLocalizedMessage());
		}
		
		// send RPC
		authenticationValidationWindow.paraValidation.setText("Validating authentication data for authID " + authID + "...");
		rpcHandler.validateAuthentication (responseURL, authID, authenticationValidationWindow);
	}

	private void createDocumentFromText(String subtext) {
        rpcHandler.createDocument(docCreator.getMovieTitle(),
                docCreator.getMovieYear(),
                docCreator.getChosenLanguage(),
                subtext);
        // sets currentDocument and calls processText() on success
    }

    protected void document_created() {
        // replacing the document-creating interface with the subtitle table:
        this.workspace = new TranslationWorkspace(this);
        guiStructure.contentPanel.setWidget(workspace);
        guiStructure.contentPanel.setStyleName("translating");
    }


	
	/**
	 * Parse the given text in the subtitle format of choice (by the radiobuttons)
	 * into this.chunklist (List<TimedChunk>).
	 * Currently verbosely outputting both input text, format
	 * and output chunks into the debug-area,
	 * also "reloads" the CellBrowser interface accordingly.
     *
     * @param subtext - multiline text (of the whole subtitle file, typically) to parse
	 */
	protected void processText(String subtext) {
		// dump the input text into the debug-area:
		log("processing the following input:\n" + subtext + "\n");
		
		// determine format (from corresponding radiobuttons) and choose parser:
		String subformat = docCreator.getChosenSubFormat();
		Parser subtextparser;
		if (subformat == "sub") {  // i.e. ".sub" is checked
			subtextparser = new ParserSub();
		}
		else {  // i.e. ".srt" is checked
			assert subformat == "srt" : "One of the subtitle formats must be chosen.";
			subtextparser = new ParserSrt();
		}
		log("subtitle format chosen: " + subformat);
				
		// parse:
		log("starting parsing");
		long startTime = System.currentTimeMillis();
		this.chunklist = subtextparser.parse(subtext, this.currentDocument.getId(), Language.EN);
		long endTime = System.currentTimeMillis();
		long parsingTime = endTime - startTime;
		log("parsing finished in " + parsingTime + "ms");

		for (TimedChunk chunk : chunklist) {
		    TranslationResult tr = new TranslationResult();
		    tr.setSourceChunk(chunk);
		    this.currentDocument.translationResults.add(tr);
		}

		// output the parsed chunks:
		log("parsed chunks: "+chunklist.size());

        int i=0;
        for (TimedChunk timedchunk : chunklist) {
            workspace.showSource(timedchunk, i++);
		}

		Scheduler.get().scheduleIncremental(new SendChunksRepeatingCommand(chunklist));

	}
	
	
	
	class SendChunksRepeatingCommand implements RepeatingCommand {

		LinkedList<TimedChunk> chunks;
		
		public SendChunksRepeatingCommand(List<TimedChunk> chunks) {
			this.chunks = new LinkedList<TimedChunk>(chunks);
		}

        //exponential window
        //
        //a "trick" - first subtitle goes in a single request so it's here soonest without wait
        //then the next two
        //then the next four
        //then next eight
        //so the first 15 subtitles arrive as quickly as possible
        //but we also want as little requests as possible -> the "window" is
        //exponentially growing
        int exponential = 1;

		@Override
        public boolean execute() {
			if (chunks.isEmpty()) {
				return false;
			} else {
                List<TimedChunk> sentTimedchunks = new ArrayList<TimedChunk>(exponential);
				for (int i = 0; i < exponential; i++) {
                    if (!chunks.isEmpty()){
                        TimedChunk timedchunk = chunks.removeFirst();
				        sentTimedchunks.add(timedchunk);
                    }
                }
                sendChunks(sentTimedchunks);
			    exponential = exponential*2;	
                return true;
			}
		}
		
		private void sendChunks(List<TimedChunk> timedchunks) {
			rpcHandler.getTranslationResults(timedchunks);
		}
	}
	
	
	public Document getCurrentDocument() {
		return currentDocument;
	}
		
	protected void setCurrentDocument(Document currentDocument) {
		this.currentDocument = currentDocument;
	}


	/**
	 * Send the given translation result as a "user-feedback" to the userspace
	 * @param transresult
	 */
	public void submitUserTranslation(TranslationResult transresult) {
		String combinedTRId = transresult.getDocumentId() + ":" + transresult.getChunkId();
		log("sending user feedback with values: " + combinedTRId + ", " + transresult.getUserTranslation() + ", " + transresult.getSelectedTranslationPairID());
		rpcHandler.setUserTranslation(transresult.getChunkId(), transresult.getDocumentId(),
				                      transresult.getUserTranslation(), transresult.getSelectedTranslationPairID());
	}


    long start=0;
	/**
	 * Output the given text in the debug textarea
     * with a timestamp relative to the first logging.
	 * @param logtext
	 */
	public void log(String logtext) {
		if (start == 0) {
            start = System.currentTimeMillis();
        }
        long diff = (System.currentTimeMillis() - start);
        guiStructure.txtDebug.setText(guiStructure.txtDebug.getText() + diff+" : " + logtext + "\n");
		guiStructure.txtDebug.setCursorPos(guiStructure.txtDebug.getText().length());
	}
	
	private void error(String errtext) {
		log(errtext);
	}
	
	/**
	 * show a dialog enabling the user to
	 * log in directly or [this line maybe to be removed]
	 * via OpenID services
	 */
    protected void showLoginDialog() {
    	
    	final DialogBox dialogBox = new DialogBox(false);
        final LoginDialog loginDialog = new LoginDialog();
        
        loginDialog.btnLogin.addClickHandler( new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialogBox.hide();
                log("trying to log in as user " + loginDialog.getUsername());
				rpcHandler.simple_login(loginDialog.getUsername(), loginDialog.getPassword());					
            }
        } );
        
        loginDialog.btnLoginGoogle.addClickHandler( new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
                log("trying to log in through Google account");
				rpcHandler.getAuthenticationURL(AuthenticationServiceType.GOOGLE, dialogBox);
			}
		});
        
        loginDialog.btnCancel.addClickHandler( new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
                log("LoginDialog closed by user hitting Cancel button");
                dialogBox.hide();
			}
		});
        
        dialogBox.setWidget(loginDialog);
        dialogBox.setGlassEnabled(true);
        dialogBox.center();
    }

	protected void please_log_in () {
		logged_out ();
		rpcHandler.displayWindow("Please log in first.");
		showLoginDialog();
	}
	
	protected void please_relog_in () {
		logged_out ();
		rpcHandler.displayWindow("You have not logged in or your session has expired. Please log in.");
		showLoginDialog();
	}
	
	protected void logged_in (String username) {
        this.username = username;
		guiStructure.login.setText("Log out user " + username);
        createDocumentCreator();
	}
	
	protected void logged_out () {
        this.username = null;
		guiStructure.login.setText("Log in");
        showWelcomePage();
	}

    protected void showWelcomePage() {
        WelcomeScreen welcomePage = new WelcomeScreen();
        guiStructure.contentPanel.setStyleName("welcoming");
        guiStructure.contentPanel.setWidget(welcomePage);
    }


    public TranslationWorkspace getTranslationWorkspace() {
        return workspace;
    }
	
}

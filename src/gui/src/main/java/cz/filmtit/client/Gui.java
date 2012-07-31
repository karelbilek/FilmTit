package cz.filmtit.client;

import com.google.gwt.core.client.*;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;
import cz.filmtit.share.*;
import cz.filmtit.share.parsing.*;
import org.vectomatic.file.File;
import org.vectomatic.file.FileList;
import org.vectomatic.file.FileReader;
import org.vectomatic.file.events.LoadEndEvent;
import org.vectomatic.file.events.LoadEndHandler;
import cz.filmtit.client.SubgestBox.FakeSubgestBox;
import com.google.gwt.cell.client.FieldUpdater;

import java.util.*;

//lib-gwt-file imports:




/**
 * Entry point for the FilmTit GWT web application,
 * including the GUI creation.
 * 
 * @author Honza Václ
 *
 */

public class Gui implements EntryPoint {

     GuiStructure guiStructure;
     
     protected Map<ChunkIndex, TimedChunk> chunkmap;

     public TimedChunk getChunk(ChunkIndex chunkIndex) {
        return chunkmap.get(chunkIndex);
     }
     
     protected RootPanel rootPanel;

     protected int counter = 0;

     private FilmTitServiceHandler rpcHandler;
     protected Document currentDocument;
     
     private PageHandler pageHandler;
     
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
		rpcHandler = new FilmTitServiceHandler(this);
          
		// page loading and switching
		pageHandler = new PageHandler(Window.Location.getParameter("page"), this);
		pageHandler.setDocumentId(Window.Location.getParameter("documentId"));

        // check whether user is logged in or not
        rpcHandler.checkSessionID();
        
    }

     void createGui() {
          
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

     }

    void createNewDocumentCreator() {
        docCreator = new DocumentCreator();

    // --- file reading interface via lib-gwt-file --- //
        final FileReader freader = new FileReader();
        freader.addLoadEndHandler( new LoadEndHandler() {
            @Override
            public void onLoadEnd(LoadEndEvent event) {
            docCreator.lblUploadProgress.setText("File uploaded successfully.");
            docCreator.btnCreateDocument.setEnabled(true);
                //log(subtext);
            }
        } );

        docCreator.fileUpload.addChangeHandler( new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                //log(fileUpload.getFilename());
            docCreator.lblUploadProgress.setVisible(true);
            docCreator.lblUploadProgress.setText("Uploading the file...");
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

        docCreator.btnCreateDocument.addClickHandler( new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                docCreator.lblCreateProgress.setVisible(true);
                docCreator.lblCreateProgress.setText("Creating the document...");
                createDocumentFromText( freader.getStringResult() );
            }
        } );
        
        guiStructure.contentPanel.setWidget(docCreator);
    }


     /**
      * show the Start a new subtitle document panel
      * inside the GUI contentPanel
      */
     void createAndLoadUserPage() {
        UserPage userpage = new UserPage(
            new FieldUpdater<Document, String>() {
                public void update(int index, Document doc, String value) {
                    editDocument(doc);
                }
            }
        );
        guiStructure.contentPanel.setStyleName("users_page");

        log("getting list of documents...");
        rpcHandler.getListOfDocuments(userpage);



        userpage.btnDisplayCreator.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                createNewDocumentCreator();
            }

        });


        guiStructure.contentPanel.setWidget(userpage);

    }
     
     void createAuthenticationValidationWindow() {
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
        rpcHandler.createDocument(
                docCreator.getTitle(),
                docCreator.getMovieTitle(),
                docCreator.getChosenLanguage(),
                subtext,
                docCreator.getMoviePathOrNull());
        // sets currentDocument and calls processText() on success
    }

    protected void document_created(String moviePath) {
        // replacing the document-creating interface with the subtitle table:
        this.workspace = new TranslationWorkspace(this, moviePath);
        guiStructure.contentPanel.setWidget(workspace);
        guiStructure.contentPanel.setStyleName("translating");
    }

    public void editDocument(Document document) {
        rpcHandler.loadDocumentFromDB(document.getId());
    }

    public void editDocument(long documentId) {
        rpcHandler.loadDocumentFromDB(documentId);
    }

    protected void processTranslationResultList(List<TranslationResult> translations) {
        
          chunkmap = new HashMap<ChunkIndex, TimedChunk>();

          List<TimedChunk> untranslatedOnes = new LinkedList<TimedChunk>();


          for (TranslationResult tr:translations) {
              TimedChunk sChunk = tr.getSourceChunk();
              chunkmap.put(sChunk.getChunkIndex(), sChunk);
              String tChunk = tr.getUserTranslation();
              

              ChunkIndex chunkIndex = sChunk.getChunkIndex();
              
              
              this.currentDocument.translationResults.put(chunkIndex, tr);
              
              workspace.showSource(sChunk);

              if (tChunk==null || tChunk.equals("")){
                 untranslatedOnes.add(sChunk);
              } else {
                 workspace.showResult(tr);
              
              }
          }
          if (untranslatedOnes.size() > 0) {
            SendChunksCommand sendChunks = new SendChunksCommand(untranslatedOnes);
            sendChunks.execute();
          }
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
         
          chunkmap = new HashMap<ChunkIndex, TimedChunk>();

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
          
          //Window.alert("1");
          // parse:
          log("starting parsing");
          long startTime = System.currentTimeMillis();
          List<TimedChunk> chunklist = subtextparser.parse(subtext, this.currentDocument.getId(), Language.EN);
          long endTime = System.currentTimeMillis();
          long parsingTime = endTime - startTime;
          log("parsing finished in " + parsingTime + "ms");
          //Window.alert("2");

          for (TimedChunk chunk : chunklist) {
              chunkmap.put(chunk.getChunkIndex(), chunk);
              ChunkIndex chunkIndex = chunk.getChunkIndex();
              TranslationResult tr = new TranslationResult();
              tr.setSourceChunk(chunk);
              this.currentDocument.translationResults.put(chunkIndex, tr);
              
          }
          
          //Window.alert("3");

          // output the parsed chunks:
          log("parsed chunks: "+chunklist.size());
            
          int size=50; 
          int i = 0;
          for (TimedChunk timedchunk : chunklist) {
            workspace.showSource(timedchunk);
            if (i%size==0) {
              //  Window.alert("another "+i);
            }
            i++;
          }
//          Window.alert("4");

          SendChunksCommand sendChunks = new SendChunksCommand(chunklist);
          sendChunks.execute();
  //        Window.alert("5");
     }
     
     
     
     class SendChunksCommand {

          LinkedList<TimedChunk> chunks;
          
          public SendChunksCommand(List<TimedChunk> chunks) {
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
               rpcHandler.getTranslationResults(timedchunks, this);
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
          
          ChunkIndex chunkIndex = transresult.getSourceChunk().getChunkIndex();
          rpcHandler.setUserTranslation(chunkIndex, transresult.getDocumentId(),
                                          transresult.getUserTranslation(), transresult.getSelectedTranslationPairID());
     }


    long start=0;
     private StringBuilder sb = new StringBuilder();
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
        sb.append(diff);
        sb.append(" : ");
       
        sb.append(logtext);
        sb.append("\n");
        guiStructure.txtDebug.setText(sb.toString());
     }
     
     private void error(String errtext) {
          log(errtext);
     }
     
     /**
      * show a dialog enabling the user to
      * log in directly
      * or via OpenID services [this line maybe to be removed]
      */
     protected void showLoginDialog() {
    	 showLoginDialog("");
     }
    protected void showLoginDialog(String username) {
         
         final DialogBox dialogBox = new DialogBox(false);
        final LoginDialog loginDialog = new LoginDialog();
        
        loginDialog.btnLogin.addClickHandler( new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            	String username = loginDialog.getUsername();
            	String password = loginDialog.getPassword();
            	if (username.isEmpty()) {
            		Window.alert("Please fill in the username!");
            	} else if (password.isEmpty()) {
            		Window.alert("Please fill in the password!");					
				} else {
	                dialogBox.hide();
	                log("trying to log in as user " + username);
	                rpcHandler.simpleLogin(username, password);
				}
            }
        } );
        
        loginDialog.btnForgottenPassword.addClickHandler( new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                 String username = loginDialog.getUsername();
                 if (username.isEmpty()) {
                      Window.alert("You must fill in your username!");
                 } else {
                     dialogBox.hide();
                    log("forgotten password - user " + username);
                    //TODO: invoke RPC call
                        //rpcHandler.forgotten_password(username);                      
                 }
            }
        } );
        
        loginDialog.btnLoginGoogle.addClickHandler( new ClickHandler() {
               @Override
               public void onClick(ClickEvent event) {
                log("trying to log in through Google account");
                    rpcHandler.getAuthenticationURL(AuthenticationServiceType.GOOGLE, dialogBox);
               }
          });
        
        loginDialog.btnRegister.addClickHandler( new ClickHandler() {
               @Override
               public void onClick(ClickEvent event) {
                log("User decided to register, showing registration form");
                dialogBox.hide();
                showRegistrationForm();
               }
          });
        
        loginDialog.btnCancel.addClickHandler( new ClickHandler() {
               @Override
               public void onClick(ClickEvent event) {
                log("LoginDialog closed by user hitting Cancel button");
                dialogBox.hide();
               }
          });
        
        loginDialog.setUsername(username);
        
        dialogBox.setWidget(loginDialog);
        dialogBox.setGlassEnabled(true);
        dialogBox.center();
    }

     /**
      * show a dialog enabling the user to
      * log in directly or [this line maybe to be removed]
      * via OpenID services
      */
    protected void showRegistrationForm() {
         
         final DialogBox dialogBox = new DialogBox(false);
        final RegistrationForm registrationForm = new RegistrationForm();
        
        registrationForm.btnRegister.addClickHandler( new ClickHandler() {
               @Override
               public void onClick(ClickEvent event) {
                log("Trying to register user...");
                // check data entered
                if (registrationForm.checkForm()) {
                    // invoke the registration
                        rpcHandler.registerUser(registrationForm.getUsername(), registrationForm.getPassword(), registrationForm.getEmail(), dialogBox);
                } else {
                     log("errors in registration form");
                     Window.alert("Please correct errors in registration form.");
                     // TODO: tell the user what is wrong
                }
               }
          });
        
        registrationForm.btnCancel.addClickHandler( new ClickHandler() {
               @Override
               public void onClick(ClickEvent event) {
                log("RegistrationForm closed by user hitting Cancel button");
                dialogBox.hide();
               }
          });
        
        dialogBox.setWidget(registrationForm);
        dialogBox.setGlassEnabled(true);
        dialogBox.center();
    }

     protected void please_log_in () {
          logged_out ();
          Window.alert("Please log in first.");
          showLoginDialog();
     }
     
     protected void please_relog_in () {
          logged_out ();
          Window.alert("You have not logged in or your session has expired. Please log in.");
          showLoginDialog();
     }
     
     protected void logged_in (String username) {
        this.username = username;
    	guiStructure.login.setText("Log out user " + username);
        pageHandler.loadPage(true);
     }
     
     protected void logged_out () {
        this.username = null;
        guiStructure.login.setText("Log in");
        pageHandler.loadPage(false);
     }

    protected void showWelcomePage() {
        WelcomeScreen welcomePage = new WelcomeScreen();

        welcomePage.login.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (getSessionID() == null) {
                    showLoginDialog();
                } else {
                    rpcHandler.logout();
                }
            }
        });
        welcomePage.register.addClickHandler( new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showRegistrationForm();
            }
        });

        guiStructure.contentPanel.setStyleName("welcoming");
        guiStructure.contentPanel.setWidget(welcomePage);
    }


    public TranslationWorkspace getTranslationWorkspace() {
        return workspace;
    }
     
}

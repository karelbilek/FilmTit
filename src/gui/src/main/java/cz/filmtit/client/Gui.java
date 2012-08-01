package cz.filmtit.client;

import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.NavWidget;
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

/**
 * Entry point for the FilmTit GWT web application,
 * including the GUI creation.
 *
 * @author Honza Václ
 *
 */

public class Gui implements EntryPoint {

    ///////////////////////////////////////
    //                                   //
    //      Data fields                  //
    //                                   //
    ///////////////////////////////////////
    
	
	// Handlers
	
	/**
	 * handles especially the menu
	 */
    GuiStructure guiStructure;

 	/**
 	 * handles RPC calls
 	 */
    FilmTitServiceHandler rpcHandler;

 	/**
 	 * handles page switching
 	 */
    PageHandler pageHandler;
    
    
    // Login state fields

    boolean loggedIn = false;
    
    private String username;

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
    
    // Other fields - some of them will probably be moved some place else

     protected Map<ChunkIndex, TimedChunk> chunkmap;

     public TimedChunk getChunk(ChunkIndex chunkIndex) {
        return chunkmap.get(chunkIndex);
     }

     protected RootPanel rootPanel;

     protected int counter = 0;

     protected Document currentDocument;

     public Document getCurrentDocument() {
          return currentDocument;
     }

     protected void setCurrentDocument(Document currentDocument) {
          this.currentDocument = currentDocument;
     }

     private TranslationWorkspace workspace = null;
     
     public TranslationWorkspace getTranslationWorkspace() {
         return workspace;
     }

     private DocumentCreator docCreator = null;

    ///////////////////////////////////////
    //                                   //
    //      The "main()" of GUI          //
    //                                   //
    ///////////////////////////////////////
    
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

    
    ///////////////////////////////////////
    //                                   //
    //      Logging                      //
    //                                   //
    ///////////////////////////////////////
    
    long start=0;
    private StringBuilder sb = new StringBuilder();
    /**
     * Output the given text in the debug textarea
    * with a timestamp relative to the first logging.
     * @param logtext
     */
    public void log(String logtext) {
   	 if (guiStructure != null) {
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
   	 else {
       	 // txtDebug not created
   		 // but let's at least display the message in the statusbar
   		 // (does not work in Firefox according to documentation)
   		 Window.setStatus(logtext);
   	 }
    }

    void error(String errtext) {
         log(errtext);
    }


    ///////////////////////////////////////
    //                                   //
    //      Login - logout methods       //
    //                                   //
    ///////////////////////////////////////
    
    /**
     * show a dialog enabling the user to
     * log in directly or [this line maybe to be removed]
     * via OpenID services
     */
	protected void showLoginDialog() {
		showLoginDialog("");
	}
     
    /**
     * show a dialog enabling the user to
     * log in directly or [this line maybe to be removed]
     * via OpenID services
     * @param username
     */
	protected void showLoginDialog(String username) {
	    LoginDialog loginDialog = new LoginDialog(username, this);
	}

    /**
     * show the registration dialog
     */
    protected void showRegistrationForm() {
        RegistrationForm registrationForm = new RegistrationForm(this);
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
    	loggedIn = true;
    	this.username = username;
    	guiStructure.logged_in(username);
    	pageHandler.loadPage(true);
    }

    protected void logged_out () {
    	loggedIn = false;
        username = null;
        guiStructure.logged_out();
        pageHandler.loadPage(false);
    }

    ///////////////////////////////////////
    //                                   //
    //      Page creation methods        //
    //                                   //
    ///////////////////////////////////////
    
    void createGui() {
        guiStructure = new GuiStructure(this);
    }
    
    protected void showWelcomePage() {
        new WelcomeScreen(this);
    }

    protected void showChangePasswordForm() {
        new ChangePassword(this);
    }

    protected void showAboutPage() {
        new About(this);
    }

    void createAuthenticationValidationWindow() {
    	new AuthenticationValidationWindow(this);
    }
	
    public void editDocument(Document document) {
        rpcHandler.loadDocumentFromDB(document.getId());
    }

    public void editDocument(long documentId) {
        rpcHandler.loadDocumentFromDB(documentId);
    }

    void createAndLoadUserPage() {
        new UserPage(this, new FieldUpdater<Document, String>() {
                    public void update(int index, Document doc, String value) {
                        editDocument(doc);
                    }
                }
            );
    }

    void createNewDocumentCreator() {
        docCreator = new DocumentCreator(this);
    }
    
    void document_created(String moviePath) {
        workspace = new TranslationWorkspace(this, moviePath);
    }

    ///////////////////////////////////////
    //                                   //
    //      Some mess remaining...       //
    //                                   //
    ///////////////////////////////////////
    
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

}

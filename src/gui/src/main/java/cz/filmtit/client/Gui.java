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
import cz.filmtit.client.dialogs.DownloadDialog;
import cz.filmtit.client.dialogs.LoginDialog;
import cz.filmtit.client.dialogs.LoginDialog.Tab;
import cz.filmtit.client.pages.GuiStructure;
import cz.filmtit.client.pages.TranslationWorkspace;
import cz.filmtit.client.subgestbox.SubgestBox.FakeSubgestBox;

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
    
	/**
	 * a singleton representing the Gui instance
	 */
	private static Gui gui;
	
	/**
	 * a singleton representing the Gui instance
	 */
	public static Gui getGui() {
		return gui;
	}

	/**
	 * handles especially the menu
	 */
    public GuiStructure guiStructure;

 	/**
 	 * handles RPC calls
 	 */
    public FilmTitServiceHandler rpcHandler;

 	/**
 	 * handles page switching
 	 */
    public PageHandler pageHandler;
   
    public PageHandler getPageHandler() {
        return pageHandler;
    }
    
    // Login state fields

    public boolean loggedIn = false;
    
    private String username;

    private String sessionID;
    
    private static final String SESSIONID = "sessionID";
    
    // persistent session ID via cookies (set to null to unset)
    public void setSessionID(String newSessionID) {
         if (newSessionID == null) {
              Cookies.removeCookie(SESSIONID);
         } else {
              Cookies.setCookie(SESSIONID, newSessionID, getDateIn1Year());
         }
         sessionID = newSessionID;
    }

    // persistent session ID via cookies (null if not set)
    public String getSessionID() {
         if (sessionID == null) {
              sessionID = Cookies.getCookie(SESSIONID);
         }
         return sessionID;
    }
    
    // Other fields

    /**
     * the current workspace
     */
    public TranslationWorkspace currentWorkspace;
    
    @SuppressWarnings("deprecation")
    public static Date getDateIn1Year() {
        // cookies should be valid for 1 year (GWT does not support anything better than the deprecated things it seems)
        Date in1year = new Date();
        in1year.setYear(in1year.getYear() + 1);
    	return in1year;
    }

    ///////////////////////////////////////
    //                                   //
    //      The "main()" of GUI          //
    //                                   //
    ///////////////////////////////////////

    @Override
    public void onModuleLoad() {
    	
    	try {
	    	
	    	// set the Gui singleton
	    	Gui.gui = this;
	
			// RPC:
			rpcHandler = new FilmTitServiceHandler();
	
			// page loading and switching
			pageHandler = new PageHandler();
			
			if (pageHandler.doCheckSessionID) {
	    		// check whether user is logged in or not
	    		gui.rpcHandler.checkSessionID();
			}
            
    	}
    	catch (Exception e) {
			exceptionCatcher(e);
		}
		
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

    public void error(String errtext) {
         log(errtext);
    }
    
    /**
     * log and alert the exception
     * @param e the exception
     * @return the logged string
     */
    public String exceptionCatcher(Throwable e) {
    	// TODO: for production, this should be:
    	// return exceptionCatcher(e, false, true)
    	return exceptionCatcher(e, true, true);    	
    }
    
    public String exceptionCatcher(Throwable e, boolean alertIt) {
    	return exceptionCatcher(e, alertIt, true);
    }
    
    public String exceptionCatcher(Throwable e, boolean alertIt, boolean logIt) {
    	
    	StringBuilder sb = new StringBuilder();
    	sb.append(e.toString());
    	sb.append('\n');
		StackTraceElement[] st = e.getStackTrace();
		for (StackTraceElement stackTraceElement : st) {
	    	sb.append(stackTraceElement);
	    	sb.append('\n');
		}
		
		String result = sb.toString();
		
		if (logIt) {
			log(result);
		}
		
		if (alertIt) {
			Window.alert("Exception caught!\n" + e + '\n' + e.getLocalizedMessage());
		}
		
		return result;
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
	public void showLoginDialog() {
		showLoginDialog("");
	}
     
    /**
     * show a dialog enabling the user to
     * log in directly or [this line maybe to be removed]
     * via OpenID services
     * @param username
     */
	public void showLoginDialog(String username) {
	    new LoginDialog(username);
	}

    public void showDownloadDialog(Document document) {
        new DownloadDialog(document);
    }

    /**
     * show the registration dialog
     */
    public void showRegistrationForm() {
        new LoginDialog(Tab.Register);
    }

    public void please_log_in () {
        logged_out ();
        Window.alert("Please log in first.");
        showLoginDialog();
    }
    
    public void please_relog_in () {
        logged_out ();
        Window.alert("You have not logged in or your session has expired. Please log in.");
        showLoginDialog();
    }

    public void logged_in (String username) {
    	log("User " + username + " is logged in.");
    	// login state fields
    	loggedIn = true;
    	this.username = username;
        // actions
    	guiStructure.logged_in(username);
    	pageHandler.loadPage();
    }

    public void logged_out () {
    	log("User is logged out.");
    	// login state fields
    	loggedIn = false;
        username = null;
        setSessionID(null);
        // actions
        guiStructure.logged_out();
        pageHandler.loadPage();
    }
    
}

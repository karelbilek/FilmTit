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

import cz.filmtit.client.PageHandler.Page;
import cz.filmtit.client.callables.SetUserTranslation;
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
    public static GuiStructure guiStructure;

 	/**
 	 * handles page switching
 	 */
    public static PageHandler pageHandler;
   
 	/**
 	 * handles page switching
 	 */
    public static PageHandler getPageHandler() {
        return pageHandler;
    }
    
    // Login state fields

    private static boolean loggedIn = false;
    
    public static boolean isLoggedIn() {
		return loggedIn;
	}

	private static String username;

    public static String getUsername() {
		return username;
	}

	private static String sessionID;
    
    private static final String SESSIONID = "sessionID";
    
    // persistent session ID via cookies (set to null to unset)
    public static void setSessionID(String newSessionID) {
         if (newSessionID == null) {
              Cookies.removeCookie(SESSIONID);
         } else {
              Cookies.setCookie(SESSIONID, newSessionID, getDateIn1Year());
         }
         sessionID = newSessionID;
    }

    // persistent session ID via cookies (null if not set)
    public static String getSessionID() {
         if (sessionID == null) {
              sessionID = Cookies.getCookie(SESSIONID);
         }
         return sessionID;
    }
    
    // Other fields

    /**
     * the current workspace
     */
    public static TranslationWorkspace currentWorkspace;
    
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
    	
    	// handle all uncaught exceptions
    	GWT.setUncaughtExceptionHandler(new ExceptionHandler(true, true));
    	
    	// set the Gui singleton
    	Gui.gui = this;

		// set the pageHandler singleton for page loading and switching
		Gui.pageHandler = new PageHandler();
		
		if (Gui.getPageHandler().doCheckSessionID) {
    		// check whether user is logged in or not
    		FilmTitServiceHandler.checkSessionID();
		}
		
    }

    
    ///////////////////////////////////////
    //                                   //
    //      Logging                      //
    //                                   //
    ///////////////////////////////////////
    
    private static long start=0;
    private static StringBuilder logStringBuilder = new StringBuilder();
    /**
     * Output the given text in the debug textarea
    * with a timestamp relative to the first logging.
     * @param logtext
     */
    public static void log(String logtext) {
   	 if (guiStructure != null) {
	        if (start == 0) {
	            start = System.currentTimeMillis();
	        }
	        long diff = (System.currentTimeMillis() - start);
	        logStringBuilder.append(diff);
	        logStringBuilder.append(" : ");

	        logStringBuilder.append(logtext);
	        logStringBuilder.append("\n");
	        guiStructure.txtDebug.setText(logStringBuilder.toString());
   	 }
   	 else {
       	 // txtDebug not created
   		 // but let's at least display the message in the statusbar
   		 // (does not work in Firefox according to documentation)
   		 Window.setStatus(logtext);
   	 }
   	 // also log to development console
   	 GWT.log(logtext);
    }

    public static void error(String errtext) {
         log(errtext);
    }
    
    /**
     * log and alert the exception
     * @param e the exception
     * @return the logged string
     */
    public static String exceptionCatcher(Throwable e) {
    	// TODO: for production, this should be:
    	// return exceptionCatcher(e, false, true)
    	return exceptionCatcher(e, true, true);    	
    }
    
    public static String exceptionCatcher(Throwable e, boolean alertIt) {
    	return exceptionCatcher(e, alertIt, true);
    }
    
    public static String exceptionCatcher(Throwable e, boolean alertIt, boolean logIt) {
    			
		StringBuilder sb = new StringBuilder();
		
    	// exception name and message
    	sb.append(e.toString());
    	sb.append('\n');
    	// exception stacktrace
		StackTraceElement[] st = e.getStackTrace();
		for (StackTraceElement stackTraceElement : st) {
	    	sb.append(stackTraceElement);
	    	sb.append('\n');
		}
		
		String result = sb.toString();
		
		if (logIt) {
	    	// log exception name, message and stacktrace
			log(result);
		}
		
		if (alertIt) {
	    	// alert exception name and message
			Window.alert("Exception caught! \n" + e.toString());
		}
		
    	// return exception name, message and stacktrace
		return result;
    }
    
    private class ExceptionHandler implements GWT.UncaughtExceptionHandler {

    	boolean alertExceptions;
    	boolean logExceptions;
    	
		private ExceptionHandler(boolean alertExceptions, boolean logExceptions) {
			this.alertExceptions = alertExceptions;
			this.logExceptions = logExceptions;
		}

		@Override
		public void onUncaughtException(Throwable e) {
			exceptionCatcher(e, alertExceptions, logExceptions);
		}
    	
    }


    ///////////////////////////////////////
    //                                   //
    //      Login - logout methods       //
    //                                   //
    ///////////////////////////////////////
    
    public static void logged_in (String username) {
    	log("User " + username + " is logged in.");
    	// login state fields
    	Gui.loggedIn = true;
    	Gui.username = username;
        // actions
    	Gui.guiStructure.logged_in(username);
    	Gui.pageHandler.loadPage();
    	
    	SetUserTranslation.setOnline(true);
    }

    /**
     * Change the state of the app to "logged out"
     * the page URL will be kept (so that if the user logs back in,
     * he will be shown the same page as before he logged out)
     */
    public static void logged_out () {
    	logged_out(false);
    }
    
    /**
     * Change the state of the app to "logged out"
     * @param goToWelcomeScreen if set to true, the page URL will be changed to WelcomeScreen;
     * otherwise the URL will be kept (so that if the user logs back in,
     * he will be shown the same page as before he logged out)
     */
    public static void logged_out (boolean goToWelcomeScreen) {
    	log("User is logged out.");
    	// login state fields
    	Gui.loggedIn = false;
    	Gui.username = null;
    	Gui.setSessionID(null);
        // actions
        Gui.guiStructure.logged_out();
        if (goToWelcomeScreen) {
        	Gui.pageHandler.loadPage(Page.WelcomeScreen);
        	Gui.pageHandler.refresh();
        } else {
        	Gui.pageHandler.loadPage(true);
        }
    }
    
}

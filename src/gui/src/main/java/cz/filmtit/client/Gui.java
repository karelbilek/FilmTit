/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/

package cz.filmtit.client;

import java.util.Date;
import java.util.Set;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.web.bindery.event.shared.UmbrellaException;

import cz.filmtit.client.PageHandler.Page;
import cz.filmtit.client.callables.CheckSessionID;
import cz.filmtit.client.callables.LogGuiMessage;
import cz.filmtit.client.pages.GuiStructure;
import cz.filmtit.share.LevelLogEnum;
import cz.filmtit.share.User;

/**
 * Entry point for the FilmTit GWT web application,
 * including the GUI creation.
 * Behaves as a static class.
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
	 * handles especially the menu
	 */
    private static GuiStructure guiStructure;

	/**
	 * handles especially the menu
	 */
 	public static GuiStructure getGuiStructure() {
		return guiStructure;
	}

	/**
 	 * handles page switching
 	 */
    private static PageHandler pageHandler;
   
 	/**
 	 * handles page switching
 	 */
    public static PageHandler getPageHandler() {
        return pageHandler;
    }
    
    /**
     * Returns the panel in which the VLC player should be displayed.
     */
    public static HTMLPanel getPanelForVLC() {
        return guiStructure.getPanelForVLC();
    }
    
    /**
     * Returns the panel in which the VLC player should be displayed.
     */
    public static HTMLPanel getPanelForVideo() {
        return guiStructure.getPanelForVideo();
    }


    // Login state fields

    /**
     * True if user is logged in, false if not.
     */
    private static boolean loggedIn = false;
    
    /**
     * True if user is logged in, false if not.
     */
    public static boolean isLoggedIn() {
		return loggedIn;
	}

    /**
     * The user that is currently logged in, or null if no user is logged in.
     */
	private static User user;

    /**
     * The user that is currently logged in, or null if no user is logged in.
     */
    public static User getUser() {
		return user;
	}

    /**
     * The name of the user that is currently logged in, or null if no user is logged in.
     */
    public static String getUsername() {
		if (user != null) {
	    	return user.getName();			
		}
		else {
			return null;
		}
	}

    /**
     * The id of the user that is currently logged in, or -1 if no user is logged in.
     */
    public static long getUserID() {
		if (user != null) {
	    	return user.getId();			
		}
		else {
			return -1;
		}
	}

    /**
     * The active sessionID, or null if no user is logged in.
     */
	private static String sessionID;
    
    private static final String SESSIONID = "sessionID";
    
    /**
     * Set the active sessionID, or set null to unset the sessionID.
     * Made persistent via cookies.
     */
    public static void setSessionID(String newSessionID) {
         if (newSessionID == null) {
              Cookies.removeCookie(SESSIONID);
         } else {
              Cookies.setCookie(SESSIONID, newSessionID, getDateIn1Year());
         }
         sessionID = newSessionID;
    }

    /**
     * Get the active sessionID, or null if no user is logged in.
     * Made persistent via cookies.
     */
    public static String getSessionID() {
         if (sessionID == null) {
              sessionID = Cookies.getCookie(SESSIONID);
         }
         return sessionID;
    }
    
    // Other fields

    /**
     * Cookies should be valid for 1 year.
     * This method calculates the desired expiry date for a cookie.
     */
    @SuppressWarnings("deprecation")
    public static Date getDateIn1Year() {
        // (GWT does not support anything better than the deprecated things it seems)
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
    	
		// set the pageHandler singleton for page loading and switching
		pageHandler = new PageHandler();
		
		if (getPageHandler().fullInitialization) {
			
            // base of GUI is created
    		guiStructure = new GuiStructure();
            
    		// check whether user is logged in or not
    		new CheckSessionID();
    		
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
         /*
         // assemble message
    	 if (start == 0) {
             start = System.currentTimeMillis();
         }
         long diff = (System.currentTimeMillis() - start);
         logStringBuilder.append(diff);
         logStringBuilder.append(" : ");
         
         logStringBuilder.append(logtext);
         logStringBuilder.append("\n");
         
         // display message
         if (guiStructure != null) {
             guiStructure.txtDebug.setText(logStringBuilder.toString());
         }
         else {
             // txtDebug not created
             // but let's at least display the message in the statusbar
             // (does not work in Firefox according to documentation)
             Window.setStatus(logtext);
         }
         // also log to development console
         GWT.log(logtext);*/
     }
     
     /**
      * Log the message to server (if its level is high enough)
      * and also log it to the debug window.
      * @param level
      * @param context
      * @param message
      */
     public static void log(LevelLogEnum level, String context, String message) {
    	 log(context + ":\n" + message);
    	 new LogGuiMessage(level, context, message);
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
		
		sb.append(e.getLocalizedMessage());
		sb.append("\n");
		
    	if (e instanceof UmbrellaException) {
    		Set<Throwable> causes = ((UmbrellaException)e).getCauses();
        	sb.append("Caused by " + causes.size() + " exceptions:\n");
    		for (Throwable cause : causes) {
				sb.append(exceptionCatcher(cause, false, false));
			}
    	}
    	else {
	    	// exception stacktrace
			StackTraceElement[] st = e.getStackTrace();
			for (StackTraceElement stackTraceElement : st) {
		    	sb.append(stackTraceElement);
		    	sb.append('\n');
			}
    	}
    	
		String result = sb.toString();
		
		if (logIt) {
	    	// log exception name, message and stacktrace
			log(LevelLogEnum.Error, e.getClass().getName(), result);
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
    
	/**
	 * To be called when the User object
	 * representing the current user
	 * has changed.
	 */
    public static void resetUser (User user) {
    	Gui.user = user;
    	Gui.getGuiStructure().logged_in(user);
    }

    public static void logged_in (User user) {
    	Gui.log("User " + user.getName() + " is logged in.");
    	// login state fields
    	Gui.loggedIn = true;
    	Gui.user = user;
        // actions
    	Gui.getPageHandler().forgetCurrentDocumentCreator();
    	Gui.getGuiStructure().logged_in(user);
    	Gui.pageHandler.loadPage();
    	Callable.callCallsToBeCalled();
    	LocalStorageHandler.setOnline(true);
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
    	Gui.log("User is logged out.");
    	// login state fields
    	Gui.loggedIn = false;
    	Gui.user = null;
    	Gui.setSessionID(null);
        // actions
    	Gui.getPageHandler().forgetCurrentDocumentCreator();
        Gui.getGuiStructure().logged_out();
        if (goToWelcomeScreen) {
        	Gui.pageHandler.loadPage(Page.WelcomeScreen);
        	Gui.pageHandler.refresh();
        } else {
        	Gui.pageHandler.loadPage(true);
        }
    }


    
}

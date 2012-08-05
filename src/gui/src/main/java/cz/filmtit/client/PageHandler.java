package cz.filmtit.client;

import java.util.Iterator;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

import cz.filmtit.share.Document;



/**
 * Handles loading and switching of pages.
 * @author rur
 *
 */
public class PageHandler {

	/**
     * The page parameter from the URL.
     */
    private Page pageUrl;
    
    /**
     * The page that should be loaded.
     */
    private Page pageToLoad = Page.None;
    
    /**
     * The page currently loaded.
     */
    private Page pageLoaded = Page.None;
    
    /**
     * Provides access to the gui.
     */
    private Gui gui = Gui.getGui();

    /**
     * whether to try to log in the user by saved sessionID
     */
	public final boolean doCheckSessionID;    

    /**
     * Various pages to be set and created.
     * The 'None' page is used when no page is set.
     */
    public enum Page {
		None,
		Blank,
		WelcomeScreen,
		UserPage,
		ChangePassword,
		AuthenticationValidationWindow,
		About,
		TranslationWorkspace,
		DocumentCreator,
		Settings
    }
    
    public PageHandler () {
    	
    	History.addValueChangeHandler(historyChangeHandler);
    	
    	pageUrl = getPageFromURL();

    	if ( isFullPage(pageUrl) ) {
    		
            // base of GUI is created
    		gui.guiStructure = new GuiStructure();
            
    		// say what we got
        	gui.log("Parsed URL and identified page " + pageUrl);
        	
            // set documentId if it is provided
    		setDocumentIdFromGETOrCookie();
        	
    		// load a Blank page before checkSessionId returns
    		loadBlankPage();
            
    		doCheckSessionID = true;
    		
    	} else {
    		
    		// do not do any funny stuff, just load the page
    		loadPage();
    		
    		doCheckSessionID = false;
    		
    	}
    }
    
    /**
     * Deterimines whether page is a full page with menu etc.
     * @param page
     * @return
     */
    private boolean isFullPage(Page page) {
    	return (page != Page.AuthenticationValidationWindow);
	}

    /**
     * Get the page requested in URL.
     * Supports both ?page=Page and #Page
     * @return the page requested if possible, or Page.None
     */
	private Page getPageFromURL () {
    	// first try #Page
    	Page page = string2page(History.getToken());
    	if (page == Page.None) {
    		// also try ?page=Page
    		page = string2page(Window.Location.getParameter("page"));
    	}
    	return page;
    }
    
	/**
     * Converts String to Page.
     * Does not propagate any exceptions.
     * @param pageString
     * @return Page.* (Page.None if string is not a page)
     */
    private Page string2page (String pageString) {
		if (pageString == null || pageString.isEmpty()) {
			// avoid exceptions
			return Page.None;
		}
		else {
			try {
				return Page.valueOf(pageString);				
			}
			catch (IllegalArgumentException e) {
				gui.log("WARNING: page name " + pageString + " is not valid!");
				return Page.None;				
			}
		}    	
    }
    
    /**
     * Determines the page to be loaded and loads it
     * (unless it is already loaded).
     * Equivalent to:
     *   setPageUrl(suggestedPage);
     *   loadPage();
     * @param suggestedPage page that should be loaded
     */
	public void loadPage(Page suggestedPage) {
		History.newItem(suggestedPage.toString());
		// invokes the historyChangeHandler
			// sets pageUrl
			// calls loadPage();
	}
	
    /**
     * Reacts to user clicking the links in menu.
     */
	private ValueChangeHandler<String> historyChangeHandler = new ValueChangeHandler<String>() {
		
		@Override
		public void onValueChange(ValueChangeEvent<String> event) {
			// find out which page the user wants
			pageUrl = string2page(event.getValue());
			// load the page
			loadPage();
		}
	};
    
	/**
	 * loads a blank page without modifying the history
	 */
    public void loadBlankPage() {
    	setPageToLoad(Page.Blank);
    	loadPageToLoad();
	}

    /**
     * Determines the page to be loaded and loads it
     * (unless it is already loaded),
     * using the page set in the URL
     * in GET parameter "page".
     */
	public void loadPage() {
		setPageToLoad();
		loadPageToLoad();
	}
	
    /**
     * Sets page to be loaded,
     * changing the URL
     * but not loading the page at the moment;
     * the page can be loaded later by calling loadPage().
     * @param pageUrl the page to be loaded
     */
	public void setPageUrl(Page pageUrl) {
		this.pageUrl = pageUrl;
		History.newItem(pageUrl.toString(), false);
    }
    
    /**
     * Determines the page to be loaded,
     * using the page set in the URL
     * in GET parameter "page".
     * @param loggedIn whether the user is logged in
     */
	private void setPageToLoad() {
		setPageToLoad(pageUrl);
    }
    
    /**
     * Determines the page to be loaded.
     * @param loggedIn whether the user is logged in
     * @param suggestedPage the page that should be preferably loaded if possible
     */
    private void setPageToLoad(Page suggestedPage) {
    	
    	switch (suggestedPage) {
	
    	// pageUrl no matter whether user is logged in or not
    	case Blank:
    	case ChangePassword:
    	case AuthenticationValidationWindow:
		case About:
			pageToLoad = suggestedPage;
			break;
			
		// pageUrl only if user is logged in, otherwise WelcomeScreen
		case TranslationWorkspace:
		case DocumentCreator:
		case Settings:
			pageToLoad = gui.loggedIn ? suggestedPage : Page.WelcomeScreen;
			break;

		// all other situations: UserPage or WelcomeScreen
		default:
			pageToLoad = gui.loggedIn ? Page.UserPage : Page.WelcomeScreen;
			break;
			
		}
    	
		gui.log("Page to load set to " + pageToLoad);
    }

    /**
     * Loads the pageToLoad unless it is already loaded.
     */
	private void loadPageToLoad() {
		if (pageToLoad != pageLoaded) {
			
			if (pageLoaded == Page.TranslationWorkspace) {
				// unloading TranslationWorkspace
				gui.currentWorkspace.setStopLoading(true);
			}
			
	    	switch (pageToLoad) {
	    	
	    	case Blank:
	    		new Blank();
				break;
	    	case ChangePassword:
	    		new ChangePassword();
				break;
	    	case AuthenticationValidationWindow:
	    		new AuthenticationValidationWindow();
				break;
			case About:
	    		new About();
				break;
			case TranslationWorkspace:
		    	if (documentId == -1) {
		    		loadPage(Page.UserPage);
					gui.log("failure on loading document: documentId -1 is not valid!");
					Window.alert("Cannnot load document - document ID (-1) is not valid!");
		    	} else {
					loadBlankPage();
		            gui.rpcHandler.loadDocumentFromDB(documentId);
		    	}
				break;
			case DocumentCreator:
				new DocumentCreator();
				break;
			case UserPage:
				new UserPage();
				break;				
			case WelcomeScreen:
				new WelcomeScreen();
				break;
			case Settings:
				new Settings();
				break;
	
			// no other situation should happen
			default:
				gui.log("ERROR: Cannot load the page " + pageToLoad);
				return;
	    	}

			gui.log("Loaded page " + pageToLoad);
	    	pageLoaded = pageToLoad;
		}
		else {
			gui.log("Not loading page " + pageToLoad + " because it is already loaded.");
		}
	}

	
	// DOCUMENT ID HANDLING	(for TranslationWorkspace)
	
    /**
     * Used by TranslationWorkspace to load the correct document;
     * -1 used if not set.
     */
    private long documentId = -1;
    
    private static final String DOCUMENTID = "documentId";
    
    /**
     * load a documentId which is not set through a GET parameter;
     * saves it into a Cookie
     * @param id
     */
	public void setDocumentId(long id) {
		this.documentId = id;
		Cookies.setCookie(DOCUMENTID, Long.toString(id), Gui.getDateIn1Year());
	}
	
    /**
     * load a documentId set through a GET parameter or a Cookie
     * (GET parameter has priority over a Cookie)
     */
	private void setDocumentIdFromGETOrCookie() {
		setDocumentIdFromGET();
		if (documentId == -1) {
			setDocumentIdFromCookie();
		}
	}
    
    /**
     * load a documentId set through a GET parameter
     */
	private void setDocumentIdFromGET() {
		// get the GET parameter
		String id = Window.Location.getParameter(DOCUMENTID);
		// parse it
		setDocumentIdFromString(id);
	}
    
    /**
     * load a documentId set through a Cookie
     */
	private void setDocumentIdFromCookie() {
		// get the parameter
		String id = Cookies.getCookie(DOCUMENTID);
		// parse it
		setDocumentIdFromString(id);
	}
    
    /**
     * load a documentId set through a string
     * @param id
     */
	private void setDocumentIdFromString(String id) {
		if (id == null) {
			// this is OK, documentId parameter is not set
			documentId = -1;
		} else {
			try {
				documentId = Long.parseLong(id);
				gui.log("documentId (" + documentId + ") acquired from parameter");
			}
			catch (NumberFormatException e) {
				// this is not OK, documentId parameter is set but is invalid
				documentId = -1;
				gui.log("WARNING: invalid documentId (" + id + ") set as parameter!");
			}
		}
	}
    	
}

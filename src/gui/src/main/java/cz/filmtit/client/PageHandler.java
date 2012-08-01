package cz.filmtit.client;

import java.util.Iterator;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
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
    private Gui gui;
    
    private GuiStructure guiStructure;
    
    /**
     * Used by TranslationWorkspace to load the correct document;
     * -1 used if not set.
     */
    private long documentId = -1;
    
	public void setDocumentId(String id) {
		if (id == null) {
			// this is OK, documentId parameter is not set
			documentId = -1;
		} else {
			try {
				documentId = Long.parseLong(id);
				gui.log("documentId (" + documentId + ") acquired from GET parameter");
			}
			catch (NumberFormatException e) {
				// this is not OK, documentId parameter is set but is invalid
				documentId = -1;
				gui.log("WARNING: invalid documentId (" + id + ") set as GET parameter!");
			}
		}
		
	}
    
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
		DocumentCreator
    }
    
    public PageHandler (Gui gui) {
    	
    	this.gui = gui;
    	guiStructure = gui.guiStructure;
    	
    	History.addValueChangeHandler(historyChangeHandler);
    	
    	pageUrl = string2page(History.getToken());
		
        // base of GUI is created for every "full" window
    	if (pageUrl != Page.AuthenticationValidationWindow) {
    		gui.createGui();
    	}
		
		setDocumentId(Window.Location.getParameter("documentId"));
    	
		// load a Blank page before checkSessionId returns
		loadBlankPage();
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
			
	    	switch (pageToLoad) {
	    	
	    	case Blank:
	    		gui.showBlankPage();
				break;
	    	case ChangePassword:
	    		gui.showChangePasswordForm();
				break;
	    	case AuthenticationValidationWindow:
	    		gui.createAuthenticationValidationWindow();
				break;
			case About:
	    		gui.showAboutPage();
				break;
			case TranslationWorkspace:
		    	if (documentId == -1) {
		    		loadPage(Page.UserPage);
					gui.log("failure on loading document: documentId -1 is not valid!");
					Window.alert("Cannnot load document - document ID (-1) is not valid!");
		    	} else {
					loadBlankPage();
		    		gui.editDocument(documentId);
		    	}
				break;
			case DocumentCreator:
				gui.createNewDocumentCreator();
				break;
			case UserPage:
				gui.createAndLoadUserPage();
				break;				
			case WelcomeScreen:
				gui.showWelcomePage();
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
}

package cz.filmtit.client;

import com.google.gwt.user.client.Window;

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
		WelcomeScreen,
		UserPage,
		ChangePassword,
		AuthenticationValidationWindow,
		About,
		TranslationWorkspace,
		DocumentCreator
    }
    
    public PageHandler (String page, Gui gui) {
    	
    	this.gui = gui;
    	pageUrl = string2page(page);
    	
        // base of GUI is created for every "full" window
    	if (pageUrl != Page.AuthenticationValidationWindow) {
    		gui.createGui();
    	}
    	
    	setPageToLoad(false);
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
     * @param loggedIn whether the user is logged in
     */
	public void loadPage(boolean loggedIn) {
		setPageToLoad(loggedIn, pageUrl);
		loadPageToLoad();
	}
	
    /**
     * Determines the page to be loaded and loads it
     * (unless it is already loaded).
     * @param loggedIn whether the user is logged in
     * @param suggestedPage page that should be loaded
     */
	public void loadPage(boolean loggedIn, Page suggestedPage) {
		setPageToLoad(loggedIn, pageToLoad);
		loadPageToLoad();
	}
	
    /**
     * Determines the page to be loaded.
     * @param loggedIn whether the user is logged in
     */
    void setPageToLoad(boolean loggedIn) {
		setPageToLoad(loggedIn, pageUrl);
    }
    
    /**
     * Determines the page to be loaded.
     * @param loggedIn whether the user is logged in
     * @param suggestedPage the page that should be preferably loaded if possible
     */
    void setPageToLoad(boolean loggedIn, Page suggestedPage) {
    	
    	switch (suggestedPage) {
	
    	// pageUrl no matter whether user is logged in or not
    	case ChangePassword:
    	case AuthenticationValidationWindow:
		case About:
			pageToLoad = suggestedPage;
			break;
			
		// pageUrl only if user is logged in, otherwise WelcomeScreen
		case TranslationWorkspace:
		case DocumentCreator:
			pageToLoad = loggedIn ? suggestedPage : Page.WelcomeScreen;
			break;

		// all other situations: UserPage or WelcomeScreen
		default:
			pageToLoad = loggedIn ? Page.UserPage : Page.WelcomeScreen;
			break;
			
		}
    }

    /**
     * Loads the pageToLoad unless it is already loaded.
     */
	void loadPageToLoad() {
		if (pageToLoad != pageLoaded) {
			
	    	switch (pageToLoad) {
	    	
	    	case ChangePassword:
	    		// TODO show ChangePassword window
	    		Window.alert("TODO: show ChangePassword window");
				break;
	    	case AuthenticationValidationWindow:
	    		gui.createAuthenticationValidationWindow();
				break;
			case About:
				// TODO show About window
	    		Window.alert("TODO: show About window");
				break;
			case TranslationWorkspace:
		    	if (documentId == -1) {
					gui.log("failure on loading document: documentId -1 is not valid!");
					Window.alert("Cannnot load document - document ID (-1) is not valid!");
		    	} else {
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
		// else: pageToLoad already loaded
	}

}
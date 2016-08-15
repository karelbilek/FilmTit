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

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;

import cz.filmtit.client.callables.LoadDocumentFromDB;
import cz.filmtit.client.pages.AuthenticationValidationWindow;
import cz.filmtit.client.pages.Blank;
import cz.filmtit.client.pages.ChangePassword;
import cz.filmtit.client.pages.DocumentCreator;
import cz.filmtit.client.pages.Help;
import cz.filmtit.client.pages.PlayerInfo;
import cz.filmtit.client.pages.PlayerDemo;
import cz.filmtit.client.pages.Settings;
import cz.filmtit.client.pages.TranslationWorkspace;
import cz.filmtit.client.pages.UserPage;
import cz.filmtit.client.pages.WelcomeScreen;

/**
 * Handles loading and switching of pages.
 *
 * @author rur
 *
 */
public class PageHandler {

    /**
     * The page parameter from the URL.
     */
    private Page pageUrl;

    /**
     * The page that should be loaded. Is set only by setPageToLoad() because
     * some checks have to be done to determine which page can be safely loaded.
     */
    private Page pageToLoad = Page.None;

    /**
     * The page currently loaded.
     */
    private Page pageLoaded = Page.None;

    /**
     * whether to create the GuiStructure and try to log in the user by saved
     * sessionID
     */
    public final boolean fullInitialization;

    /**
     * Whether to scroll to the top of the page on loadPageToLoad(). Set to
     * false when the first page is loaded, so that the view stays the same if
     * user presses F5.
     */
    private boolean scrollToTop = true;

    /**
     * Get value of scrollToTop and reset it to the default value (true).
     *
     * @return
     */
    private boolean grabScrollToTop() {
        boolean result = scrollToTop;
        scrollToTop = true;
        return result;
    }

    /**
     * Various pages to be set and created. The 'None' page is used when no page
     * is set.
     */
    public enum Page {
        None,
        Blank,
        WelcomeScreen,
        UserPage,
        ChangePassword,
        AuthenticationValidationWindow,
        TranslationWorkspace,
        DocumentCreator,
        Settings,
        PlayerInfo,
        PlayerDemo,
        Help,
    }

    /**
     * Creates an instance of the PageHandler which starts reponding to page
     * swicth requests. It also retrieves the page requested in the Url and
     * loads it on its creation.
     */
    public PageHandler() {

        History.addValueChangeHandler(historyChangeHandler);

        getPageFromURL();

        if (isFullPage(pageUrl)) {

            // say what we got
            Gui.log("Parsed URL and identified page " + pageUrl);

            // set documentId if it is provided
            setDocumentIdFromGETOrCookie();

            scrollToTop = false;

            fullInitialization = true;

        } else {

            // do not do any funny stuff, just load the page
            loadPage();

            fullInitialization = false;

        }
    }

    /**
     * Deterimines whether page is a full page with menu etc.
     *
     * @param page
     * @return
     */
    private boolean isFullPage(Page page) {
        return (page != Page.AuthenticationValidationWindow);
    }

    // set pageUrl
    /**
     * Get the page requested in URL, setting pageUrl. Supports both ?page=Page
     * and #Page
     *
     * @return the page requested if possible, or Page.None
     */
    private void getPageFromURL() {
        // first try #Page
        Page page = string2page(History.getToken());
        if (page == Page.None) {
            // also try ?page=Page
            page = string2page(Window.Location.getParameter("page"));
        }
        pageUrl = page;
    }

    /**
     * Reacts to user going forward and backward.
     */
    private ValueChangeHandler<String> historyChangeHandler = new ValueChangeHandler<String>() {
        @Override
        public void onValueChange(ValueChangeEvent<String> event) {
            if (LocalStorageHandler.isOnline()) {
                // find out which page the user wants
                pageUrl = string2page(event.getValue());
                // load the page
                loadPage(true);
            } else {
                // do not switch pages in Offline Mode
                // keep the URL unchanged
                setPageUrl(pageLoaded);
            }
        }
    };

    /**
     * Converts String to Page. Does not propagate any exceptions.
     *
     * @param pageString
     * @return Page.* (Page.None if string is not a page)
     */
    private Page string2page(String pageString) {
        if (pageString == null || pageString.isEmpty()) {
            // avoid exceptions
            return Page.None;
        } else {
            try {
                return Page.valueOf(pageString);
            } catch (IllegalArgumentException e) {
                Gui.log("WARNING: page name " + pageString + " is not valid!");
                return Page.None;
            }
        }
    }

    /**
     * Sets page to be loaded, changing the URL but not loading the page at the
     * moment; the page can be loaded later by calling loadPage().
     *
     * @param pageUrl the page to be loaded
     */
    public void setPageUrl(Page pageUrl) {
        this.pageUrl = pageUrl;
        History.newItem(pageUrl.toString(), false);
    }

    /**
     * Determines the page to be loaded and loads it (unless it is already
     * loaded). Sets pageUrl. Equivalent to: setPageUrl(suggestedPage);
     * loadPage();
     *
     * @param suggestedPage page that should be loaded
     */
    public void loadPage(Page suggestedPage) {
        setPageUrl(suggestedPage);
        loadPage();
    }

    /**
     * Determines the page to be loaded and loads it. Sets pageUrl.
     *
     * @param suggestedPage page that should be loaded
     * @param evenIfAlreadyLoaded true to reload the page if it is already
     * loaded, false not to
     */
    public void loadPage(Page suggestedPage, boolean evenIfAlreadyLoaded) {
        setPageUrl(suggestedPage);
        loadPage(evenIfAlreadyLoaded);
    }

    // pageUrl -> suggestedPage
    /**
     * Determines the page to be loaded and loads it (unless it is already
     * loaded), using pageUrl (the page set in the URL in GET parameter "page").
     */
    public void loadPage() {
        loadPage(false);
    }

    /**
     * Determines the page to be loaded and loads it (unless it is already
     * loaded), using pageUrl (the page set in the URL in GET parameter "page").
     */
    public void loadPage(boolean evenIfAlreadyLoaded) {
        setPageToLoad();
        loadPageToLoad(evenIfAlreadyLoaded);
    }

    // set suggestedPage
    /**
     * Loads a blank page without modifying the history. (Does not set pageUrl,
     * preserves scrollToTop.)
     */
    public void loadBlankPage() {
        boolean scrollToTop = this.scrollToTop;
        setPageToLoad(Page.Blank);
        loadPageToLoad();
        this.scrollToTop = scrollToTop;
    }

    // suggestedPage -> pageToLoad
    /**
     * Determines the page to be loaded, using pageUrl (the page set in the URL
     * in GET parameter "page"). Sets pageToLoad.
     */
    private void setPageToLoad() {
        setPageToLoad(pageUrl);
    }

    /**
     * Determines the page to be loaded, sets pageToLoad.
     *
     * @param suggestedPage the page that should be preferably loaded if
     * possible
     */
    private void setPageToLoad(Page suggestedPage) {

        switch (suggestedPage) {

            // pageUrl no matter whether user is logged in or not
            case Blank:
            case ChangePassword:
            case AuthenticationValidationWindow:
            case PlayerInfo:
            case PlayerDemo:
            case Help:

                pageToLoad = suggestedPage;
                break;

            // pageUrl only if user is logged in, otherwise WelcomeScreen
            case TranslationWorkspace:
            case DocumentCreator:
            case Settings:
                pageToLoad = Gui.isLoggedIn() ? suggestedPage : Page.WelcomeScreen;
                break;

            // all other situations: UserPage or WelcomeScreen
            default:
                pageToLoad = Gui.isLoggedIn() ? Page.UserPage : Page.WelcomeScreen;
                break;

        }

        Gui.log("Page to load set to " + pageToLoad);
    }

    // pageToLoad -> pageLoaded    
    /**
     * Loads the pageToLoad unless it is already loaded. Sets pageLoaded.
     */
    private void loadPageToLoad() {
        loadPageToLoad(false);
    }

    /**
     * Loads the pageToLoad. Sets pageLoaded. Uses
     *
     * @param evenIfAlreadyLoaded reload the page if already loaded
     */
    private void loadPageToLoad(boolean evenIfAlreadyLoaded) {
        if (pageToLoad != pageLoaded || evenIfAlreadyLoaded) {

            // unloading TranslationWorkspace
            if (pageLoaded == Page.TranslationWorkspace && TranslationWorkspace.getCurrentWorkspace() != null) {
                TranslationWorkspace.getCurrentWorkspace().setStopLoading(true);
            }

            // scroll to top if not prevented
            if (grabScrollToTop()) {
                Window.scrollTo(Window.getScrollLeft(), 0);
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
                case PlayerDemo:
                    new PlayerDemo();
                    break;
                case PlayerInfo:
                    new PlayerInfo();
                    break;
                case TranslationWorkspace:
                    if (documentId == -1) {
                        loadPage(Page.UserPage);
                        Gui.log("failure on loading document: documentId -1 is not valid!");
                    } else {
                        new LoadDocumentFromDB(documentId);
                    }
                    break;
                case DocumentCreator:
                    reshowCurrentDocumentCreator();
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
                case Help:
                    new Help();
                    break;

                // no other situation should happen
                default:
                    Gui.log("ERROR: Cannot load the page " + pageToLoad);
                    return;
            }

            Gui.log("Loaded page " + pageToLoad);
            pageLoaded = pageToLoad;

            if (isFullPage(pageLoaded)) {
                // set the correct menu item
                Gui.getGuiStructure().activateMenuItem(pageLoaded);
            }
        } else {
            Gui.log("Not loading page " + pageToLoad + " because it is already loaded.");
        }
    }

    /**
     * Reload the current page. If user is in Offline Mode, this method does
     * nothing.
     */
    public void refresh() {
        if (LocalStorageHandler.isOnline()) {
            setPageToLoad(pageLoaded);
            loadPageToLoad(true);
        }
    }

    /**
     * Reload the current page if and only if it is the page given.
     */
    public void refreshIf(Page page) {
        if (pageLoaded == page) {
            refresh();
        }
    }

    // DOCUMENT ID HANDLING	(for TranslationWorkspace)
    /**
     * Used by TranslationWorkspace to load the correct document; -1 used if not
     * set.
     */
    private long documentId = -1;

    private static final String DOCUMENTID = "documentId";

    /**
     * load a documentId which is not set through a GET parameter; saves it into
     * a Cookie
     *
     * @param id
     */
    public void setDocumentId(long id) {
        this.documentId = id;
        Cookies.setCookie(DOCUMENTID, Long.toString(id), Gui.getDateIn1Year());
    }

    /**
     * load a documentId set through a GET parameter or a Cookie (GET parameter
     * has priority over a Cookie)
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
     *
     * @param id
     */
    private void setDocumentIdFromString(String id) {
        if (id == null) {
            // this is OK, documentId parameter is not set
            documentId = -1;
        } else {
            try {
                documentId = Long.parseLong(id);
                Gui.log("documentId (" + documentId + ") acquired from parameter");
            } catch (NumberFormatException e) {
                // this is not OK, documentId parameter is set but is invalid
                documentId = -1;
                Gui.log("WARNING: invalid documentId (" + id + ") set as parameter!");
            }
        }
    }

    private DocumentCreator currentDocumentCreator;

    /**
     * remembers the current document creator
     */
    public void setCurrentDocumentCreator(DocumentCreator documentCreator) {
        currentDocumentCreator = documentCreator;
    }

    /**
     * forgets the current document creator
     */
    public void forgetCurrentDocumentCreator() {
        currentDocumentCreator = null;
    }

    /**
     * shows again the current document creator, or shows a new document creator
     * if there is none remembered
     */
    public void reshowCurrentDocumentCreator() {
        if (currentDocumentCreator != null) {
            Gui.getPageHandler().setPageUrl(Page.DocumentCreator);
            Gui.getGuiStructure().showPageInPanel(currentDocumentCreator);
        } else {
            new DocumentCreator();
        }
    }

}

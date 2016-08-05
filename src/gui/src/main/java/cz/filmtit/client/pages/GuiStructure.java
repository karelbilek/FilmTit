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

package cz.filmtit.client.pages;


import com.github.gwtbootstrap.client.ui.Brand;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.NavText;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

import cz.filmtit.client.Gui;
import cz.filmtit.client.PageHandler.Page;
import cz.filmtit.client.callables.Logout;
import cz.filmtit.client.dialogs.LoginDialog;
import cz.filmtit.share.User;

/**
 * Defines the content panel, into which the individual pages are loaded, and the top menu, which enables the users to log in and out and to switch pages.
 * @author rur
 *
 */
public class GuiStructure extends Composite {
	
	private static GuiStructureUiBinder uiBinder = GWT.create(GuiStructureUiBinder.class);


	interface GuiStructureUiBinder extends UiBinder<Widget, GuiStructure> {
	}

	/**
	 * Initializes the Gui structure.
	 */
	public GuiStructure() {
		initWidget(uiBinder.createAndBindUi(this));
		
		allMenuItems = new NavLink[]{ documentCreator, welcomeScreen, userPage, settings, help };
		
		documentCreator.addClickHandler(new MenuClickHandler(Page.DocumentCreator));
		welcomeScreen.addClickHandler(new MenuClickHandler(Page.WelcomeScreen));
		userPage.addClickHandler(new MenuClickHandler(Page.UserPage));
		settings.addClickHandler(new MenuClickHandler(Page.Settings));
		
        // top menu handlers
        login.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
            	 new LoginDialog();
             }
        });

        // top menu handlers
        logout.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
            	 new Logout();
             }
        });

		// the default layout is the logged out one
		logged_out();
		
		RootPanel rootPanel = RootPanel.get();
        rootPanel.add(this, 0, 0);

	}
	
	/**
	 * Displays the given page in the content panel
	 * @param page
	 */
	public void showPageInPanel(Widget page) {
		contentPanel.setWidget(page);
	}
	
    ///////////////////////////////////////
    //                                   //
    //      Public state change methods  //
    //                                   //
    ///////////////////////////////////////
	
	/**
	 * to be called when switching pages
	 */
	public void activateMenuItem(Page pageLoaded) {
		deactivateAllMenuItems();
		NavLink activeMenuItem = page2menuItem(pageLoaded);
		if (activeMenuItem != null) {
			activateMenuItem(activeMenuItem);			
		}
	}
	
	/**
	 * To be called when the User object
	 * representing the current user
	 * has changed.
	 */
	public void resetUser (User user) {
		logout.setText("Log out user " + user.getName());
		username.setText("User " + user.getName());
	}
	
	/**
	 * to be called to switch the view from "logged out" to "logged in"
	 */
	public void logged_in (User user) {
		// login/logout link
		login.setVisible(false);
		logout.setText("Log out user " + user.getName());
		logout.setVisible(true);
		username.setText("User " + user.getName());
		
		// visibility
		welcomeScreen.setVisible(false);
		userPage.setVisible(true);
		documentCreator.setVisible(true);
		settings.setVisible(true);
		help.setVisible(true);
		
		brand.setVisible(true);
		brandOffline.setVisible(false);
		offlineHint.setVisible(false);
		username.setVisible(false);
	}
	
	/**
	 * to be called to switch the view from "logged in" to "logged out"
	 */
	public void logged_out () {
		// login/logout link
		logout.setVisible(false);
		login.setVisible(true);
		// visibility
		welcomeScreen.setVisible(true);
		userPage.setVisible(false);
		documentCreator.setVisible(false);
		settings.setVisible(false);
		help.setVisible(true);

		brand.setVisible(true);
		brandOffline.setVisible(false);
		offlineHint.setVisible(false);
		username.setVisible(false);
	}
	
	/**
	 * To be called when user enters the Offline Mode.
	 */
	public void offline_mode () {
		// hide all links - they cannot be clicked anyway
		logout.setVisible(false);
		login.setVisible(false);
		welcomeScreen.setVisible(false);
		userPage.setVisible(false);
		documentCreator.setVisible(false);
		settings.setVisible(false);
		help.setVisible(false);
		
		// show Offline Mode texts
		brand.setVisible(false);
		brandOffline.setVisible(true);
		offlineHint.setVisible(true);
		username.setVisible(true);
	}
	
    ///////////////////////////////////////
    //                                   //
    //      The pages in menu            //
    //                                   //
    ///////////////////////////////////////

	
	@UiField
	NavLink welcomeScreen;

	@UiField
	NavLink userPage;

	@UiField
	NavLink documentCreator;

	@UiField
	NavLink settings;

	@UiField
	NavLink help;
	
	/**
	 * Reacts to clicks on NavLinks.
	 * Used instead of the Hyperlinks
	 * because the Hyperlink handler does not fire
	 * if the new page URL is the same as the old one.
	 */
	private class MenuClickHandler implements ClickHandler {

		Page pageUrl;
		
		private MenuClickHandler(Page pageUrl) {
			super();
			this.pageUrl = pageUrl;
		}

		@Override
		public void onClick(ClickEvent event) {
			if (pageUrl == Page.DocumentCreator) {
				// loading a new DocumentCreator
				Gui.getPageHandler().forgetCurrentDocumentCreator();
			}
			Gui.getPageHandler().loadPage(pageUrl, true);
		}
		
	}

	NavLink[] allMenuItems;

	private NavLink page2menuItem (Page page) {
		switch (page) {
		case WelcomeScreen:
			return welcomeScreen;
		case UserPage:
			return userPage;
		case DocumentCreator:
			return documentCreator;
		case Settings:
			return settings;
		case Help:
			return help;
		default:
			return null;
		}
	}
	
	private void deactivateAllMenuItems() {
        for (NavLink item : allMenuItems) {
            item.setActive(false);
	    }
    }

	private void activateMenuItem(NavLink menuItem) {
	    menuItem.setActive(true);
    }

    ///////////////////////////////////////
    //                                   //
    //      Other things                 //
    //                                   //
    ///////////////////////////////////////
	
	@UiField
	Brand brand;
	
	@UiField
	Brand brandOffline;
	
	@UiField
	NavText offlineHint;
	
	@UiField
	NavLink login;

	@UiField
	NavLink logout;

	// TODO
	@UiField
	NavLink online;

	@UiField
	NavText username;

	@UiField
    ScrollPanel contentPanel;
	
	//@UiField
	//public TextArea txtDebug;
	
    @UiField
	HTMLPanel panelForVLC;
    
    @UiField
        HTMLPanel panelForVideo;

    /**
     * Returns the panel in which the VLC player is to be shown.
     */
    public HTMLPanel getPanelForVLC() {
        return panelForVLC;
    }
    
       /**
     * @return the panelForVideo
     */
    public HTMLPanel getPanelForVideo() {
        return panelForVideo;
    }
    
    
}

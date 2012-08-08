package cz.filmtit.client.pages;


import com.github.gwtbootstrap.client.ui.TextArea;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.github.gwtbootstrap.client.ui.*;

import cz.filmtit.client.FilmTitServiceHandler;
import cz.filmtit.client.Gui;
import cz.filmtit.client.PageHandler.Page;


public class GuiStructure extends Composite {
	
	private static GuiStructureUiBinder uiBinder = GWT.create(GuiStructureUiBinder.class);

	interface GuiStructureUiBinder extends UiBinder<Widget, GuiStructure> {
	}

	public GuiStructure() {
		initWidget(uiBinder.createAndBindUi(this));
		
		allMenuItems = new Hyperlink[]{ documentCreator, about, welcomePage, userPage };
		
        // top menu handlers
        login.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
            	 Gui.getGui().showLoginDialog();
             }
        });

        // top menu handlers
        logout.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
            	 FilmTitServiceHandler.logout();
             }
        });

		// the default layout is the logged out one
		logged_out();
		
		RootPanel rootPanel = RootPanel.get();
        rootPanel.add(this, 0, 0);

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
		Hyperlink activeMenuItem = page2menuItem(pageLoaded);
		if (activeMenuItem != null) {
			activateMenuItem(activeMenuItem);			
		}
	}
	
	/**
	 * to be called to switch the view from "logged out" to "logged in"
	 */
	public void logged_in (String username) {
		// login/logout link
		login.setVisible(false);
		logout.setText("Log out user " + username);
		logout.setVisible(true);
		// visibility
		welcomePage.setVisible(false);
		userPage.setVisible(true);
		documentCreator.setVisible(true);
		// about.setVisible(true);
	}
	
	/**
	 * to be called to switch the view from "logged in" to "logged out"
	 */
	public void logged_out () {
		// login/logout link
		logout.setVisible(false);
		login.setVisible(true);
		// visibility
		welcomePage.setVisible(true);
		userPage.setVisible(false);
		documentCreator.setVisible(false);
		// about.setVisible(true);
	}
	
    ///////////////////////////////////////
    //                                   //
    //      The pages in menu            //
    //                                   //
    ///////////////////////////////////////
	
	@UiField
	Hyperlink welcomePage;

	@UiField
	Hyperlink userPage;

	@UiField
	Hyperlink documentCreator;

	@UiField
	Hyperlink about;

	Hyperlink[] allMenuItems;

	private Hyperlink page2menuItem (Page page) {
		switch (page) {
		case About:
			return about;
		case WelcomeScreen:
			return welcomePage;
		case UserPage:
			return userPage;
		case DocumentCreator:
			return documentCreator;
		default:
			return null;
		}
	}
	
	static final String ACTIVE = "active";
	
	private void deactivateAllMenuItems() {
        for (Hyperlink item : allMenuItems) {
            item.removeStyleName(ACTIVE);
	    }
    }

	private void activateMenuItem(Hyperlink menuItem) {
	    menuItem.addStyleName(ACTIVE);
    }

    ///////////////////////////////////////
    //                                   //
    //      Other things                 //
    //                                   //
    ///////////////////////////////////////
	
	@UiField
	NavLink login;

	@UiField
	NavLink logout;

	@UiField
    ScrollPanel contentPanel;
	
	@UiField
	public TextArea txtDebug;
	
}

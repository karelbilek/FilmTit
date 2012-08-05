package cz.filmtit.client.pages;


import com.github.gwtbootstrap.client.ui.TextArea;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.github.gwtbootstrap.client.ui.*;

import cz.filmtit.client.Gui;


public class GuiStructure extends Composite {
	
	private static GuiStructureUiBinder uiBinder = GWT.create(GuiStructureUiBinder.class);

	interface GuiStructureUiBinder extends UiBinder<Widget, GuiStructure> {
	}

	public GuiStructure() {
		initWidget(uiBinder.createAndBindUi(this));
		
        // top menu handlers
        login.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
            	 Gui.getGui().showLoginDialog();
             }
        });

        // top menu handlers
        logout.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
            	 Gui.getGui().rpcHandler.logout();
             }
        });

		// the default layout is the logged out one
		logged_out();
		
		RootPanel rootPanel = RootPanel.get();
        rootPanel.add(this, 0, 0);

        allMenuItems = new Hyperlink[]{ documentCreator, about, welcomePage, userPage };
		
	}
	
	Hyperlink[] allMenuItems;

	static final String ACTIVE = "active";
	
	public void activateMenuItem(Hyperlink menuItem) {
        for (Hyperlink item : allMenuItems) {
            item.removeStyleName(ACTIVE);
	    }
	    menuItem.addStyleName(ACTIVE);
    }

	@UiField
	Hyperlink welcomePage;

	@UiField
	Hyperlink userPage;

	@UiField
	Hyperlink documentCreator;

	@UiField
	Hyperlink about;

	@UiField
	NavLink login;

	@UiField
	NavLink logout;

	@UiField
    ScrollPanel contentPanel;
	
	@UiField
	public TextArea txtDebug;
	
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
}

package cz.filmtit.client;


import com.github.gwtbootstrap.client.ui.TextArea;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

import com.github.gwtbootstrap.client.ui.*;


public class GuiStructure extends Composite {
	
	private static GuiStructureUiBinder uiBinder = GWT.create(GuiStructureUiBinder.class);

	interface GuiStructureUiBinder extends UiBinder<Widget, GuiStructure> {
	}

	public GuiStructure(final Gui gui) {
		initWidget(uiBinder.createAndBindUi(this));
		
        // top menu handlers
        login.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                  if (gui.loggedIn) {
                	  gui.rpcHandler.logout();
                  } else {
                      gui.showLoginDialog();
                  }
             }
        });

       welcomePage.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
            	gui.showWelcomePage();
            }
       });

       userPage.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
            	 gui.createAndLoadUserPage();
             }
        });

       about.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
            	 gui.showAboutPage();
             }
        });

       documentCreator.addClickHandler(new ClickHandler() {
              public void onClick(ClickEvent event) {
            	  gui.createNewDocumentCreator();
              }
         });
       
		// the default layout is the logged out one
		logged_out();
		
		
        gui.rootPanel = RootPanel.get();
        gui.rootPanel.add(this, 0, 0);

        allMenuItems = new NavWidget[]{ documentCreator, about, welcomePage, userPage };
		
	}
	
    NavWidget[] allMenuItems;

    public void activateMenuItem(NavWidget menuItem) {

        for (NavWidget item : allMenuItems) {
            item.setActive(false);
        }
        menuItem.setActive(true);

    }

	@UiField
	NavLink welcomePage;

	@UiField
	NavLink userPage;

	@UiField
	NavLink documentCreator;

	@UiField
	NavLink about;

	@UiField
	NavLink login;

	@UiField
    ScrollPanel contentPanel;
	
	@UiField
	TextArea txtDebug;
	
	public void logged_in (String username) {
		// login/logout link
		login.setText("Log out user " + username);
		// visibility
		welcomePage.setVisible(false);
		userPage.setVisible(true);
		documentCreator.setVisible(true);
		// about.setVisible(true);
	}
	
	public void logged_out () {
		// login/logout link
		login.setText("Log in");
		// visibility
		welcomePage.setVisible(true);
		userPage.setVisible(false);
		documentCreator.setVisible(false);
		// about.setVisible(true);
	}
}

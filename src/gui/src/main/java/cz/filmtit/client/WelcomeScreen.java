package cz.filmtit.client;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import cz.filmtit.client.PageHandler.Page;

public class WelcomeScreen extends Composite {

	private static WelcomeScreenUiBinder uiBinder = GWT
			.create(WelcomeScreenUiBinder.class);

	interface WelcomeScreenUiBinder extends UiBinder<Widget, WelcomeScreen> {
	}

	private Gui gui = Gui.getGui();
	
	public WelcomeScreen() {
		initWidget(uiBinder.createAndBindUi(this));
		
		gui.guiStructure.activateMenuItem(gui.guiStructure.welcomePage);

        login.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (gui.loggedIn) {
                	gui.rpcHandler.logout();
                } else {
                	gui.showLoginDialog();
                }
            }
        });
        register.addClickHandler( new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                gui.showRegistrationForm();
            }
        });

        about.addClickHandler( new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            	gui.pageHandler.loadPage(Page.About);
            }
        });

        gui.guiStructure.contentPanel.setStyleName("welcoming");
        gui.guiStructure.contentPanel.setWidget(this);
	}


    @UiField
    Button login;

    @UiField
    Button register;

    @UiField
    Button about;

}

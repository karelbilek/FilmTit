package cz.filmtit.client.pages;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import cz.filmtit.client.Gui;
import cz.filmtit.client.PageHandler.Page;
import cz.filmtit.client.dialogs.LoginDialog;
import cz.filmtit.client.dialogs.LoginDialog.Tab;
import cz.filmtit.client.callables.Logout;

/**
 * A page displayed as the first page of the application
 * @author rur
 *
 */
public class WelcomeScreen extends Composite {

	private static WelcomeScreenUiBinder uiBinder = GWT
			.create(WelcomeScreenUiBinder.class);

	interface WelcomeScreenUiBinder extends UiBinder<Widget, WelcomeScreen> {
	}

	/**
	 * Shows the page.
	 */
	public WelcomeScreen() {
		initWidget(uiBinder.createAndBindUi(this));
		
        login.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (Gui.isLoggedIn()) {
                	new Logout();
                } else {
                	new LoginDialog();
                }
            }
        });
        register.addClickHandler( new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new LoginDialog(Tab.Register);
            }
        });

        about.addClickHandler( new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            	Gui.getPageHandler().loadPage(Page.Help);
            }
        });

        Gui.getGuiStructure().contentPanel.setStyleName("welcoming");
        Gui.getGuiStructure().contentPanel.setWidget(this);
	}


    @UiField
    Button login;

    @UiField
    Button register;

    @UiField
    Button about;

}

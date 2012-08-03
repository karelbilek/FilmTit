package cz.filmtit.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Paragraph;

public class AuthenticationValidationWindow extends Composite {

	private static AuthenticationValidationWindowUiBinder uiBinder = GWT
			.create(AuthenticationValidationWindowUiBinder.class);

	interface AuthenticationValidationWindowUiBinder extends
			UiBinder<Widget, AuthenticationValidationWindow> {
	}

	public AuthenticationValidationWindow(final Gui gui) {
		initWidget(uiBinder.createAndBindUi(this));

        // ----------------------------------------------- //
        // --- AuthenticationValidationWindow creation --- //
        // ----------------------------------------------- //

		RootPanel rootPanel = RootPanel.get();
		rootPanel.add(this, 0, 0);
		
      btnCancel.addClickHandler( new ClickHandler() {
             @Override
             public void onClick(ClickEvent event) {
                  // TODO: say to the UserSpace that I am closing the window
              close();
             }
        });

        // get authentication data
      paraValidation.setText("Processing authentication data...");
        // response URL
        String responseURL = Window.Location.getQueryString();
        // String responseURL = Window.Location.getParameter("responseURL");
        // auhID
        long authID = 0;
        String authIDstring = Window.Location.getParameter("authID");
        try {
             authID = Long.parseLong(authIDstring);
        }
        catch (Exception e) {
             // TODO: handle exception
             Window.alert("Cannot parse authID " + authIDstring + " as a number! " + e.getLocalizedMessage());
        }

        // send RPC
        paraValidation.setText("Validating authentication data for authID " + authID + "...");
        gui.rpcHandler.validateAuthentication (responseURL, authID, this);
    }
	
	/**
	 * strangely enough, there is no Window.close() in GWT
	 */
	native public void close()/*-{
		$wnd.close();
		// self.close();
	}-*/;

    @UiField
	Paragraph paraValidation;
	
    @UiField
	Button btnCancel;
}

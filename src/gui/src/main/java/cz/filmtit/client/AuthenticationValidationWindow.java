package cz.filmtit.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Paragraph;

public class AuthenticationValidationWindow extends Composite {

	private static AuthenticationValidationWindowUiBinder uiBinder = GWT
			.create(AuthenticationValidationWindowUiBinder.class);

	interface AuthenticationValidationWindowUiBinder extends
			UiBinder<Widget, AuthenticationValidationWindow> {
	}

	public AuthenticationValidationWindow() {
		initWidget(uiBinder.createAndBindUi(this));
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

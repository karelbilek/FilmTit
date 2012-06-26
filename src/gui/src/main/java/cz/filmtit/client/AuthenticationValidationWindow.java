package cz.filmtit.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class AuthenticationValidationWindow extends Composite {

	private static AuthenticationValidationWindowUiBinder uiBinder = GWT
			.create(AuthenticationValidationWindowUiBinder.class);

	interface AuthenticationValidationWindowUiBinder extends
			UiBinder<Widget, AuthenticationValidationWindow> {
	}

	public AuthenticationValidationWindow() {
		initWidget(uiBinder.createAndBindUi(this));
	}

}

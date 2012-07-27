package cz.filmtit.client;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class WelcomeScreen extends Composite {

	private static WelcomeScreenUiBinder uiBinder = GWT
			.create(WelcomeScreenUiBinder.class);

	interface WelcomeScreenUiBinder extends UiBinder<Widget, WelcomeScreen> {
	}

	public WelcomeScreen() {
		initWidget(uiBinder.createAndBindUi(this));
	}


    @UiField
    Button login;

    @UiField
    Button register;


}

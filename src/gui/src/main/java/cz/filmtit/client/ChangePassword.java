package cz.filmtit.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class ChangePassword extends Composite {

	private static ChangePasswordUiBinder uiBinder = GWT
			.create(ChangePasswordUiBinder.class);

	interface ChangePasswordUiBinder extends UiBinder<Widget, ChangePassword> {
	}

	public ChangePassword() {
		initWidget(uiBinder.createAndBindUi(this));
	}

}

package cz.filmtit.client;

import com.github.gwtbootstrap.client.ui.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TabBar;
import com.google.gwt.user.client.ui.Widget;
import org.vectomatic.file.FileUploadExt;

public class UserPage extends Composite {

	private static UserPageUiBinder uiBinder = GWT
			.create(UserPageUiBinder.class);

	interface UserPageUiBinder extends UiBinder<Widget, UserPage> {
	}

	public UserPage() {
		initWidget(uiBinder.createAndBindUi(this));

	}

    @UiField
    TabPanel tabPanel;

    @UiField
    Tab tabDocumentList;

    @UiField
    Tab tabNewDocument;

}

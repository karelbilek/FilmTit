package cz.filmtit.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class About extends Composite {

	private static AboutUiBinder uiBinder = GWT.create(AboutUiBinder.class);

	interface AboutUiBinder extends UiBinder<Widget, About> {
	}

	private Gui gui = Gui.getGui();
	
	public About() {
		initWidget(uiBinder.createAndBindUi(this));

		gui.guiStructure.activateMenuItem(gui.guiStructure.about);
		gui.guiStructure.contentPanel.setStyleName("about");
		gui.guiStructure.contentPanel.setWidget(this);
	}

}

package cz.filmtit.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class Settings extends Composite {

	private static SettingsUiBinder uiBinder = GWT
			.create(SettingsUiBinder.class);

	interface SettingsUiBinder extends UiBinder<Widget, Settings> {
	}

	public Settings(Gui gui) {
		initWidget(uiBinder.createAndBindUi(this));
		
		//gui.guiStructure.activateMenuItem(gui.guiStructure.documentCreator);
		gui.guiStructure.contentPanel.setStyleName("settings");
        gui.guiStructure.contentPanel.setWidget(this);
		
	}

}

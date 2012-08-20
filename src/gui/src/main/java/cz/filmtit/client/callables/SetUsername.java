package cz.filmtit.client.callables;

import cz.filmtit.client.Gui;
import cz.filmtit.client.pages.Settings;

/**
 * Change user's username.
 */
public class SetUsername extends SetSetting<String> {

	public SetUsername(String setting, Settings settingsPage) {
		super(setting, settingsPage);
	}
	
	@Override
	protected void call() {
		filmTitService.setUsername(Gui.getSessionID(), setting, this);
	}

}


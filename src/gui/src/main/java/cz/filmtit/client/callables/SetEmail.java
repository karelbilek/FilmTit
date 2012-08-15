package cz.filmtit.client.callables;

import cz.filmtit.client.Gui;
import cz.filmtit.client.pages.Settings;

/**
 * Change user's e-mail.
 */
public class SetEmail extends SetSetting<String> {

	public SetEmail(String setting, Settings settingsPage) {
		super(setting, settingsPage);
	}
	
	@Override
	protected void call() {
		filmTitService.setEmail(Gui.getSessionID(), setting, this);
	}

}


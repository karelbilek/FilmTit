package cz.filmtit.client.callables;

import cz.filmtit.client.Gui;
import cz.filmtit.client.pages.Settings;

/**
 * An ancestor to methods setting some settings.
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


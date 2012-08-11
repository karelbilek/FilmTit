package cz.filmtit.client.callables;

import cz.filmtit.client.Gui;
import cz.filmtit.client.pages.Settings;

/**
 * An ancestor to methods setting some settings.
 */
public class SetPermanentlyLoggedIn extends SetSetting<Boolean> {

	public SetPermanentlyLoggedIn(Boolean setting, Settings settingsPage) {
		super(setting, settingsPage);
	}

	@Override
	protected void call() {
		filmTitService.setPermanentlyLoggedIn(Gui.getSessionID(), setting, this);
	}

	@Override
	protected String getClassName() {
		return "SetPermanentlyLoggedIn";
	}

}


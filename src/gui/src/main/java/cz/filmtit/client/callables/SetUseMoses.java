package cz.filmtit.client.callables;

import cz.filmtit.client.Gui;
import cz.filmtit.client.pages.Settings;

/**
 * An ancestor to methods setting some settings.
 */
public class SetUseMoses extends SetSetting<Boolean> {

	public SetUseMoses(Boolean setting, Settings settingsPage) {
		super(setting, settingsPage);
	}

	@Override
	protected void call() {
		filmTitService.setUseMoses(Gui.getSessionID(), setting, this);
	}

}


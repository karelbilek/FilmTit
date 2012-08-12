package cz.filmtit.client.callables;

import cz.filmtit.client.Gui;
import cz.filmtit.client.pages.Settings;

/**
 * An ancestor to methods setting some settings.
 */
public class SetMaximumNumberOfSuggestions extends SetSetting<Integer> {

	public SetMaximumNumberOfSuggestions(Integer setting, Settings settingsPage) {
		super(setting, settingsPage);
	}

	@Override
	protected void call() {
		filmTitService.setMaximumNumberOfSuggestions(Gui.getSessionID(), setting, this);
	}

}


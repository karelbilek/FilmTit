package cz.filmtit.client.callables;

import cz.filmtit.client.Gui;
import cz.filmtit.client.pages.Settings;

/**
 * Set maximum number of suggestions to show for each line.
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


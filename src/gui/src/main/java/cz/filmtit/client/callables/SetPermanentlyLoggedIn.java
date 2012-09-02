package cz.filmtit.client.callables;

import cz.filmtit.client.Gui;
import cz.filmtit.client.pages.Settings;

/**
 * Stay logged in permanently (for 1 month) instead of 1 hour (sets the session timeout)
 */
public class SetPermanentlyLoggedIn extends SetSetting<Boolean> {

    /**
     * Stay logged in permanently (for 1 month) instead of 1 hour (sets the session timeout)
     * Does <b>not</b> enqueue the call immediately,
     * call enqueue() explicitly!
     */
	public SetPermanentlyLoggedIn(Boolean setting, Settings settingsPage) {
		super(setting, settingsPage);
	}

	@Override
	protected void call() {
		filmTitService.setPermanentlyLoggedIn(Gui.getSessionID(), setting, this);
	}

}


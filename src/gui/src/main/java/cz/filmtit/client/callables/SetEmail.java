package cz.filmtit.client.callables;

import cz.filmtit.client.Gui;
import cz.filmtit.client.pages.Settings;

/**
 * Change user's e-mail.
 */
public class SetEmail extends SetSetting<String> {

    /**
     * Change user's e-mail.
     * Does <b>not</b> enqueue the call immediately,
     * call enqueue() explicitly!
     */
	public SetEmail(String setting, Settings settingsPage) {
		super(setting, settingsPage);
	}
	
	@Override
	protected void call() {
		filmTitService.setEmail(Gui.getSessionID(), setting, this);
	}

}


package cz.filmtit.client.callables;

import cz.filmtit.client.Gui;
import cz.filmtit.client.ReceivesSettings;


/**
 * Include MT results in translation suggestions.
 */
public class SetUseMT extends SetSetting<Boolean> {

	public SetUseMT(Boolean setting, ReceivesSettings settingsPage) {
		super(setting, settingsPage);
	}

	@Override
	protected void call() {        
		filmTitService.setUseMoses(Gui.getSessionID(), setting, this);
	}

}


package cz.filmtit.client;

import cz.filmtit.share.User;

public interface ReceivesSettings {
	
	void onSettingsReceived (User user);
	public void settingSuccess();
	public void settingError(String message);
}

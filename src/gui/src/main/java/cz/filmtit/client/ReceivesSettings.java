package cz.filmtit.client;

import cz.filmtit.share.User;

/**
 * An interface to communicate with the Settings class,
 * which is ready to receive the User object containing user settings
 * and which changes the settings.
 * Used in the LoadSettings callable to handle the response
 * and in SetSetting to handle results of changing the settings.
 * @author rur
 *
 */
public interface ReceivesSettings {
	
	/**
	 * Called by LoadSettings when settings are successfully received.
	 */
	void onSettingsReceived (User user);
	/**
	 * Called by SetSetting when a setting has been successfully changed.
	 */
	public void settingSuccess();
	/**
	 * Called by SetSetting when a setting has not been successfully changed.
	 */
	public void settingError(String message);
}

/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/

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

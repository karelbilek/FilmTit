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


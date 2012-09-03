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

import cz.filmtit.client.Callable;
import cz.filmtit.client.ReceivesSettings;
import cz.filmtit.share.exceptions.InvalidValueException;

/**
 * An ancestor to methods setting some settings.
 * The settings page is informed about the success or failure on return.
 */
public abstract class SetSetting<T> extends Callable<Void> {
	
	// parameters
	protected T setting;
	private ReceivesSettings settingsPage;

	@Override
	public String getName() {
		return getNameWithParameters(setting);
	}
	
    @Override
    public void onSuccessAfterLog(Void o) {
    	settingsPage.settingSuccess();
    }
    
    @Override
    public void onFailureAfterLog(Throwable returned) {
    	if (returned instanceof InvalidValueException) {
    		// the value is invalid, there is no point in trying again
    		onFinalError(returned.getLocalizedMessage());
    	}
    	else {
        	super.onFailureAfterLog(returned);
    	}
    }

    @Override
    protected void onFinalError(String message) {
        settingsPage.settingError(message);
    }
    
    /**
     * Does <b>not</b> enqueue the call immediately,
     * call enqueue() explicitly!
     */
    public SetSetting(T setting, ReceivesSettings settingsPage) {
		super();
		
		this.setting = setting;
		this.settingsPage = settingsPage;

		// do not enqueue on construction
        // enqueue();
	}

}


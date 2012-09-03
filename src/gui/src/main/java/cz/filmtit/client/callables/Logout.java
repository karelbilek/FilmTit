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
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;

import cz.filmtit.client.*;

import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.core.client.*;
import cz.filmtit.share.*;
import cz.filmtit.share.exceptions.*;
import java.util.*;

/**
 * Invalidate the user session with the given sessionID.
 * @author rur
 *
 */
public class Logout extends Callable<Void> {
	
    @Override
    public void onSuccessAfterLog(Void o) {
        Gui.logged_out(true);
    }
    
    // all errors on log out are ignored...
    
    @Override
    protected void onFinalError(String message) {
        Gui.log("ERROR: logout didn't succeed! Forcing local logout... " + message);
        Gui.logged_out(true);
    }
    
    @Override
    protected void onInvalidSession() {
        Gui.log("already logged out");
        Gui.logged_out(true);
    }
    
    /**
     * Invalidate the user session with the given sessionID.
     */
	public Logout() {
		super();			
		enqueue();
	}

	@Override protected void call() {
        filmTitService.logout(Gui.getSessionID(), this);
	}
}


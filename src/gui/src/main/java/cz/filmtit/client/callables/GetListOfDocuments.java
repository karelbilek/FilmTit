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
import cz.filmtit.client.pages.UserPage;

import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.core.client.*;
import cz.filmtit.share.*;
import java.util.*;

/**
 * Returns all documents owned by the user, ordered by date and time of last
 * change. The documents returned are then shown on the userpage.
 *
 * @author rur
 *
 */
public class GetListOfDocuments extends Callable<List<Document>> {

    // parameters
    private UserPage userpage;

    @Override
    public void onSuccessAfterLog(List<Document> result) {
        Gui.log("received " + result.size() + " documents");

        userpage.setDocuments(result);
        for (Document d : result) {
            Gui.log("GUI Dalsi document. Ma " + d.getTranslationResults().size() + " prfku.");
        }
    }

    /**
     * Returns all documents owned by the user, ordered by date and time of last
     * change. The documents returned are then shown on the userpage.
     */
    public GetListOfDocuments(UserPage userpage) {
        super();

        this.userpage = userpage;

        enqueue();
    }

    @Override
    protected void call() {
        filmTitService.getListOfDocuments(Gui.getSessionID(), this);
    }

}

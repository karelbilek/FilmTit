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

package cz.filmtit.share;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

/**
 * An object of SessionResponse class is sent the GUI when a user successfully logs in
 * to the application. It contains the user unique session ID and the user object
 * containing settings of the user without the list of the documents the user own.
 *
 * @author Jindřich Libovický
 */

public class SessionResponse implements Serializable, IsSerializable {
    /**
     * A default constructor required by GWT.
     */
    public SessionResponse() {}

    /**
     * Creates a SessionResponse object of given properties.
     * @param sessionID Newly generated session ID
     * @param userWithoutDocs Shared user object not contains documents.
     */
    public SessionResponse(String sessionID, User userWithoutDocs) {
        this.sessionID = sessionID;
        this.userWithoutDocs = userWithoutDocs;
    }

    /**
     * Newly generated session ID
     */
    public String sessionID;
    /**
     * Shared user object not contains documents.
     */
    public User userWithoutDocs;
}

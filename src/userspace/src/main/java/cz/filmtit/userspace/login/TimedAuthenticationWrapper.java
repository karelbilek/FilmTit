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

package cz.filmtit.userspace.login;

import org.expressme.openid.Authentication;

import java.util.Date;

/**
 * An extension of the JOpenID Authentication class which contains the time of its creation to provide the server
 * information when it should be timed out.
 *
 * @author Jindřich Libovický
 */
public class TimedAuthenticationWrapper {
    /**
     * JOpenID authentication object.
     */
    private Authentication authentication;
    /**
     * Time when the object was created.
     */
    private long creationTime = new Date().getTime();

    /**
     * Creates the authentication wrapper for authentication object provided by OpenID manager
     * and remembers its creation time.
     * @param authentication
     */
    public TimedAuthenticationWrapper(Authentication authentication) {
        this.authentication = authentication;
    }

    /**
     * Gets the time the object was created.
     * @return The time the object was created.
     */
    public long getCreationTime() { return creationTime; }

    /**
     * Gets the JOpenID authentication object.
     * @return JOpenID authentication object
     */
    public Authentication getAuthentication() { return  authentication; }
}

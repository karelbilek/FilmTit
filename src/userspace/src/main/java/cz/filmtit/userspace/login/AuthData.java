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

import org.expressme.openid.Endpoint;

import java.util.Date;

/**
 * Class representing authentication is process while logging in using OpenID
 */
public class AuthData {
    /**
     * Mac key for Open ID authentication.
     */
    public byte[] Mac_key;
    /**
     * OpenID endpoint.
     */
    public Endpoint endpoint;

    /**
     * Time when the authentication session was started as long.
     */
    private long creationTime = new Date().getTime();

    /**
     * Gets the time when the authentication started as long.
     * @return Time when the authentication session was started.
     */
    public long getCreationTime() { return creationTime; }
}
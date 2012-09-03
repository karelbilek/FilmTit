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

package cz.filmtit.server;

import cz.filmtit.userspace.servlets.FilmTitBackendServer;
import cz.filmtit.core.Configuration;
import cz.filmtit.core.ConfigurationSingleton;

import java.io.File;

/**
 * Helper class for starting the FilmTit service with the specified configuration file on the
 * specified port.
 */

public class StartServer {

    public static FilmTitBackendServer back;
    public static FilmTitFrontendServer front;

    public static void main(String[] args) throws ClassNotFoundException {

        //We need to initialize a Singleton containing the configuration to be able to
        //pass it to all classes.
        ConfigurationSingleton.setConf(new Configuration(new File(args[0])));       

        //If no port is specified, try port 80
        int port = (args.length < 2) ? 80 : (Integer.parseInt(args[1]));

        //Create the front-end server
        front = new FilmTitFrontendServer(port);
    }

}

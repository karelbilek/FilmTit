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

package cz.filmtit.userspace.tests;

import cz.filmtit.core.Configuration;
import cz.filmtit.core.ConfigurationSingleton;
import cz.filmtit.core.tests.TestUtil;
import cz.filmtit.userspace.servlets.FilmTitBackendServer;

import java.io.File;

/**
 * Keeps all the functionality of the FilmtitBackendServer, except loading the translation memory which is
 * switched to the in memory database.
 *
 * @author Jindřich Libovický
*/

public class MockFilmTitBackendServer extends FilmTitBackendServer {
    static {
      ConfigurationSingleton.setConf(new Configuration(new File("configuration.xml")));
    }

    @Override
    protected void loadTranslationMemory() {
        Configuration config = new Configuration(new File("configuration.xml"));
        TM =  TestUtil.createTMWithDummyContent(config);
        //TM = Factory.createTMFromConfiguration(
        //        ConfigurationSingleton.conf(),
        //        true, // readonly
        //        false  // in memory
        //);
    }

    /**
     * Logs in a user which is created in this code
     * @return Session ID of the
     */
    public String loginADummyUser() {
        return null;
    }


}

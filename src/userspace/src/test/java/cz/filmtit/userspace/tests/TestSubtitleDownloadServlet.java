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
import cz.filmtit.userspace.servlets.FilmTitBackendServer;
import cz.filmtit.userspace.servlets.SubtitleDownloadServlet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Jindřich Libovický
 */
public class TestSubtitleDownloadServlet {
    @BeforeClass
    public static void setupConfiguration() {
        Configuration configuration = new Configuration("configuration.xml");
        ConfigurationSingleton.setConf(configuration);
        MockHibernateUtil.changeUtilsInAllClasses();
    }

    @AfterClass
    public static void clean() {
        MockHibernateUtil.clearDatabase();
    }

    @Test
    public void testGettingProperFile() {
        FilmTitBackendServer backendServer = new MockFilmTitBackendServer();
        SubtitleDownloadServlet downloadServlet = new SubtitleDownloadServlet(backendServer);

        //HttpServletRequest req = new HttpServletRequest()
    }
}

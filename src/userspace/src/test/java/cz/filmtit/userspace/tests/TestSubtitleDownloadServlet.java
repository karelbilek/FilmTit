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

        //HttpServletRequest
    }
}

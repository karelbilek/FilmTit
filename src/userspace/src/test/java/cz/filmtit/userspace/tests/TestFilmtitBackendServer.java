package cz.filmtit.userspace.tests;

import cz.filmtit.core.Configuration;
import cz.filmtit.core.ConfigurationSingleton;
import cz.filmtit.userspace.servlets.FilmTitBackendServer;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.message.MessageException;

/**
 * @author Jindřich Libovický
 */
public class TestFilmtitBackendServer {
    @BeforeClass
    public static void setupConfiguration() {
        Configuration configuration = new Configuration("configuration.xml");
        ConfigurationSingleton.setConf(configuration);
        MockHibernateUtil.changeUtilsInAllClasses();
    }


    @Test
    public void testGetAutheticationURL() throws ConsumerException, MessageException {
        FilmTitBackendServer server = new MockFilmTitBackendServer();
        //server.authenticateOpenId("http://google.com/");
    }
}

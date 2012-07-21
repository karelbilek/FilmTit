package cz.filmtit.userspace.tests;

import cz.filmtit.core.Configuration;
import cz.filmtit.core.ConfigurationSingleton;
import cz.filmtit.userspace.FilmTitBackendServer;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.message.MessageException;

/**
 * @author Jindřich Libovický
 */
public class TestFilmtitBackendServer {
    @BeforeClass
    public static void InitializeDatabase() {
        DatabaseUtil.setDatabase();
        ConfigurationSingleton.setConf(new Configuration("configuration.xml"));
    }

    @Test
    public void testGetAutheticationURL() throws ConsumerException, MessageException {
        FilmTitBackendServer server = new MockFilmTitBackendServer();
        server.authenticateOpenId("http://google.com/");
    }
}

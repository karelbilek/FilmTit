package cz.filmtit.userspace.tests;

import cz.filmtit.share.AuthenticationServiceType;
import cz.filmtit.userspace.FilmTitBackendServer;
import org.junit.BeforeClass;
import org.junit.Test;
import cz.filmtit.core.Configuration;
import cz.filmtit.core.ConfigurationSingleton;

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
    public void testGetAutheticationURL() {
        FilmTitBackendServer server = new MockFilmTitBackendServer();
        System.out.println(server.getAuthenticationURL(1234, AuthenticationServiceType.GOOGLE));
    }
}

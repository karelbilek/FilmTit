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

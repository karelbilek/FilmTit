package cz.filmtit.userspace.tests;

import cz.filmtit.core.tests.TestUtil;
import cz.filmtit.core.ConfigurationSingleton;
import cz.filmtit.userspace.FilmTitBackendServer;

/**
 * Keeps all the functionality of the FilmtitBackendServer, except loading the translation memory which is
 * switched to the in memory database.
 *
 * @author Jindřich Libovický
*/

public class MockFilmTitBackendServer extends FilmTitBackendServer {
    @Override
    protected void loadTranslationMemory() {
        TM =  TestUtil.createTMWithDummyContent(ConfigurationSingleton.getConf());
    }

    /**
     * Logs in a user which is created in this code
     * @return Session ID of the
     */
    public String loginADummyUser() {
        return null;
    }


}

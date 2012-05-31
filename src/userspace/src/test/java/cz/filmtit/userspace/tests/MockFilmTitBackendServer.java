package cz.filmtit.userspace.tests;

import cz.filmtit.core.Configuration;
import cz.filmtit.core.tests.TestUtil;
import cz.filmtit.userspace.FilmTitBackendServer;
import java.io.File;

/**
 * Keeps all the functionality of the FilmtitBackendServer, except loading the translation memory which is
 * switched to the in memory database.
 *
 * @author Jindřich Libovický
*/

public class MockFilmTitBackendServer extends FilmTitBackendServer {
    @Override
    protected void loadTranslationMemory() {
        TM =  TestUtil.createTMWithDummyContent(new Configuration(new File("configuration.xml")));
    }


}

package cz.filmtit.userspace.tests;

import cz.filmtit.share.Document;
import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.TranslationResult;
import cz.filmtit.userspace.FilmTitBackendServer;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class TestUSDocument {
    @BeforeClass
    public static void InitializeDatabase() {
        DatabaseUtil.setDatabase();
    }

    @Test
    public void TestServerCall() {
        FilmTitBackendServer server = new FilmTitBackendServer();
        Document resultDocument = server.createDocument("Movie title", "2012", "cs");

        assertEquals("Movie title", resultDocument.getMovie().getTitle());
        assertEquals("2012", resultDocument.getMovie().getYear());
        assertEquals("Czech", resultDocument.getLanguage().getName());
        assertTrue(resultDocument.getId() != Long.MIN_VALUE);
    }

    @Test
    public void TestSaveAndLoad() { }
}



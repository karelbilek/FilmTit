package cz.filmtit.userspace.tests;

import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.TranslationResult;
import cz.filmtit.userspace.FilmTitBackendServer;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class TestUSTranslationResult {
    @BeforeClass
    public static void InitializeDatabase() {
        DatabaseUtil.setDatabase();
    }

    @Test
    public void TestServerCall() {
        FilmTitBackendServer server = new MockFilmTitBackendServer();
        TranslationResult sampleResult =
                server.getTranslationResults(new TimedChunk("001", "002", 1, "Sample chunk", 5, 0));

        assertEquals("001", sampleResult.getSourceChunk().getStartTime());
        assertEquals("002", sampleResult.getSourceChunk().getEndTime());
        assertEquals(1, sampleResult.getSourceChunk().getPartNumber());
        assertEquals("Sample chunk", sampleResult.getSourceChunk().getSurfaceForm());
        assertEquals(5, sampleResult.getChunkId());
        assertEquals(0, sampleResult.getDocumentId());
    }

    @Test
    public void TestSaveAndLoad() { }
}



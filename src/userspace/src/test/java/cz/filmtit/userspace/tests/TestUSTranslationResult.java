package cz.filmtit.userspace.tests;

import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.TranslationResult;
import cz.filmtit.userspace.FilmTitBackendServer;
import cz.filmtit.userspace.USTranslationResult;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertEquals;

public class TestUSTranslationResult {
    @BeforeClass
    public static void InitializeDatabase() {
        DatabaseUtil.setDatabase();
    }

    @Test
    public void testServerCall() {
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
    public void testSaveAndLoad() { }

    @Test
    public void testFeedBackNotCrash() {
        USTranslationResult testRes = new USTranslationResult(
                new TimedChunk("0:00", "0:00",0, "Sample text", 0, 0l));

        //Session dbSession = HibernateUtil.getSessionFactory().getCurrentSession();
        //dbSession.beginTransaction();

        //testRes.setUserTranslation("User translation");
        //testRes.saveToDatabase(dbSession);

        //testRes.saveToDatabase(dbSession);

        //dbSession.getTransaction().commit();

        List<TranslationResult> res = USTranslationResult.getUncheckedResults();
        assertEquals(0, res.size());
    }
}



package cz.filmtit.userspace.tests;

import cz.filmtit.core.ConfigurationSingleton;
import cz.filmtit.core.model.TranslationMemory;
import cz.filmtit.core.tests.TestUtil;
import cz.filmtit.share.Document;
import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.TranslationResult;
import cz.filmtit.userspace.HibernateUtil;
import cz.filmtit.userspace.USDocument;
import cz.filmtit.userspace.USTranslationResult;
import org.hibernate.Session;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class TestUSTranslationResult {
    @BeforeClass
    public static void InitializeDatabase() {
        DatabaseUtil.setDatabase();
    }

    @Test
    public void testDatabaseSaveAndLoad() {
        USTranslationResult sampleResult = new USTranslationResult(new TimedChunk("001", "002", 1, "Sample chunk", 5, 0));

        // save to database
        Session dbSession = HibernateUtil.getSessionFactory().getCurrentSession();
        dbSession.beginTransaction();

        sampleResult.saveToDatabase(dbSession);

        long savedID = sampleResult.getDatabaseId();

        // load it from database again
        List queryResult = dbSession.createQuery("select t from USTranslationResult t where t.databaseId = " +
            Long.toString(savedID)).list();

        assertEquals(1, queryResult.size());

        USTranslationResult loadedResult = (USTranslationResult)(queryResult.get(0));

        dbSession.getTransaction().commit();

        // test if the loaded and saved objects are the same
        assertEquals("001", loadedResult.getStartTime());
        assertEquals("002", loadedResult.getEndTime());
        assertEquals(1, loadedResult.getPartNumber());
        assertEquals("Sample chunk", loadedResult.getText());
        assertEquals(5, loadedResult.getSharedId());
        assertEquals(0, loadedResult.getDocumentDatabaseId());
        assertEquals(null, loadedResult.getUserTranslation());

        // change the user translation
        sampleResult.setUserTranslation("A translation a user has added.");

        dbSession = HibernateUtil.getSessionFactory().getCurrentSession();
        dbSession.beginTransaction();

        // save the change to the database
        sampleResult.saveToDatabase(dbSession);

        // load the object again
        queryResult = dbSession.createQuery("select t from USTranslationResult t where t.databaseId = " +
                Long.toString(savedID)).list();

        assertEquals(1, queryResult.size());

        loadedResult = (USTranslationResult)(queryResult.get(0));

        // test if the loaded object reflected the change correctly
        assertEquals("001", loadedResult.getStartTime());
        assertEquals("002", loadedResult.getEndTime());
        assertEquals(1, loadedResult.getPartNumber());
        assertEquals("Sample chunk", loadedResult.getText());
        assertEquals(5, loadedResult.getSharedId());
        assertEquals(0, loadedResult.getDocumentDatabaseId());
        assertEquals("A translation a user has added.", loadedResult.getUserTranslation());

        // delete the tested TranslationResults

        dbSession.createQuery("delete from USTranslationResult t where t.databaseId = "
                + Long.toString(savedID));

        dbSession.getTransaction().commit();
    }

    @Test
    public void testGenerateMTSuggestions() {
        TranslationMemory TM = TestUtil.createTMWithDummyContent(ConfigurationSingleton.getConf());

        USDocument document = new USDocument(new Document("Hannah and Her Sisters", "1986", "en"));

        USTranslationResult usTranslationResult = new USTranslationResult(new TimedChunk("001", "002", 1, "Sample chunk", 5, 0));
        usTranslationResult.setParent(document);

        usTranslationResult.generateMTSuggestions(TM);
        assertNotNull(usTranslationResult.getTranslationResult().getTmSuggestions());
    }

    @Test
    public void testProvidingFeedback() {
        USTranslationResult testRes = new USTranslationResult(
                new TimedChunk("0:00", "0:00",0, "Sample text", 0, 0l));

        org.hibernate.Session dbSession = HibernateUtil.getSessionFactory().getCurrentSession();
        dbSession.beginTransaction();

        testRes.setUserTranslation("User translation");
        testRes.saveToDatabase(dbSession);

        dbSession.getTransaction().commit();

        List<TranslationResult> res = USTranslationResult.getUncheckedResults();
        assertEquals(2, res.size());
    }

    // THE TEST BELOW SHOULD BE UPDATED AND MOVED TO DIFFERENT TEST CLASS

    @Test
    public void testServerCallWithUserEditing() {
        /*FilmTitBackendServer server = new MockFilmTitBackendServer();

        // TODO: some false logging in has to be added

        DocumentResponse response = server.createNewDocument("Movie title", "2008", "cs");
        long usedDocumentID = response.document.getId();

        TranslationResult sampleResult =
                server.getTranslationResults(new TimedChunk("001", "002", 1, "Sample chunk", 5, usedDocumentID));

        sampleResult.setUserTranslation("The translation the user provided.");
        sampleResult.setSelectedTranslationPairID(0);

        server.setUserTranslation(sampleResult.getChunkId(), usedDocumentID, sampleResult.getUserTranslation(),
                sampleResult.getSelectedTranslationPairID());

        // TODO: attempt to save it to the database   */
    }

    @Test
    public void testSimpleServerCall() {
        /*FilmTitBackendServer server = new MockFilmTitBackendServer();
        TranslationResult sampleResult =
                server.getTranslationResults(new TimedChunk("001", "002", 1, "Sample chunk", 5, 0));

        // TODO: some false logging in has to be added

        assertEquals("001", sampleResult.getSourceChunk().getStartTime());
        assertEquals("002", sampleResult.getSourceChunk().getEndTime());
        assertEquals(1, sampleResult.getSourceChunk().getPartNumber());
        assertEquals("Sample chunk", sampleResult.getSourceChunk().getSurfaceForm());
        assertEquals(5, sampleResult.getChunkId());
        assertEquals(0, sampleResult.getDocumentId());   */
    }


}



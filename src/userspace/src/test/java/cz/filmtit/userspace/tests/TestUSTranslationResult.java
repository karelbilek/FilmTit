package cz.filmtit.userspace.tests;

import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.TranslationResult;
import cz.filmtit.userspace.HibernateUtil;
import cz.filmtit.userspace.USTranslationResult;
import org.hibernate.Session;
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

        /*Configuration conf = ConfigurationSingleton.getConf();
        TranslationMemory TM = TestUtil.createTMWithDummyContent(conf);

        USDocument document = new USDocument(new Document("Hannah and Her Sisters", "1986", "en"));

        USTranslationResult usTranslationResult = new USTranslationResult(new TimedChunk("001", "002", 1, "Sample chunk", 5, 0));
        usTranslationResult.setDocument(document);

        usTranslationResult.generateMTSuggestions(TM);
        assertNotNull(usTranslationResult.getTranslationResult().getTmSuggestions());*/
    }

    @Test
    public void testProvidingFeedback() {
        USTranslationResult testRes = new USTranslationResult(
                new TimedChunk("0:00", "0:00",0, "Sample text", 0, 0l));

        org.hibernate.Session dbSession = HibernateUtil.getSessionFactory().openSession();
        dbSession.beginTransaction();

        testRes.setUserTranslation("User translation");
        testRes.saveToDatabase(dbSession);

        dbSession.getTransaction().commit();

        List<TranslationResult> res = USTranslationResult.getUncheckedResults();
        //assertEquals(1, res.size());
    }

}



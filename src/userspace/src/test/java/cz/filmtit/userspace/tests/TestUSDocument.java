package cz.filmtit.userspace.tests;

import cz.filmtit.core.Configuration;
import cz.filmtit.core.ConfigurationSingleton;
import cz.filmtit.share.Document;
import cz.filmtit.share.TimedChunk;
import cz.filmtit.userspace.USDocument;
import cz.filmtit.userspace.USHibernateUtil;
import cz.filmtit.userspace.USTranslationResult;
import cz.filmtit.userspace.USUser;
import org.hibernate.Session;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;

public class TestUSDocument {
    @BeforeClass
    public static void setupConfiguration() {
        Configuration configuration = new Configuration("configuration.xml");
        ConfigurationSingleton.setConf(configuration);
        MockHibernateUtil.changeUtilsInAllClasses();
    }

    @AfterClass
    public static void clean() {
        MockHibernateUtil.clearDatabase();
    }


    private USHibernateUtil usHibernateUtil = MockHibernateUtil.getInstance();

    @Test
    public  void testUSDocumentConstructor() {
        USUser user = new USUser("name");
        Document doc = new Document("Movie title", "cs", "");
        USDocument resultUSDocument = new USDocument(doc, user);

        assertEquals(resultUSDocument.getLanguageCode(), doc.getLanguage().getCode());
    }

    @Test
    public void testSaveAndLoadWithTranslationResults() {
        Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();

        // create a sample document and save it to the database to know the ID
        USUser user = new USUser("name");
        Document doc = new Document("Movie title", "cs", "");
        USDocument sampleUSDocument = new USDocument(doc, user);

        sampleUSDocument.saveToDatabase(dbSession);
        usHibernateUtil.closeAndCommitSession(dbSession);

        // now add few sample chunks
        long documentID = sampleUSDocument.getDatabaseId();
        USTranslationResult sampleTR1 = new USTranslationResult(new TimedChunk("01:43:29,000", "01:43:32,128", 0, "Sample text 1", 1, documentID));
        USTranslationResult sampleTR2 = new USTranslationResult(new TimedChunk("00:01:57,377", "00:02:01,172", 0, "Sample text 2", 2, documentID));
        USTranslationResult sampleTR3 = new USTranslationResult(new TimedChunk("00:02:01,297", "00:02:03,758", 0, "Sample text 3", 3, documentID));

        sampleUSDocument.addTranslationResult(sampleTR1);
        sampleUSDocument.addTranslationResult(sampleTR2);
        sampleUSDocument.addTranslationResult(sampleTR3);

        // safe the translation results
        dbSession = usHibernateUtil.getSessionWithActiveTransaction();
        sampleUSDocument.saveToDatabase(dbSession);
        usHibernateUtil.closeAndCommitSession(dbSession);

        // test if the translation results got the database IDs
        assertNotSame(Long.MIN_VALUE, sampleTR1.getDatabaseId());
        assertNotSame(Long.MIN_VALUE, sampleTR2.getDatabaseId());
        assertNotSame(Long.MIN_VALUE, sampleTR3.getDatabaseId());

        // now load the document from database
        dbSession = usHibernateUtil.getSessionWithActiveTransaction();

        List queryResult = dbSession.createQuery("select d from USDocument d where d.databaseId = " +
                Long.toString(documentID)).list();

        usHibernateUtil.closeAndCommitSession(dbSession);

        assertEquals(1, queryResult.size());
        USDocument loadedDocument = (USDocument)(queryResult.get(0));

        // test if its the same
        assertEquals(sampleUSDocument.getLanguageCode(), doc.getLanguage().getCode());

        // now call the loadChunksFromDb method
        loadedDocument.loadChunksFromDb();

        // test if the loaded TranslationResults are the same as the saved ones
        assertEquals(3, loadedDocument.getTranslationResultValues().size());
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testDatabaseImmutability() {
        USUser user = new USUser("name");
        Document doc = new Document("Movie title", "cs", "");
        USDocument resultUSDocument = new USDocument(doc, user);

        resultUSDocument.setDatabaseId(2001);
    }
}




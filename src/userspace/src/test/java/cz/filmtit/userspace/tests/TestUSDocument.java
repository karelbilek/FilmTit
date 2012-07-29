package cz.filmtit.userspace.tests;

import cz.filmtit.share.Document;
import cz.filmtit.share.TimedChunk;
import cz.filmtit.userspace.USDocument;
import cz.filmtit.userspace.USTranslationResult;
import org.hibernate.Session;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;

public class TestUSDocument {
    @BeforeClass
    public static void initializeDatabase() {
        DatabaseUtil.setDatabase();
    }

    @Test
    public  void testUSDocumentConstructor() {
        Document doc = new Document("Movie title", "2012", "cs");
        USDocument resultUSDocument = new USDocument(doc, null);

        assertEquals(resultUSDocument.getMovieTitle(), doc.getMovie().getTitle());
        assertEquals(resultUSDocument.getYear(), doc.getMovie().getYear());
        assertEquals(resultUSDocument.getLanguageCode(), doc.getLanguage().getCode());

        assertEquals(false,resultUSDocument.isFinished());
    }

    @Test
    public void testSaveAndLoadWithTranslationResults() {
        Session dbSession = DatabaseUtil.getSession();

        // create a sample document and save it to the database to know the ID
        Document doc = new Document("Movie title", "2012", "cs");
        USDocument sampleUSDocument = new USDocument(doc, null);
        sampleUSDocument.setFinished(false);

        sampleUSDocument.setSpentOnThisTime(120);
        sampleUSDocument.setTranslationGenerationTime(50);

        dbSession.beginTransaction();
        sampleUSDocument.saveToDatabase(dbSession);
        dbSession.getTransaction().commit();

        // now add few sample chunks
        long documentID = sampleUSDocument.getDatabaseId();
        USTranslationResult sampleTR1 = new USTranslationResult(new TimedChunk("01:43:29,000", "01:43:32,128", 0, "Sample text 1", 1, documentID));
        USTranslationResult sampleTR2 = new USTranslationResult(new TimedChunk("00:01:57,377", "00:02:01,172", 0, "Sample text 2", 2, documentID));
        USTranslationResult sampleTR3 = new USTranslationResult(new TimedChunk("00:02:01,297", "00:02:03,758", 0, "Sample text 3", 3, documentID));

        sampleUSDocument.addTranslationResult(sampleTR1);
        sampleUSDocument.addTranslationResult(sampleTR2);
        sampleUSDocument.addTranslationResult(sampleTR3);

        // safe the translation results
        dbSession.beginTransaction();
        sampleUSDocument.saveToDatabase(dbSession);
        dbSession.getTransaction().commit();

        // test if the translation results got the database IDs
        assertNotSame(Long.MIN_VALUE, sampleTR1.getDatabaseId());
        assertNotSame(Long.MIN_VALUE, sampleTR2.getDatabaseId());
        assertNotSame(Long.MIN_VALUE, sampleTR3.getDatabaseId());

        // now load the document from database
        dbSession.beginTransaction();

        List queryResult = dbSession.createQuery("select d from USDocument d where d.databaseId = " +
                Long.toString(documentID)).list();

        dbSession.getTransaction().commit();

        assertEquals(1, queryResult.size());
        USDocument loadedDocument = (USDocument)(queryResult.get(0));

        // test if its the same
        assertEquals(sampleUSDocument.getMovieTitle(), doc.getMovie().getTitle());
        assertEquals(sampleUSDocument.getYear(), doc.getMovie().getYear());
        assertEquals(sampleUSDocument.getLanguageCode(), doc.getLanguage().getCode());

        // now call the loadChunksFromDb method
        loadedDocument.loadChunksFromDb();

        // test if the loaded TranslationResults are the same as the saved ones
        assertEquals(3, loadedDocument.getTranslationsResults().size());

    }

    @Test(expected=UnsupportedOperationException.class)
    public void testDatabaseImmutability() {
        Session session = DatabaseUtil.getSession();
        Document doc = new Document("Movie title", "2012", "cs");
        USDocument resultUSDocument = new USDocument(doc, null);

        resultUSDocument.setDatabaseId(2001);
    }

    // SHOULD BE MOVED SOMEWHERE ...

    @Test
    public void testServerCall() {
        /*FilmTitBackendServer server = new MockFilmTitBackendServer();
        Document resultDocument = server.createDocument("Movie title", "2012", "cs");

        assertEquals("Movie title", resultDocument.getMovie().getTitle());
        assertEquals("2012", resultDocument.getMovie().getYear());
        assertEquals("cs", resultDocument.getLanguage().getCode());
        assertEquals("Czech", resultDocument.getLanguage().getName());
        assertTrue(resultDocument.getId() != Long.MIN_VALUE);*/
    }
}




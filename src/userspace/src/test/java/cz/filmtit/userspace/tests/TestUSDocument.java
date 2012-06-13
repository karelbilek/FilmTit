package cz.filmtit.userspace.tests;

import cz.filmtit.share.Document;
import cz.filmtit.userspace.USDocument;
import org.hibernate.Session;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class TestUSDocument {
    @BeforeClass
    public static void initializeDatabase() {
        DatabaseUtil.setDatabase();
    }


    @Test
    public  void testUSDocumentConstructor() {
        Document doc = new Document("Movie title", "2012", "cs");
        USDocument resultUSDocument = new USDocument(doc);

        assertEquals(resultUSDocument.getMovieTitle(), doc.getMovie().getTitle());
        assertEquals(resultUSDocument.getYear(), doc.getMovie().getYear());
        assertEquals(resultUSDocument.getLanguageCode(), doc.getLanguage().getCode());

        assertEquals(false,resultUSDocument.isFinished());
    }

    @Test
    public void testSaveAndLoadWithTranslationResults() {
        Session session = DatabaseUtil.getSession();
        Document doc = new Document("Movie title", "2012", "cs");
        USDocument resultUSDocument = new USDocument(doc);
        resultUSDocument.setFinished(false);

        resultUSDocument.setSpentOnThisTime(120);
        resultUSDocument.setTranslationGenerationTime(50);

        // TODO: Add some sample subtitles

        session.beginTransaction();

        if (session.isOpen()) {
            resultUSDocument.saveToDatabase(session);
        }
        session.getTransaction().commit();

        // TODO: after loading the document from database test if the Tranlsation results are loaded properly including the parent reference

    }

    @Test(expected=UnsupportedOperationException.class)
    public void testDatabaseImmutability() {
        Session session = DatabaseUtil.getSession();
        Document doc = new Document("Movie title", "2012", "cs");
        USDocument resultUSDocument = new USDocument(doc);

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




package cz.filmtit.userspace.tests;

import cz.filmtit.share.Document;
import cz.filmtit.userspace.FilmTitBackendServer;
import cz.filmtit.userspace.USDocument;
import org.hibernate.Session;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.annotation.Annotation;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class TestUSDocument implements Test {
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
        assertEquals("cs", resultDocument.getLanguage().getCode());
        assertEquals("Czech", resultDocument.getLanguage().getName());
        assertTrue(resultDocument.getId() != Long.MIN_VALUE);
    }

    @Test
    public  void  TestUSDocumentConstructor()
    {
        Document doc = new Document("Movie title", "2012", "cs");
        USDocument resultUSDocument = new USDocument(doc);

        assertEquals(resultUSDocument.getMovieTitle(), doc.getMovie().getTitle());
        assertEquals(resultUSDocument.getYear(), doc.getMovie().getYear());
        assertEquals(resultUSDocument.getLanguageCode(), doc.getLanguage().getCode());

        assertEquals(false,resultUSDocument.isFinished());


    }
    @Test
    public void TestSave() {
        Session session = DatabaseUtil.getSession();
        Document doc = new Document("Movie title", "2012", "cs");
        USDocument resultUSDocument = new USDocument(doc);
        resultUSDocument.setFinished(false);

        resultUSDocument.setSpentOnThisTime(120);
        resultUSDocument.setTranslationGenerationTime(50);
        session.beginTransaction();
        if (session.isOpen())
        {
         resultUSDocument.saveToDatabase(session);
        }
        session.getTransaction().commit();


    }

    @Test(expected=UnsupportedOperationException.class)
    public void TestDatabaseImmutability() {
        Session session = DatabaseUtil.getSession();
        Document doc = new Document("Movie title", "2012", "cs");
        USDocument resultUSDocument = new USDocument(doc);

        resultUSDocument.setDatabaseId(2001);
    }

    @Test
    public void TestLoad() {
        FilmTitBackendServer server = new FilmTitBackendServer();
        Document resultDocument = server.createDocument("Movie title", "2012", "cs");
        USDocument doc = USDocument.load(resultDocument.getId());

        assertEquals(doc.getLanguageCode(), "cs");
        assertEquals(doc.getMovieTitle(),"Movie title");
        assertEquals(doc.getYear(),"2012");
    }

    @Override
    public Class<? extends Throwable> expected() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long timeout() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean equals(Object obj) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int hashCode() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String toString() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}




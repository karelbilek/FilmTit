package cz.filmtit.userspace.tests;

import cz.filmtit.share.Document;
import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.TranslationResult;
import cz.filmtit.userspace.FilmTitBackendServer;
import  cz.filmtit.userspace.USDocument;
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

    public  void  TestUSDocumentConstructor()
    {
        FilmTitBackendServer server = new FilmTitBackendServer();
        Document resultDocument = server.createDocument("Movie title", "2012", "cs");
        USDocument resultUSDocument = new USDocument(resultDocument);

        assertEquals(resultUSDocument.getMovieTitle(), resultDocument.getMovie().getTitle());
        assertEquals(resultUSDocument.getYear(), resultDocument.getMovie().getYear());
        assertEquals(resultUSDocument.getLanguageCode(), resultDocument.getLanguage().getCode());
        assertEquals(resultUSDocument.getLanguage().getName(), resultDocument.getLanguage().getName());

        assertEquals(false,resultUSDocument.isFinished());


    }
    @Test
    public void TestSaveAndLoad() { }


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




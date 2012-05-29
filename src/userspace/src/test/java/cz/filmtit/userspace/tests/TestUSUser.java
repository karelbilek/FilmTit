package cz.filmtit.userspace.tests;

import cz.filmtit.share.Document;
import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.TranslationResult;
import cz.filmtit.userspace.FilmTitBackendServer;
import cz.filmtit.userspace.USDocument;
import cz.filmtit.userspace.USUser;
import junit.framework.*;
import org.junit.*;
import org.junit.Assert;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class TestUSUser {
    @BeforeClass
    public static void InitializeDatabase() {
        DatabaseUtil.setDatabase();
    }


    @Test
    public void TestOwnedDocument() {
        USUser user  = new USUser();
        Assert.assertEquals(0,user.getOwnedDocuments().size());
   }

    @Test
    public void TestSaveUser()
    {
        USUser user  = new USUser();

    }

}



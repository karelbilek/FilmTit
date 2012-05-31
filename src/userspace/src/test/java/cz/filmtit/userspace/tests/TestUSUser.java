package cz.filmtit.userspace.tests;

import cz.filmtit.userspace.USUser;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestUSUser {
    @BeforeClass
    public static void InitializeDatabase() {
        DatabaseUtil.setDatabase();
    }


    @Test
    public void testOwnedDocument() {
        USUser user  = new USUser();
        Assert.assertEquals(0,user.getOwnedDocuments().size());
   }

    @Test
    public void testSaveUser()
    {
        USUser user  = new USUser();

    }

}



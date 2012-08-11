package cz.filmtit.userspace.tests;

import cz.filmtit.core.Configuration;
import cz.filmtit.core.ConfigurationSingleton;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestUSUser {
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



    @Test
    public void testOwnedDocument() {
        //USUser user  = new USUser();
        //Assert.assertEquals(0,user.getOwnedDocuments().size());
   }

    @Test
    public void testChangeUserName()
    {
        //USUser user  = new USUser();

    }

}



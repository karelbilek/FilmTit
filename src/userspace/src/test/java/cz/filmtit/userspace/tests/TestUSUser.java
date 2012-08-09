package cz.filmtit.userspace.tests;

import cz.filmtit.core.Configuration;
import cz.filmtit.core.ConfigurationSingleton;
import cz.filmtit.userspace.USHibernateUtil;
import cz.filmtit.userspace.USUser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertEquals;

public class TestUSUser {
    @BeforeClass
    public static void setupConfiguration() {
        Configuration configuration = new Configuration("configuration.xml");
        ConfigurationSingleton.setConf(configuration);
        MockHibernateUtil.changeUtilsInAllClasses();
    }

    private USHibernateUtil usHibernateUtil = MockHibernateUtil.getInstance();

    @AfterClass
    public static void clean() {
        MockHibernateUtil.clearDatabase();
    }

    @Test
    public void testDatabaseSaveAndLoad() {
        USUser user = new USUser("user", "password", "bu@bu.bu", null);

        // test initial settings
        assertEquals(ConfigurationSingleton.conf().maximumSuggestionsCount(), user.getMaximumNumberOfSuggestions());
        assertEquals(true, user.getUseMoses());
        assertEquals(false, user.isPermanentlyLoggedId());

        user.setEmail("hu@hu.hu");
        user.setMaximumNumberOfSuggestions(23);
        user.setPermanentlyLoggedId(true);
        user.setUseMoses(false);

        org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();
        user.saveToDatabase(dbSession);
        usHibernateUtil.closeAndCommitSession(dbSession);

        dbSession = usHibernateUtil.getSessionWithActiveTransaction();
        List dbRes = dbSession.createQuery("select u from USUser u where u.databaseId = :did")
                .setParameter("did", user.getDatabaseId()).list();
        assertEquals(1, dbRes.size());
        usHibernateUtil.closeAndCommitSession(dbSession);

        USUser dbUser = (USUser)dbRes.get(0);

        // test changed settings
        assertEquals("user", dbUser.getUserName());
        assertEquals("password", dbUser.getPassword());
        assertEquals("hu@hu.hu", dbUser.getEmail());
        assertEquals(23, dbUser.getMaximumNumberOfSuggestions());
        assertEquals(false, dbUser.getUseMoses());
        assertEquals(true, dbUser.isPermanentlyLoggedId());

    }

}



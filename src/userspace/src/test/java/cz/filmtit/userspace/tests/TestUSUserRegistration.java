package cz.filmtit.userspace.tests;

import cz.filmtit.core.Configuration;
import cz.filmtit.core.ConfigurationSingleton;
import cz.filmtit.userspace.FilmTitBackendServer;
import cz.filmtit.userspace.HibernateUtil;
import cz.filmtit.userspace.USUser;
import org.hibernate.Session;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertEquals;

public class TestUSUserRegistration {
    @BeforeClass
    public static void InitializeDatabase() {
        DatabaseUtil.setDatabase();
        ConfigurationSingleton.setConf(new Configuration("configuration.xml"));
    }

    @Test
    public void testRegistration() {

        FilmTitBackendServer server = new MockFilmTitBackendServer();

        server.registration("Pepa", "hitman", "cechjoe@gmail.com", null);

        Session dbSession = HibernateUtil.getSessionWithActiveTransaction();
        List UserResult = dbSession.createQuery("select d from USUser d where d.userName ='Pepa' AND d.password = 'hitman'").list();
        HibernateUtil.closeAndCommitSession(dbSession);

        assertEquals(1, UserResult.size());
        USUser loadedUser = (USUser)(UserResult.get(0));

        assertEquals("Pepa",loadedUser.getUserName());
        assertEquals("hitman",loadedUser.getPassword());
        assertEquals("cechjoe@gmail.com",loadedUser.getEmail());
    }

}



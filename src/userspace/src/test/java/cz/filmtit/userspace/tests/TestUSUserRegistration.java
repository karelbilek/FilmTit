package cz.filmtit.userspace.tests;

import cz.filmtit.userspace.FilmTitBackendServer;
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
    }



    @Test
    public void testRegistration()
    {
        FilmTitBackendServer server = new FilmTitBackendServer();

        server.Registration("Pepa","hitman","cechjoe@gmail.com",null);

        Session dbSession = DatabaseUtil.getSession();
        List UserResult = dbSession.createQuery("select d from USUser d where d.userName ='Pepa' AND d.password = 'hitman'").list();

        dbSession.getTransaction().commit();

        assertEquals(1, UserResult.size());
        USUser loadedUser = (USUser)(UserResult.get(0));

        assertEquals("Pepa",loadedUser.getUserName());
        assertEquals("hitman",loadedUser.getPassword());
        assertEquals("cechjoe@gmail.com",loadedUser.getEmail());
    }

}



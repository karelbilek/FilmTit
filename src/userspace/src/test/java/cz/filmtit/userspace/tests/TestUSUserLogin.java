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
import static junit.framework.Assert.assertFalse;

public class TestUSUserLogin {
    @BeforeClass
    public static void InitializeDatabase() {
        DatabaseUtil.setDatabase();
        ConfigurationSingleton.setConf(new Configuration("configuration.xml"));
    }
    FilmTitBackendServer server = null;
     void TestUSUserLogin()
     {
        CreateServer();
     }

    void CreateServer()
    {
        server = new MockFilmTitBackendServer();
    }

    @Test
    public void testRegistration() {

        String name = "Pepa";
        String pass = "hitman";
        String email = "cechjoe@gmail.com";
     if (server ==null)  {CreateServer();};


       server.registration(name, pass, email, null);

       Session dbSession = HibernateUtil.getSessionWithActiveTransaction();
       List UserResult = dbSession.createQuery("select d from USUser d where d.userName ='"+name+"' ").list();
       HibernateUtil.closeAndCommitSession(dbSession);

       assertFalse(UserResult.size()==0);

    }

   @Test
    public void testLogin()
    {
        String name = "Pepa";
        String pass = "hitman";
        String email = "cechjoe@gmail.com";
        if (server ==null)  {CreateServer();};
        if (server.simple_login(name,pass)!="")
        {
            server.registration(name,pass,email,null);
        }

        assertFalse(server.simple_login(name,pass)=="");
    }
}



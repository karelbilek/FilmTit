package cz.filmtit.userspace.tests;

import cz.filmtit.core.Configuration;
import cz.filmtit.core.ConfigurationSingleton;
import cz.filmtit.userspace.FilmTitBackendServer;
import cz.filmtit.userspace.USHibernateUtil;
import org.hibernate.Session;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertFalse;

public class TestUSUserLogin {
    @BeforeClass
    public static void setupConfiguration() {
        Configuration configuration = new Configuration("configuration.xml");
        ConfigurationSingleton.setConf(configuration);
        MockHibernateUtil.changeUtilsInAllClasses();
    }

    private USHibernateUtil usHibernateUtil = MockHibernateUtil.getInstance();
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

       Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();
       List UserResult = dbSession.createQuery("select d from USUser d where d.userName ='"+name+"' ").list();
       usHibernateUtil.closeAndCommitSession(dbSession);

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



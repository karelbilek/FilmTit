package cz.filmtit.userspace.tests;

import cz.filmtit.core.Configuration;
import cz.filmtit.core.ConfigurationSingleton;
import cz.filmtit.userspace.servlets.FilmTitBackendServer;
import cz.filmtit.userspace.USHibernateUtil;
import cz.filmtit.userspace.USUser;
import org.hibernate.Session;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class TestUSUserLogin {

    private static String name = "DefaultUser";
    private static String pass = "filmtit";
    private static String email = "filmtit@gmail.com";
    private static String newPass = "filmtit2012";


    @BeforeClass
    public static void setupConfiguration() {
        Configuration configuration = new Configuration("configuration.xml");
        ConfigurationSingleton.setConf(configuration);
        MockHibernateUtil.changeUtilsInAllClasses();
    }

    private USHibernateUtil usHibernateUtil = MockHibernateUtil.getInstance();
    FilmTitBackendServer server = null;
     void TestUSUserLogin(){
        CreateServer();
     }

    void CreateServer(){
        server = new MockFilmTitBackendServer();
    }

    @Test
    public void testRegistration() {


     if (server ==null)  {CreateServer();};


       server.registration(name, pass, email, null);

       Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();
       List UserResult = dbSession.createQuery("select d from USUser d where d.userName ='"+name+"' ").list();
       usHibernateUtil.closeAndCommitSession(dbSession);

       assertFalse(UserResult.size()==0);

    }

   @Test
    public void testLogin(){

      if (server ==null)  {CreateServer();};
        if (server.simpleLogin(name,pass)!="")
       {
           server.registration(name,pass,email,null);
       }
       String session = server.simpleLogin(name, pass);


    }


    @Test
    public void testChangePass(){
       String string_token = "test001";
       if (server ==null)  {CreateServer();};
       server.createTestChange(name,string_token);
       server.changePassword(name,newPass,string_token);
       String session = server.simpleLogin(name, newPass);
       assertTrue("test pass",session != null);
    }


    @Test
    public void testUrlChange(){

        if (server ==null)  {CreateServer();};
        USUser user = new USUser(name,pass,email,null);
        server.sendChangePasswordMail(user);

    }

}



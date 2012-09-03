/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/

package cz.filmtit.userspace.tests;

import cz.filmtit.core.Configuration;
import cz.filmtit.core.ConfigurationSingleton;
import cz.filmtit.share.SessionResponse;
import cz.filmtit.share.exceptions.InvalidValueException;
import cz.filmtit.userspace.USHibernateUtil;
import cz.filmtit.userspace.USUser;
import cz.filmtit.userspace.login.ChangePassToken;
import cz.filmtit.userspace.servlets.FilmTitBackendServer;
import org.hibernate.Session;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.*;

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

    @AfterClass
    public static void clean() {
        MockHibernateUtil.clearDatabase();
    }

    private USHibernateUtil usHibernateUtil = MockHibernateUtil.getInstance();
    FilmTitBackendServer server = new MockFilmTitBackendServer();

    @Test
    public void testRegistration() throws InvalidValueException {
        server.registration(name, pass, email, null);

        Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();
        List userDbResult = dbSession.createQuery("select d from USUser d where d.userName ='"+name+"' ").list();
        usHibernateUtil.closeAndCommitSession(dbSession);

        USUser userFromDb = (USUser)userDbResult.get(0);

        assertFalse(userDbResult.size() == 0);
        assertEquals(name, userFromDb.getUserName());
        assertEquals(email, userFromDb.getEmail());
    }


    @Test
    public void testLogin() throws NoSuchFieldException, IllegalAccessException, InvalidValueException {
        // if not able to log in, register the user
        boolean succRegister = false;
        if (server.simpleLogin(name,pass) == null) {
            succRegister = server.registration(name, pass, email, null);
        }

        // succesful login
        if (succRegister){
            SessionResponse response = server.simpleLogin(name, pass);
            assertNotNull(response);

        // test if there is active session in the server
        Field activeSessionsField = FilmTitBackendServer.class.getDeclaredField("activeSessions");
        activeSessionsField.setAccessible(true);
        Map<String, Session> activeSessions = (Map<String, Session>) activeSessionsField.get(server);
        assertTrue(activeSessions.containsKey(response.sessionID));
        }
        else{
            assertEquals("Registration fail ",false,succRegister);
        }

        MockHibernateUtil.clearDatabase(); // clear database to be able to use the same creditals again
    }


    @Test
    public void testChangePass() throws NoSuchFieldException, IllegalAccessException {
        String stringToken = "test001";

        Field activeTokensField = FilmTitBackendServer.class.getDeclaredField("activeChangePassTokens");
        activeTokensField.setAccessible(true);
        Map<String, ChangePassToken> activeTokens = (Map<String, ChangePassToken>) activeTokensField.get(server);
        activeTokens.put(name, new ChangePassToken(stringToken));

        server.changePassword(name, newPass, stringToken);
        SessionResponse response = server.simpleLogin(name, newPass);
        //assertNotNull(response);
    }


    @Test  // TODO: what is this good for?
    public void testUrlChange(){
        USUser user = new USUser(name, pass, email, null);
        server.sendChangePasswordMail(user);
    }
}

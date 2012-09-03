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
import cz.filmtit.userspace.*;
import cz.filmtit.userspace.servlets.FilmTitBackendServer;
import org.hibernate.SessionFactory;
import org.hibernate.service.ServiceRegistryBuilder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JindĹ™ich LibovickĂ˝
 */
public class MockHibernateUtil extends USHibernateUtil {
    protected static MockHibernateUtil instance = null;

    protected MockHibernateUtil() {
        super();
    }

    public static USHibernateUtil getInstance() {
        if (instance == null) {
            instance = new MockHibernateUtil();
        }
        return instance;
    }


    // use the user space path instead of the original core one
    @Override
    public java.net.URL getConfigurationFile() {
        return MockHibernateUtil.class.getResource("/cz/filmtit/userspace/tests/userspace-test.cfg.xml");
    }

    @Override
    protected SessionFactory buildSessionFactory() {
        try {
            cz.filmtit.core.Configuration projectConfiguration = ConfigurationSingleton.conf();

            // Create the SessionFactory from core.cfg.xml
            org.hibernate.cfg.Configuration hibernateConfiguration = new org.hibernate.cfg.Configuration();

            hibernateConfiguration.configure(getConfigurationFile());

            serviceRegistry = new ServiceRegistryBuilder().applySettings(hibernateConfiguration.getProperties()).buildServiceRegistry();
            sessionFactory = hibernateConfiguration.buildSessionFactory(serviceRegistry);
            return sessionFactory;
        }
        catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            logger.error("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static void clearDatabase() {
        org.hibernate.Session dbSession = getInstance().getSessionWithActiveTransaction();
        dbSession.createQuery("delete from USTranslationResult").executeUpdate();
        dbSession.createQuery("delete from USDocument").executeUpdate();
        dbSession.createQuery("delete from USUser").executeUpdate();
        dbSession.createQuery("delete from Session").executeUpdate();
        getInstance().closeAndCommitSession(dbSession);
    }


    public static void changeUtilsInAllClasses() {
        Configuration configuration = new Configuration("configuration.xml");
        ConfigurationSingleton.setConf(configuration);

        List<Class<?>> classList = new ArrayList<Class<?>>();
        classList.add(FilmTitBackendServer.class);
        classList.add(Session.class);
        classList.add(DatabaseObject.class);

        for (Class c : classList) {
            Field hbUtilField = null;
            try {
                hbUtilField = c.getDeclaredField("usHibernateUtil");
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            hbUtilField.setAccessible(true);
            try {
                hbUtilField.set(null, getInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}

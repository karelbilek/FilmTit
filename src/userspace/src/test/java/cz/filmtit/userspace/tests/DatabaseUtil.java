package cz.filmtit.userspace.tests;

import cz.filmtit.userspace.HibernateUtil;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import java.lang.reflect.Field;

/**
 * Settings for the database for the Unit Tests.
 *
 * @author Jindřich Libovický
 */

public class DatabaseUtil {
    /**
     * Switches the database to the in-memory test database.
     * The method creates a Hibernate Session Factory and provides it to the HibernateUtil class before it is run
     * for the first time and the internals of the class are initialized.
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public static void setDatabase() {
        HibernateUtil.buildSessionFactoryFromHbmFile("cz/filmtit/userspace/tests/hibernate-test.cfg.xml");
    }
}

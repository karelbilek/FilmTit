package cz.filmtit.userspace.tests;

import cz.filmtit.core.CoreHibernateUtil;
import cz.filmtit.userspace.USHibernateUtil;

/**
 * Settings for the database for the Unit Tests.
 *
 * @author Jindřich Libovický
 */

public class DatabaseUtil {
    /**
     * Switches the database to the in-memory test database.
     * The method creates a Hibernate Session Factory and provides it to the USHibernateUtil class before it is run
     * for the first time and the internals of the class are initialized.
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public static void setDatabase() {
        CoreHibernateUtil.buildSessionFactoryFromHbmFile("cz/filmtit/core/tests/core-test.cfg.xml");
        USHibernateUtil.buildSessionFactoryFromHbmFile("cz/filmtit/userspace/tests/userspace-test.cfg.xml");
    }

}

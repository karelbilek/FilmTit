package cz.filmtit.userspace.tests;

import cz.filmtit.userspace.HibernateUtil;

import java.lang.reflect.Field;

/**
 * Settings for the database for the Unit Tests.
 *
 * @author Jindřich Libovický
 */

public class DatabaseUtil {
    public static void setDatabase() throws NoSuchFieldException, IllegalAccessException {
        Field configFile = HibernateUtil.class.getDeclaredField("configurationFile");
        configFile.setAccessible(true);
        configFile.set(null, "cz/filmtit/userspace/tests/hibernate-test.cfg.xml");
    }
}

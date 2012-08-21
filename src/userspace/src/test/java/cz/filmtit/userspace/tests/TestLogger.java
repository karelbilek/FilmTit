package cz.filmtit.userspace.tests;

import cz.filmtit.core.Configuration;
import cz.filmtit.core.ConfigurationSingleton;
import cz.filmtit.userspace.USLogger;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: josef.cech
 * Date: 7.8.12
 * Time: 8:17
 * To change this template use File | Settings | File Templates.
 */
public class TestLogger {

    @BeforeClass
    public static void setupConfiguration() {
        Configuration configuration = new Configuration("configuration.xml");
        ConfigurationSingleton.setConf(configuration);
        MockHibernateUtil.changeUtilsInAllClasses();

    }

    public TestLogger(){}

    @Test
    public void testInstance(){
        USLogger logger = USLogger.getInstance();
        assertNotNull(logger);
    }

    @Test
    public void testDebugNotice(){
        USLogger logger = USLogger.getInstance();
        logger.info("Test", "testing message");
    }
    @Test
    public void testWarning(){
        USLogger logger = USLogger.getInstance();
        logger.warning("Test", "testing warning message");

    }
    @Test
    public void testError(){
        USLogger logger = USLogger.getInstance();
        logger.error("Test", "testing error message");
    }
    @Test
    public void testDebug(){
        USLogger logger = USLogger.getInstance();
        logger.debug("Test", "testing debug message");
    }
}

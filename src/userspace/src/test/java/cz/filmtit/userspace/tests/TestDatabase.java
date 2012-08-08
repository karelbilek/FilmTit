package cz.filmtit.userspace.tests;

import cz.filmtit.core.Configuration;
import cz.filmtit.core.ConfigurationSingleton;
import cz.filmtit.share.TimedChunk;
import cz.filmtit.userspace.USHibernateUtil;
import cz.filmtit.userspace.USTranslationResult;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test if the database connection does not crash in some situations.
 *
 * @author Jindřich Libovický
 */
public class TestDatabase {
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

    /**
     * Runs multiple thread that works with database at the same moment.
     */
    @Test
    public void testMultiThreading() {
        for (int i = 0; i < 20; ++i) {
            new DatabaseWorker(i).run();
        }

        Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();
        dbSession.createQuery("delete from USTranslationResult");
        dbSession.getTransaction().commit();
    }

    private class DatabaseWorker extends Thread {
        DatabaseWorker(int count) {
            this.count = count;
        }

        int count;

        @Override
        public void run() {
            Session dbSession = usHibernateUtil.getSessionFactory().openSession();

            for(int i = 0; i < 200; ++i) {

                USTranslationResult sampleResult = new USTranslationResult(new TimedChunk("001", "002", 1, "Sample chunk", i, count));

                dbSession.buildLockRequest(LockOptions.UPGRADE);
                dbSession.beginTransaction();
                sampleResult.saveToDatabase(dbSession);
                dbSession.getTransaction().commit();
            }

            dbSession.close();
        }
    }
}

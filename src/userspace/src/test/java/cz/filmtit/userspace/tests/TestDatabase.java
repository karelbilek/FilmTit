package cz.filmtit.userspace.tests;

import cz.filmtit.share.TimedChunk;
import cz.filmtit.userspace.USHibernateUtil;
import cz.filmtit.userspace.USTranslationResult;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test if the database connection does not crash in some situations.
 *
 * @author Jindřich Libovický
 */
public class TestDatabase {
    @BeforeClass
    public static void InitializeDatabase() {
        DatabaseUtil.setDatabase();
    }

    /**
     * Runs multiple thread that works with database at the same moment.
     */
    @Test
    public void testMultiThreading() {
        for (int i = 0; i < 20; ++i) {
            new DatabaseWorker().run();
        }

        Session dbSession = USHibernateUtil.getSessionWithActiveTransaction();
        dbSession.createQuery("delete from USTranslationResult");
        dbSession.getTransaction().commit();
    }

    private class DatabaseWorker extends Thread {
        @Override
        public void run() {
            Session dbSession = USHibernateUtil.getSessionFactory().openSession();

            for(int i = 0; i < 200; ++i) {

                USTranslationResult sampleResult = new USTranslationResult(new TimedChunk("001", "002", 1, "Sample chunk", 5, 0));

                dbSession.buildLockRequest(LockOptions.UPGRADE);
                dbSession.beginTransaction();
                sampleResult.saveToDatabase(dbSession);
                dbSession.getTransaction().commit();
            }

            dbSession.close();
        }
    }
}

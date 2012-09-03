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

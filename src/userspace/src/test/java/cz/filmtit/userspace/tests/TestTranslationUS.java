package cz.filmtit.userspace.tests;

import cz.filmtit.userspace.HibernateUtil;
import cz.filmtit.userspace.TranslationUS;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestTranslationUS {
    @BeforeClass
    public static void setDatabase() throws NoSuchFieldException, IllegalAccessException {
        Field configFile = HibernateUtil.class.getDeclaredField("configurationFile");
        configFile.setAccessible(true);
        configFile.set(null, "cz/filmtit/userspace/tests/hibernate-test.cfg.xml");
    }

    @Test
    public void testDatabaseSaveAndLoad() {
        // create a sample translation object
        TranslationUS testTranslation = new TranslationUS();
        testTranslation.setText("This is an sample translation.");
        testTranslation.setScore(0.35f);
        testTranslation.setMatchDatabaseId(113);

        // save the translation to the database
        org.hibernate.Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        testTranslation.saveToDatabase(session);

        session.getTransaction().commit();
        
        // a database ID should have been assigned before...
        // ... we'll test if the default value has changed
        long databaseId = testTranslation.getDatabaseId();
        assertTrue(testTranslation.getDatabaseId() != Long.MIN_VALUE);

        //  load the translation from database again in different database transaction
        session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        List result = session.createQuery("select t from TranslationUS t where t.databaseId = :tid")
                .setParameter("tid", databaseId).list();

        session.getTransaction().commit();

        // exactly one result should be obtained
        assertEquals(result.size(), 1);
        TranslationUS returnedTranslation = (TranslationUS)result.get(0);
        
        //  finally test if all the TranslationUS properties are equal...
        assertEquals(testTranslation.getDatabaseId(), returnedTranslation.getDatabaseId());
        assertEquals(testTranslation.getMatchDatabaseId(), returnedTranslation.getMatchDatabaseId());
        assertEquals(testTranslation.getScore(), returnedTranslation.getScore(), 0.0001d);
        assertEquals(testTranslation.getText(), returnedTranslation.getText());
    }

    @Test
    public void testDelete() {
        // create a sample translation object
        TranslationUS testTranslation = new TranslationUS();
        testTranslation.setText("This is an sample translation.");
        testTranslation.setScore(0.35f);
        testTranslation.setMatchDatabaseId(113);

        org.hibernate.Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        testTranslation.saveToDatabase(session);

        session.getTransaction().commit();

        // test if there's one translation in the database
        assertTrue(isInDatabase(testTranslation));
        
        // now delete the translation in new db transaction
        session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        testTranslation.deleteFromDatabase(session);

        session.getTransaction().commit();

        // and test if there's no translation in the database
        assertFalse(isInDatabase(testTranslation));
    }

    /**
     * Test if two translations are the same.
     * @param t1 First translation.
     * @param t2 Second tranlsation.
     */
    public static void translationsEqual(TranslationUS t1, TranslationUS t2) {
        assertEquals(t1.getDatabaseId(), t2.getDatabaseId());
        assertEquals(t1.getMatchDatabaseId(), t2.getMatchDatabaseId());
        assertEquals(t1.getText(), t2.getText());
        assertEquals(t1.getScore(), t2.getScore(), 0.0001d);
    }

    /**
     * Tests if the translation t is in database.
     * @param t Tested translation.
     * @return True if it is in database, false otherwise.
     */
    public static boolean isInDatabase(TranslationUS t) {
        // the database transaction
        org.hibernate.Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        List result = session.createQuery("select t from TranslationUS t where t.databaseId = :tid")
                .setParameter("tid", t.getDatabaseId()).list();

        session.getTransaction().commit();

        // at most one occurrence should be found
        assertTrue(result.size() <= 1);

        // exactly one result...
        if (result.size() == 1) {
            return true;
        }
        // zero occurrences
        else if (result.size() == 0) {
            return false;
        }
        return false;
    }
}
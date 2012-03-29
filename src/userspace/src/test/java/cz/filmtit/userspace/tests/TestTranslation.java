package cz.filmtit.userspace.tests;

import cz.filmtit.userspace.*;
import org.junit.*;
import java.lang.reflect.*;
import java.util.List;

import junit.framework.JUnit4TestAdapter;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestTranslation {
    @BeforeClass
    public static void setDatabase() throws NoSuchFieldException, IllegalAccessException {
        Field configFile = HibernateUtil.class.getDeclaredField("configurationFile");
        configFile.setAccessible(true);
        configFile.set(null, "cz/filmtit/userspace/tests/hibernate-test.cfg.xml");
    }

    @Test
    public void testDatabaseSaveAndLoad() {
        // create a sample translation object
        Translation testTranslation = new Translation();
        testTranslation.setText("This is an sample translation.");
        testTranslation.setScore(0.35f);
        testTranslation.setMatchDatabaseId(113);

        // save the translation to the database
        testTranslation.saveToDatabase();
        
        // a database ID should have been assigned before...
        // ... we'll test if the default value has changed
        long databaseId = testTranslation.getDatabaseId();
        assertTrue(testTranslation.getDatabaseId() != Long.MIN_VALUE);

        //  load the translation from database again
        org.hibernate.Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        List result = session.createQuery("select t from Translation t where t.databaseId = :tid")
                .setParameter("tid", databaseId).list();

        session.getTransaction().commit();

        // exactly one result should be obtained
        assertEquals(result.size(), 1);
        Translation returnedTranslation = (Translation)result.get(0);
        
        //  finally test if all the Translation properties are equal...
        assertEquals(testTranslation.getDatabaseId(), returnedTranslation.getDatabaseId());
        assertEquals(testTranslation.getMatchDatabaseId(), returnedTranslation.getMatchDatabaseId());
        assertEquals(testTranslation.getScore(), returnedTranslation.getScore(), 0.0001d);
        assertEquals(testTranslation.getText(), returnedTranslation.getText());
    }

    @Test
    public void testDelete() {
        // create a sample translation object
        Translation testTranslation = new Translation();
        testTranslation.setText("This is an sample translation.");
        testTranslation.setScore(0.35f);
        testTranslation.setMatchDatabaseId(113);        
        testTranslation.saveToDatabase();

        // test if there's one translation in the database
        org.hibernate.Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        List result = session.createQuery("select t from Translation t where t.databaseId = :tid")
                .setParameter("tid", testTranslation.getDatabaseId()).list();

        session.getTransaction().commit();

        // exactly one result should be obtained
        assertEquals(result.size(), 1);
        
        // now delete the translation
        testTranslation.deleteFromDatabase();

        // and test if there's no translation in the database
        session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        result = session.createQuery("select t from Translation t where t.databaseId = :tid")
                .setParameter("tid", testTranslation.getDatabaseId()).list();

        session.getTransaction().commit();

        assertEquals(result.size(), 0);
    }
}
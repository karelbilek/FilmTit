package cz.filmtit.userspace.tests;

import cz.filmtit.userspace.DatabaseObject;
import cz.filmtit.userspace.HibernateUtil;
import cz.filmtit.userspace.MatchUS;
import cz.filmtit.userspace.TranslationUS;
import org.hibernate.Session;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TestMatchUS {
    @BeforeClass
    public static void setDatabase() throws NoSuchFieldException, 
        IllegalAccessException {
        TestTranslationUS.setDatabase();
    }

    /**
     * This test if the dependent translations are loaded from the database properly.
     */
    @Test
    public void testLoadFromDatabase() {
        // first create a sample match
        MatchUS match = new MatchUS();
        match.setChunkDatabaseId(1l);
        match.setText("This is a match.");

        // save the match do database using the private method
        try {
            Method saveMatch = DatabaseObject.class.getDeclaredMethod("saveJustObject");
            saveMatch.setAccessible(true);
            saveMatch.invoke(match);
        } catch (NoSuchMethodException e) {} catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        // create four sample translations and save them to database
        org.hibernate.Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        List<TranslationUS> sampleTranslations = generateSampleTranslations();
        for (TranslationUS translation : sampleTranslations) {
            translation.setMatchDatabaseId(match.getDatabaseId());
            translation.saveToDatabase(session);
        }

        session.getTransaction().commit();

        // load corresponding translations from database in new transaction
        session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();        
        
        match.loadTranslationsFromDatabase(session);
        
        session.getTransaction().commit();
        
        assertTrue(match.getTranslations().size() == 4);
        for (int i = 0; i < 4; ++i) {
            TestTranslationUS.translationsEqual(match.getTranslations().get(i),
                    sampleTranslations.get(i));
        }
    }

    /**
     * Generates a list of sample translations for test.
     * @return List of sample translations.
     */
    private List<TranslationUS> generateSampleTranslations() {
        List<TranslationUS> sampleTranslations = new ArrayList<TranslationUS>();

        for (int i = 1; i < 5; ++i) {
            TranslationUS translation = new TranslationUS();
            translation.setText("Sample translation no. " + Integer.toString(i));
            translation.setScore((double)i);
            sampleTranslations.add(translation);
        }

        return sampleTranslations;
    }

    /**
     * When the match is obtained from the translation memory, a match is 
     * created at one moment together with its translations. This tests if both
     * the match and the translations will save to the database
     * correctly.
     */
    @Test
    public void testConstructorAndSaving() {
        List<TranslationUS> sampleTranslations = generateSampleTranslations();

        MatchUS match = new MatchUS("This is a sample match.",
                sampleTranslations);

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        match.saveToDatabase(session);
        
        session.getTransaction().commit();

        // now test if database ID was assigned and if translations
        // got the match ID
        assertTrue(match.getDatabaseId() != Long.MIN_VALUE);
        for (TranslationUS t : sampleTranslations) {
            assertEquals(t.getMatchDatabaseId(), match.getDatabaseId());
        }

        // now load the match from database and test if its equal to the
        // previous one
        session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        List result = session.createQuery("select m from MatchUS t where m.databaseId = :mid")
                .setParameter("mid", match.getDatabaseId()).list();

        session.getTransaction().commit();

        assertEquals(result.size(), 1);
        matchesEqual(match, (MatchUS)(result.get(0)));
    }

    /**
     * This tests if all dependent translations are removed as well
     * when the match is deleted from the database.
     */
    @Test
    public void testDelete() {
        MatchUS match = new MatchUS("This is a second sample match.",
                generateSampleTranslations());

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        match.saveToDatabase(session);

        session.getTransaction().commit();

        // test if it is in the database
        assertTrue(isInDatabase(match));

        // delete it
        session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        match.deleteFromDatabase(session);
        
        session.getTransaction().commit();

        // test it is not in the database
        assertFalse(isInDatabase(match));
    }

    /**
     * Tests if two matches are equal.
     * @param m1
     * @param m2
     */
    public static void matchesEqual(MatchUS m1, MatchUS m2) {
        assertEquals(m1.getDatabaseId(), m2.getDatabaseId());
        assertEquals(m1.getChunkDatabaseId(), m2.getChunkDatabaseId());
        assertEquals(m1.getText(), m2.getText());
        assertEquals(m1.getTranslations().size(), m2.getTranslations().size());
        for (int i = 0; i < m1.getTranslations().size(); ++i) {
            TestTranslationUS.translationsEqual(m1.getTranslations().get(i),
                    m2.getTranslations().get(i));
        }
    }

    /**
     * Test if the match is in database. If the depending translations are
     * inconsistent with the the match, asserts it.
     * @param m The tested match.
     * @return  True if it is in database, false otherwise.
     */
    public static boolean isInDatabase(MatchUS m) {
        // the database transaction
        org.hibernate.Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        List result = session.createQuery("select t from MatchUS t where m.databaseId = :mid")
                .setParameter("tid", m.getDatabaseId()).list();

        session.getTransaction().commit();

        // at most one occurrence should be found
        assertTrue(result.size() <= 1);

        // exactly one result...
        if (result.size() == 1) {
            // also all the translations should be in the database
            for (TranslationUS t : m.getTranslations()) {
                assertTrue(TestTranslationUS.isInDatabase(t));
            }
            return true;
        }
        // zero occurrences
        else if (result.size() == 0) {
            // none of the depending translation should be in the database
            for (TranslationUS t : m.getTranslations()) {
                assertFalse(TestTranslationUS.isInDatabase(t));
            }
            
            return false;
        }
        return false;
    }
}

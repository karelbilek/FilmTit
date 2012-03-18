package cz.filmtit.userspace.tests;

import cz.filmtit.userspace.*;
import org.junit.*;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestTranslation {
    // some before class reflection changing the database mapping

    @Test
    public void databaseSaveAndLoad() {
        // create a sample translation object
        Translation testTranslation = new Translation();
        testTranslation.setText("This is an sample translation.");
        testTranslation.setScore(0.35f);
        testTranslation.setMatchDatabaseId(113);

        // save the translation to the database
        //testTranslation.saveToDatabase();
        
        // a database ID should have been assigned...
        // ... we test if the default value has changed
        //long databaseId = testTranslation.getDatabaseId();
        //assertTrue(testTranslation.getDatabaseId() != Long.MIN_VALUE);

        //  load the translation from database
        HibernateUtil.getSessionFactory();

    }
}
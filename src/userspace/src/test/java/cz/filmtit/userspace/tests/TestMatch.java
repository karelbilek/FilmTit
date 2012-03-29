package cz.filmtit.userspace.tests;

import cz.filmtit.userspace.*;
import org.junit.*;
import java.lang.reflect.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestMatch {
    @BeforeClass
    public static void setDatabase() throws NoSuchFieldException, IllegalAccessException {
        TestTranslation.setDatabase();
    }

    /**
     * This test if the dependent translations are loaded from the database properly.
     */
    @Test
    public void testLoadFromDatabase() {
        // first create a sample match
        Match match = new Match();
        match.setChunkDatabaseId(1l);
        match.setMatch("This is a match.");

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
        for (int i = 1; i < 5; ++i) {
            Translation translation = new Translation();
            translation.setMatchDatabaseId(match.getDatabaseId());
            translation.setText("Sample translation no. " + Integer.toString(i));
            translation.setScore((double)i);
            translation.saveToDatabase();
        }

        match.loadTranslationsFromDatabase();

        assertTrue(match.getTranslations().size() == 4);
        // test it more carefully ...
    }

    /**
     * When the match is obtained from the translation memory, a match is created at one moment together
     * with its translations. This tests if
     */
    @Test
    public void testConstructorAndSaving() {}

    /**
     * This tests if all dependent translations are removed as well
     * when the match is deleted from the database.
     */
    @Test
    public void testDelete() {}
}

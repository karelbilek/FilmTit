package cz.filmtit.userspace;

import java.util.*;
            
/**
 * Represents a match found in the TM.
 *
 * @author Jindřich Libovický
*/

public class Match {
    /**
     * Default constructor for Hibernate. It does nothing.
     */
    public Match() {
        translations = new ArrayList<Translation>();
    }

    // Here another constructor will follow for creating from TM

    // Method for loading the translation from User Space database

    private int databaseID;
    private String match;
    private List<Translation> translations;

    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    public Match(String match, List<Translation> translations) {
        this.match = match;
        this.translations = translations;
    }

    public int getDatabaseID() {
        return databaseID;
    }

    public void setDatabaseID(int databaseID) {
        this.databaseID = databaseID;
    }

    public List<Translation> getTranslations() {
        return translations;
    }
}

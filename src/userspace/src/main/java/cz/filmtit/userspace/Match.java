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

    private Long databaseId;
    private String match;
    private List<Translation> translations;
    private Long chunkDatabaseId = -1l;
    private boolean gotFromDb = false;

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

    public Long getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(Long databaseId) {
        this.databaseId = databaseId;
    }

    public List<Translation> getTranslations() {
        return translations;
    }

    public Long getChunkDatabaseId() {
        return chunkDatabaseId;
    }

    public void setChunkDatabaseId(Long chunkDatabaseID) {
        this.chunkDatabaseId = chunkDatabaseID;
    }

    public void loadTranslationsFromDatabase() {
        org.hibernate.Session session = UserSpace.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        // query the matches from the database
        List foundTranslations = session.createQuery("select t from Translations where m.chunkId = :tid")
                .setParameter("tid", databaseId).list();

        translations = new ArrayList<Translation>();
        for (Object t : foundTranslations) {
            translations.add((Translation)t);
        }

        session.getTransaction().commit();
    }

    /**
     * Saves the object to the database.
     */
    public void saveToDatabase() {
        org.hibernate.Session session = UserSpace.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        if (chunkDatabaseId == -1l) {
            throw(new IllegalStateException("The database ID of the parent chunk must be set" +
                    " before saving the object to database."));
        }

        if (gotFromDb) { session.update(this); }
        else { 
            session.save(this);
            setDatabaseId((Long) session.getIdentifier(this));
        }

        session.getTransaction().commit();
        
        // TODO: if it was a new object, is the databaseId set right now?
        
        for (Translation t : translations) {
            t.setMatchDatabaseId(databaseId);
            t.saveToDatabase();
        }
    }

    // TODO: Delete from database
}

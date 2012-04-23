package cz.filmtit.userspace;

import org.hibernate.Session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
            
/**
 * Represents a match found in the TM.
 *
 * @author Jindřich Libovický
*/

public class USMatch extends DatabaseObject {

    private Match match;
    /**
     * Collection of corresponding translations wrapped to the User Space
     * objects.
     */
    private List<USTranslation> translations;
    /**
     * Database ID of corresponding chunk in database. There's no need
     * to have a different connection to the predecessor because it's
     * never sent to the client without the whole chunk.
     */
    private long chunkDatabaseId = -1;

    /**
     * Default constructor for Hibernate. Creates the empty shared Match
     * object and expects it to be initialized by setters
     * (typically hibernate).
     */
    public USMatch() {
        match = new Match();
    }

    /**
     * Creates an instance of User Space Match from the shared Match.
     *
     * It just assigns it to the inner variable, User Space objects
     * wrapping the contained translations are created when necessary.
     * @param m A match from the shared module.
     */
    public USMatch(Match m) {
        match = m;
    }

    /**
     * Creates the Match object by providing explicitly its content.
     *
     * It is used when the Chunk receives the suggestions from the TM.
     * @param text
     * @param translations
     */
    public USMatch(String text, List<USTranslation> translations) {
        match = new Match();
        match.text = text;

        for (USTranslation t : translations) {
            match.translations.add(t.getSharedTranslation());
        }
        this.translations = Collections.synchronizedList(translations);
    }

    public String getText() {
        return match.text;
    }

    public void setText(String text) {
        if (match.text == null) { match.text = text; }
        else { throw new UnsupportedOperationException("The match text can be set just once."); }
    }

    public List<USTranslation> getTranslations() {
        // create it if it does not exist
        if (translations == null && match.translations != null) {
            List<USTranslation> newList = new ArrayList<USTranslation>();
            for ( Translation t : match.translations ) {
                USTranslation newTranslation = new USTranslation(t);
                newTranslation.setMatchDatabaseId(getDatabaseId());
                newList.add(new USTranslation(t));
            }
            translations = Collections.synchronizedList(newList);
        }
        // otherwise just return the value
        return translations;
    }

    public long getChunkDatabaseId() {
        return chunkDatabaseId;
    }

    public void setChunkDatabaseId(long chunkDatabaseID) {
        if (this.chunkDatabaseId == -1) {
            this.chunkDatabaseId = chunkDatabaseID;
        }
        else {
            throw new  UnsupportedOperationException("USTranslation text can be set just once.");
        }
    }

    public Match getSharedMatch() {
        return  match;
    }
    
    public void loadTranslationsFromDatabase(Session session) {
        // query the matches from the database
        List foundTranslations = session.createQuery("select t from USTranslation t where t.matchDatabaseId = :tid")
                .setParameter("tid", getDatabaseId()).list();

        List <USTranslation> newTranslations = new ArrayList<USTranslation>();
        for (Object t : foundTranslations) {
            newTranslations.add((USTranslation)t);
        }
        translations = Collections.synchronizedList(newTranslations);
    }

    /**
     * Saves the object to the database.
     */
    public void saveToDatabase(Session session) {
        saveJustObject(session);

        for (USTranslation t : getTranslations()) {
            t.setMatchDatabaseId(getDatabaseId());
            t.saveToDatabase(session);
        }
    }

    public void deleteFromDatabase(Session session) {
        deleteJustObject(session);
        for (USTranslation translation : getTranslations()) {
            translation.deleteFromDatabase(session);
        }
    }
}

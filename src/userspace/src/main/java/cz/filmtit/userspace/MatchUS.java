package cz.filmtit.userspace;

import cz.filmtit.share.Match;
import cz.filmtit.share.Translation;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
            
/**
 * Represents a match found in the TM.
 *
 * @author Jindřich Libovický
*/

public class MatchUS extends DatabaseObject {

    private Match match;
    /**
     * Collection of corresponding translations wrapped to the User Space
     * objects.
     */
    private List<TranslationUS> translationsUS;
    /**
     * Database ID of corresponding chunk in database. There's no need
     * to have a different connection to the predecessor because it's
     * never sent to the client without the whole chunk.
     */
    private Long chunkDatabaseId = Long.MIN_VALUE;

    /**
     * Default constructor for Hibernate. Creates the empty shared Match
     * object and expects it to be initialized by setters
     * (typically hibernate).
     */
    public MatchUS() {
        match = new Match();
    }

    /**
     * Creates an instance of User Space Match from the shared Match.
     *
     * It just assigns it to the inner variable, User Space objects
     * wrapping the contained translations are created when necessary.
     * @param m A match from the shared module.
     */
    public MatchUS(Match m) {
        match = m;
    }

    /**
     * Creates the Match object by providing explicitly its content.
     *
     * It is used when the Chunk receives the suggestions from the TM.
     * @param text
     * @param translations
     */
    public MatchUS(String text, List<TranslationUS> translations) {
        match = new Match();
        match.text = text;

        for (TranslationUS t : translations) {
            match.translations.add(t.getSharedTranslation());
        }
        translationsUS = Collections.synchronizedList(translations);
    }

    public String getText() {
        return match.text;
    }

    public void setText(String text) {
        if (match.text == null) { match.text = text; }
        else { throw new UnsupportedOperationException("The match text can be set just once."); }
    }

    public List<TranslationUS> getTranslations() {
        // create it if it does not exist
        if (translationsUS == null) {
            List<TranslationUS> newList = new ArrayList<TranslationUS>();
            for ( Translation t : match.translations ) {
                newList.add(new TranslationUS(t));
            }
            translationsUS = Collections.synchronizedList(newList);
        }
        // otherwise just return the value
        return translationsUS;
    }

    public Long getChunkDatabaseId() {
        return chunkDatabaseId;
    }

    public void setChunkDatabaseId(Long chunkDatabaseID) {
        if (chunkDatabaseID == Long.MIN_VALUE) {
            this.chunkDatabaseId = chunkDatabaseID;
        }
        else {
            throw new  UnsupportedOperationException("TranslationUS text can be set just once.");
        }
    }

    public Match getSharedMatch() {
        return  match;
    }
    
    public void loadTranslationsFromDatabase(Session session) {
        // query the matches from the database
        List foundTranslations = session.createQuery("select t from TranslationUS t where t.matchDatabaseId = :tid")
                .setParameter("tid", getDatabaseId()).list();

        List <TranslationUS> newTranslations = new ArrayList<TranslationUS>();
        for (Object t : foundTranslations) {
            newTranslations.add((TranslationUS)t);
        }
        translationsUS = Collections.synchronizedList(newTranslations);
    }

    /**
     * Saves the object to the database.
     */
    public void saveToDatabase(Session session) {
        saveJustObject(session);

        for (TranslationUS t : translationsUS) {
            t.setMatchDatabaseId(getDatabaseId());
            t.saveToDatabase(session);
        }
    }

    public void deleteFromDatabase(Session session) {
        deleteJustObject(session);
        for (TranslationUS translation : translationsUS) {
            translation.deleteFromDatabase(session);
        }
    }
}

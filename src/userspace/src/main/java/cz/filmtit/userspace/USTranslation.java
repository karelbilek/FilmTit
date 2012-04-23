package cz.filmtit.userspace;

/**
 * Represents a single translation returned by the TM.
 * It belongs to exactly one match.

 * @author Jindřich Libovický
*/

import org.hibernate.Session;

public class USTranslation extends DatabaseObject {

    private Translation translation;
    /**
     * Database ID of corresponding match in database. There's no need
     * to have a different connection to the predecessor because it's
     * never sent to the client without the whole chunk.
     */
    private long matchDatabaseId = -1l ;

    /**
     * Creates the translation object of given score and text.
     * @param text Text of the translation.
     * @param score Rank of the translation from TM.
     */
    public USTranslation(String text, double score) {
        translation = new Translation();
        translation.text = text;
        translation.score = score;
    }

    /**
     * Creates the User Space Translation object from the shared Translation
     * object.
     * @param t
     */
    public USTranslation(Translation t) {
        translation = t;
    }

    /**
     * Default constructor. Creates an uninitialized shared objects and expects
     * something (hibernate) to fill the fields using setters.
     */
    public USTranslation() {
        translation = new Translation();
    }

    public String getText() {
        return translation.text;
    }

    /**
     * Sets the text of tranlsation if it hasn't been done before.
     * @param text The text of tranlsation.
     * @throws UnsupportedOperationException In case of attempt to assign
     *          the text of translation more than once.
     */
    public void setText(String text) {
        if (translation.text == null) { translation.text = text; }
        else {
            throw new UnsupportedOperationException("Translation text can be set just once.");
        }
    }

    public double getScore() {
        return translation.score;
    }

    public void setScore(double score) {
        if (translation.score == Double.MIN_VALUE) { translation.score = score; }
        else { throw new UnsupportedOperationException("USTranslation text can be set just once."); }
    }

    public long getMatchDatabaseId() {
        return matchDatabaseId;
    }

    public void setMatchDatabaseId(long matchDatabaseId) {
        this.matchDatabaseId = matchDatabaseId;
    }

    Translation getSharedTranslation() {
        return translation;
    }

    public void saveToDatabase(Session session) {
        if (matchDatabaseId == -1l ) {
            throw(new IllegalStateException("The database ID of the parent match must be set" +
                    " before saving the object to database."));
        }
        saveJustObject(session);
    }

    public void deleteFromDatabase(Session session) {
        deleteJustObject(session);
    }
}

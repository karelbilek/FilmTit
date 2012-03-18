package cz.filmtit.userspace;

/**
 * Represents a single translation returned by the TM. Belongs to exactly one match.
 * @author Jindřich Libovický
*/

public class Translation extends DatabaseObject {
    /**
     * Creates the translation object of given score and text.
     * @param text Text of the translation.
     * @param score Rank of the translation from TM.
     */
    public Translation(String text, double score) {
        this.text = text;
        this.score = score;
    }

    /**
     * Default constructor. (Hibernate requires it.)
     */
    public Translation() {}

    private String text = null;
    private double score = Double.MIN_VALUE;
    private long matchDatabaseId = Long.MIN_VALUE;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (this.text == null) { this.text = text; }
        else { throw new UnsupportedOperationException("Translation text can be set just once."); }
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        if (this.score == Double.MIN_VALUE) { this.score = score; }
        else { throw new UnsupportedOperationException("Translation text can be set just once."); }
    }

    public long getMatchDatabaseId() {
        return matchDatabaseId;
    }

    public void setMatchDatabaseId(long matchDatabaseId) {
        this.matchDatabaseId = matchDatabaseId;
    }

    public void saveToDatabase() {
        if (matchDatabaseId == Long.MIN_VALUE) {
            throw(new IllegalStateException("The database ID of the parent match must be set" +
                    " before saving the object to database."));
        }
        saveJustObject();
    }

    public void deleteFromDatabase() {
        deleteJustObject();
    }
}

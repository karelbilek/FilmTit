package cz.filmtit.userspace;

/**
 * Represents a single translation returned by the TM. Belongs to exactly one match.
 *
 * @author Jindřich Libovický
*/

public class Translation {
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

    private String text;
    private double score;
    private int databaseId;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public double getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public int getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(int databaseId) {
        this.databaseId = databaseId;
    }
}

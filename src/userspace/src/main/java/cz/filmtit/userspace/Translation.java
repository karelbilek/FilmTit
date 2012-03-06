package cz.filmtit.userspace;

/**
 * Represents a single translation returned by the TM. Belongs to exactly one match.
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
    private Long databaseId;
    private Long matchDatabaseId = -1l;
    /**
     * A sign if the object was received from that databas (true)
     * or is still just in the memory.
     */
    private boolean gotFromDb = false;

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

    public Long getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(Long databaseId) {
        gotFromDb = true;
        this.databaseId = databaseId;
    }

    public Long getMatchDatabaseId() {
        return matchDatabaseId;
    }

    public void setMatchDatabaseId(Long matchDatabaseId) {
        this.matchDatabaseId = matchDatabaseId;
    }

    public void saveToDatabase() {
        org.hibernate.Session session = UserSpace.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        if (matchDatabaseId == -1) {
            throw(new IllegalStateException("The database ID of the parent match must be set" +
                    " before saving the object to database."));
        }

        if (gotFromDb) { session.update(this); }
        else { session.save(this);}

        session.getTransaction().commit();
    }

    // TODO: Delete from database
}

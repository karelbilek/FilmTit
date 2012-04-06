package cz.filmtit.userspace;

import org.hibernate.Session;

/**
 * An object which is stored in the database.
 * @author Jindřich Libovický
 */
public abstract class DatabaseObject {
    private long databaseId = Long.MIN_VALUE;
    /**
     * Sign if the object was load from the database (true) or is just in memory (false).
     */
    private boolean gotFromDb = false;

    public long getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(long databaseId) {
        if (this.databaseId == databaseId) { return; }
        if (this.databaseId == Long.MIN_VALUE) {
            this.databaseId = databaseId;
            gotFromDb = true;
        }
        else {
            throw new UnsupportedOperationException("Once the database ID is set, it cannot be changed");
        }
    }

    /**
     * Save the properties of the DatabaseObject to the database,
     * but not the dependent objects (like matches for chunks etc.).
     * @param session A database transaction in which operation is done.
     */
    protected void saveJustObject(Session session) {
        if (!gotFromDb) { // completely new object
            session.save(this);
        }
        else {           // just update an existing one
            session.update(this);
        }

    }

    /**
     * Deletes this object stored in the database,
     * but not the dependent objects (like matches for chunks etc.).
     * @param session A database transaction in which operation is done.
     */
    protected void deleteJustObject(Session session) {
        // object which is from the database, just cannot be removed
        if (!gotFromDb) { return; }

        // simply remove the corresponding line from db
        session.delete(this);
    }

    public abstract void saveToDatabase(Session session);
    public abstract void deleteFromDatabase(Session session);
}

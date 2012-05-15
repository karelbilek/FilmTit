package cz.filmtit.userspace;

import org.hibernate.Session;

/**
 * An object which is stored in the database.
 * @author Jindřich Libovický
 */
public abstract class DatabaseObject {
    protected long databaseId = Long.MIN_VALUE;
    /**
     * Sign if the object was load from the database (true) or is just in memory (false).
     */
    protected boolean gotFromDb = false;

    /**
     * Method that gets the database ID in case its also used in the shared class. If the database ID is not
     * shared, it should just return the inner database ID.
      * @return Database ID.
     */
    protected abstract long getSharedDatabaseId();

    /**
     * Sets the database ID which is in the shared class. If the database ID is not shared and is used for the
     * User Space purposes, it should have an empty body.
     * @param databaseId
     */
    protected abstract void setSharedDatabaseId(long databaseId);

    public long getDatabaseId() {
        return getSharedDatabaseId();
    }

    public void setDatabaseId(long databaseId) {
        if (this.databaseId == databaseId) { return; }
        if (this.databaseId == Long.MIN_VALUE) {
            this.databaseId = databaseId;
            setSharedDatabaseId(databaseId);
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
            session.save(this);  // USDocument throws an exception here
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

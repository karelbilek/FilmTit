package cz.filmtit.userspace;

import org.hibernate.Session;

/**
 * An object which is stored in the database. Contains the basic database functionality. Mostly
 * the wrappers of shared classes are extend this class.
 * @author Jindřich Libovický
 */
public abstract class DatabaseObject {
    /**
     * An identifier which is used in the database. If the object has not been stored in the database
     * before, a default value of Long.MIN_VALUE is used.
     */
    protected long databaseId = Long.MIN_VALUE;
    /**
     * A sign if the object was loaded from the database (true) or is just in memory (false)
     * and was created during a current session.
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
     * @param databaseId Database ID.
     */
    protected abstract void setSharedDatabaseId(long databaseId);

    /**
     * Get the unique database identifier of the object or the value of Long.MIN_VALUE if the
     * has not been in the database so far.
     * @return The database ID of the object or Long.MIN_VALUE if it has not been set yet.
     */
    public long getDatabaseId() {
        return getSharedDatabaseId();
    }

    /**
     * Sets the database ID of the object if it has not been set so far. It is supposed
     * to be called almost exclusively from the Hibernate library and it not
     * invoked from the actual code of our application.
     * @param databaseId The new database ID.
     * @exception UnsupportedOperationException An exception is thrown in the case a reassigning
     *   of the ID is attempted, i.e. every time when the ID is not equal to Long.MIN_VALUE

     */
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
     * but not the dependent objects (like matches for chunks etc.) which are not explicitly
     * included in the Hibernate mapping.
     * @param session A database transaction in which operation is done.
     *
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
     * Deletes the object from the database,
     * but not the dependent objects (like matches for chunks etc.) which are not explicitly
     * included in the Hibernate mapping.
     * @param session A database session which is the operation happening in.
     */
    protected void deleteJustObject(Session session) {
        // object which is from the database, just cannot be removed
        if (!gotFromDb) { return; }

        // simply remove the corresponding line from db
        session.delete(this);
    }

    /**
     * Deletes the object from the database including the whole structure which depends on the object,
     *  no matter if it is included in the Hibernate mapping or not.
     * @param session A database session which is the operation happening in.
     */
    public abstract void saveToDatabase(Session session);

    /**
     * Saves the object to the database including the whole structure which depends on the object,
     * no matter if it is included in the Hibernate mapping or not.
     * @param session A database session which is the operation happening in.
     */
    public abstract void deleteFromDatabase(Session session);
}

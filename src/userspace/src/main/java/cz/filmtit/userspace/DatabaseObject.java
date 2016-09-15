/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/

package cz.filmtit.userspace;

import org.hibernate.Session;
import org.jboss.logging.Logger;

/**
 * An object which is stored in the database. Contains the basic database functionality.
 * The wrappers of shared classes extend this class.
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
     * included in the shared class or the extended class is not a wrapper of a shared class,
     * it should just return the inner database ID from field databaseId in the DatabaseObject.
     * @return Database ID.
     */
    protected abstract long getSharedClassDatabaseId();

    /**
     * Sets the database ID and if it is necessary propagates the database ID setting to the wrapped object
     * if there is one. This setter is called from the setDatabaseId setter after the databaseId field is set
     * (which is the authoritative for the Hibernate mapping).
     * @param databaseId Database ID.
     */
    protected abstract void setSharedClassDatabaseId(long databaseId);

    /**
     * Get the unique database identifier of the object or the value of Long.MIN_VALUE if the
     * has not been in the database so far.
     * @return The database ID of the object or Long.MIN_VALUE if it has not been set yet.
     */
    public long getDatabaseId() {
        return getSharedClassDatabaseId();
    }

    Logger logger = Logger.getLogger("DatabaseObject");

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
            setSharedClassDatabaseId(databaseId);
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
    protected synchronized void saveJustObject(Session session) {
        if (this instanceof USTranslationResult) {
            USTranslationResult tr = (USTranslationResult) this;
            
            if (tr.getSharedId() < 0) {
                RuntimeException e = new RuntimeException("object : ShareID lesser than zero!");
                
                StringBuilder errorBuilder = new StringBuilder();
                StackTraceElement[] st = e.getStackTrace();
                for (StackTraceElement stackTraceElement : st) {
                    errorBuilder.append(stackTraceElement.toString());
                    errorBuilder.append("\n");
                }

                logger.error(errorBuilder.toString());

                throw new RuntimeException("session id < 0");
            }
        }
        

        if (databaseId == Long.MIN_VALUE) {
            session.save(this);
        }
        else {
            session.merge(this);
        }
    }

    /**
     * Deletes the object from the database,
     * but not the dependent objects (like matches for chunks etc.) which are not explicitly
     * included in the Hibernate mapping.
     * @param session A database session which is the operation happening in.
     */
    protected synchronized void deleteJustObject(Session session) {
        // object which is from the database, just cannot be removed
        if (!gotFromDb) { return; }

        // simply remove the corresponding line from db
        session.delete(this);
    }

    /**
     * Saves the object to the database including the whole structure which depends on the object,
     *  no matter if it is included in the Hibernate mapping or not.
     * @param session A database session which is the operation happening in.
     */
    public abstract void saveToDatabase(Session session);

    /**
     * Deletes the object from the database including the whole structure which depends on the object,
     * no matter if it is included in the Hibernate mapping or not.
     * @param session A database session which is the operation happening in.
     */
    public abstract void deleteFromDatabase(Session session);

    /**
     * Instance of the singleton class for managing database sessions.
     */
    protected static USHibernateUtil usHibernateUtil = USHibernateUtil.getInstance();
}

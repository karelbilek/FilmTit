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

import java.util.Date;

/**
 * A class representing a logging message from the client. Log messages are stored persistently in the database
 * in order enable later review of the logs.
 * @author Pepa Čech
 */
public class LogMessage extends DatabaseObject {
    /**
     * Type of the message converted to integer
     */
    private int messageType;
    /**
     * Context of the event
     */
    private String context;
    /**
     * Actual logged message
     */
    private String message;
    /**
     * Time of the event
     */
    private Date date;
    /**
     * User in whose session the event happened (may be null)
     */
    private USUser user;

    private LogMessage(){
        messageType = -1;
        context = new String();
        message = new String();
        date = new Date();
    }

    /**
     * Creates a new Log event of given properties.
     * @param type  Type of the event
     * @param context   Context in which the event happened (e.g. logging in, loading document etc.)
     * @param message   Actual text of the log event
     * @param logTime   Time when the event happened
     */
    public LogMessage(int type, String context , String message, Date logTime){
    	this(type, context, message, logTime, null);
    }

    /**
     * Creates a new Log event of given properties.
     * @param type  Type of the event
     * @param context   Context in which the event happened (e.g. logging in, loading document etc.)
     * @param message   Actual text of the log event
     * @param logTime   Time when the event happened
     * @param user  User who is logged in to the client
     */
    public LogMessage(int type, String context , String message, Date logTime, USUser user){
     setMessageType(type);
     setContext(context);
     setDate(logTime);
     setMessage(message);
     setUser(user);
    }

    /**
     * Sets the type of logged event as a string.
     * @param type Type of logged event as a string.
     */
    public void setMessageType(int type) {
        this.messageType = type;
    }

    /**
     * Sets the context of the event.
     * @param context Context of the event
     */
    public void setContext(String context) {
        this.context = context;
    }

    /**
     * Sets the log message.
     * @param message Log message.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Sets the time of the event.
     * @param logTime Time of the event
     */
    public void setDate(Date logTime) {
        this.date = logTime;
    }

	public void setUser(USUser user) {
		this.user = user;
	}

    /**
     * Gets type of the event as integer. (Used by Hibernate only.)
     * @return Type of the event as integer.
     */
    private int getMessageType() {
        return messageType;
    }

    /**
     * Gets the event context. (Used by Hibernate only.)
     * @return The event context.
     */
    private String getContext() {
        return context;
    }

    /**
     * Gets the event message. (Used by Hibernate only.)
     * @return The event message.
     */
    private String getMessage() {
        return message;
    }

    /**
     * Gets the time of the event. (Used by Hibernate only.)
     * @return Time of the event.
     */
    private Date getDate() {
        return date;
    }

    /**
     * Gets the user for who the event has been logged.
     * @return User that caused the event
     */
    private USUser getUser() {
		return user;
	}

    /**
     * Gets id of the user who caused the event. It is used by Hibernate only.
     * @return Id of the user who caused the event
     */
    private long getUserId() {
        if (user == null) { return -1; }
        return user.getDatabaseId();
    }

    /**
     * Setter of the id of user who caused the event. It does nothing but it is required
     * by Hibernate to be present in the class.
     * @param userId An arbitrary number
     */
    private void setUserId(long userId) {}

    /**
     * Since it does not wrap any object, it gets directly the database ID.
     * @return The database ID.
     */
    @Override
    protected long getSharedClassDatabaseId() {
        return databaseId;
    }

    /**
     * Does nothing because it is not a shared class wrapper.
     * @param databaseId Database ID.
     */
    @Override
    protected void setSharedClassDatabaseId(long databaseId) {}

    /**
     * Saves the object to the database
     * @param session An active database session.
     */
    @Override
    public void saveToDatabase(Session session) {
        saveJustObject(session);
    }

    /**
     * Deletes the object from the database.
     * @param session An active database session.
     */
    @Override
    public void deleteFromDatabase(Session session) {
       deleteJustObject(session);
    }
}

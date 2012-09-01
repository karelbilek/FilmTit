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

    public LogMessage(int type, String context , String message, Date logTime){
    	this(type, context, message, logTime, null);
    }
    
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


    private USUser getUser() {
		return user;
	}

    private long getUserId() {
        if (user == null) { return -1; }
        return user.getDatabaseId();
    }

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

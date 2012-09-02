package cz.filmtit.share;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

/**
 * An object of SessionResponse class is sent the GUI when a user successfully logs in
 * to the application. It contains the user unique session ID and the user object
 * containing settings of the user without the list of the documents the user own.
 *
 * @author Jindřich Libovický
 */

public class SessionResponse implements Serializable, IsSerializable {
    /**
     * A default constructor required by GWT.
     */
    public SessionResponse() {}

    /**
     * Creates a SessionResponse object of given properties.
     * @param sessionID Newly generated session ID
     * @param userWithoutDocs Shared user object not contains documents.
     */
    public SessionResponse(String sessionID, User userWithoutDocs) {
        this.sessionID = sessionID;
        this.userWithoutDocs = userWithoutDocs;
    }

    /**
     * Newly generated session ID
     */
    public String sessionID;
    /**
     * Shared user object not contains documents.
     */
    public User userWithoutDocs;
}

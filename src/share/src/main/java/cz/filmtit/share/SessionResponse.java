package cz.filmtit.share;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

public class SessionResponse implements Serializable, IsSerializable {
    public SessionResponse() {}

    public SessionResponse(String sessionID, User userWithoutDocs) {
        this.sessionID = sessionID;
        this.userWithoutDocs = userWithoutDocs;
    }

    public String sessionID;
    public User userWithoutDocs;
}

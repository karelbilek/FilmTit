package cz.filmtit.userspace;

/*
    - whenever a JSON message comes, update the lastOperation
    - pass further all the JSON messages except logging out
 */

/**
 * Represents a running session.
 * @author Jindřich Libovický
 */
public class Session {
    private int sessionId;
    private User user;
    private long sessionStart;
    private long lastOperation;

    enum state {active, loggedOut, terminated, kill}

    public long getLastOperation() {
        return lastOperation;
    }

    public void setLastOperation(long lastOperation) {
        this.lastOperation = lastOperation;
    }

    public User getUser() {
        return user;
    }

    public void Logout() {
        // save everything to database and write a log of this session
    }

    /**
     * Terminates the session. Usually in the situation when the user open a new one.
     */
    public void Terminate() {
        // save everything to database and write a log of this session
    }

    /**
     * Kills the session when it times out.
     */
    public void Kill() {
        // save everything to database and write a log of this session
    }
}

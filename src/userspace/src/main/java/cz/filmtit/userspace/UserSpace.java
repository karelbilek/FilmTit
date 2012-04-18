package cz.filmtit.userspace;

/*
    - time out limit to a configuration file
    - should pass forward the JSON communication for existing sessions
    - be able to start new sessions
 */

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * A singleton class which represents the running USUser Space
 * @author Jindřich Libovický
 */
public class UserSpace {
    private static UserSpace instance = null;

    private UserSpace() { }

    public static UserSpace getInstance() {
        if (instance == null) {
            instance = new UserSpace();
        }
        return instance;
    }
    
    public void start()  {
        new WatchingTimeOut().run();
    } 

    /**
     * The time limit when a session should be terminated as timed out. [in millisecond]
     */
    static final long SESSION_TIME_OUT_LIMIT = 1000 * 60 * 20;
    /**
     * The table of active sessions. Contains mapping fom session IDs to the Session objects.
     */
    private Map<Long, Session> runningSessions;
    /**
     * The set of currently logged-in users. Necessary because a user can be logged in just once.
     */
    private Set<USUser> loggedInUsers;

    /**
     * A thread that checks out whether the sessions are timed out.
     */
    class WatchingTimeOut extends Thread {
        public void run() {
            while(true) {
                for (Long sessionId : runningSessions.keySet()) {
                    long now = new Date().getTime();
                    Session thisSession = runningSessions.get(sessionId);
                    if (thisSession.getLastOperation() + SESSION_TIME_OUT_LIMIT < now) {
                        loggedInUsers.remove(thisSession.getUser());
                        thisSession.Kill();
                        runningSessions.remove(sessionId);
                    }
                }
                try { Thread.sleep(60 * 1000); }
                catch (Exception e) {}
            }
        }
    }
}

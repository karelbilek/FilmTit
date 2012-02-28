package cz.filmtit.userspace;

/*
    - time out limit to a configuration file
    - should pass forward the JSON communication for existing sessions
    - be able to start new sessions
 */

import java.util.*;
/**
 * A singleton class which represents the running User Space
 * @author Jindřich Libovický
 */
public class UserSpace {
    static final long TIME_OUT_LIMIT = 1000 * 60 * 20;
    Map<Integer, Session> runningSessions;
    Set<User> loggedInUsers;

    /**
     * A thread that checks out whether the sessions are timed out.
     */
    class WatchingTimeOut extends Thread {
        public void run() {
            while(true) {
                for (int sessionId : runningSessions.keySet()) {
                    long now = new Date().getTime();
                    Session thisSession = runningSessions.get(sessionId);
                    if (thisSession.getLastOperation() + TIME_OUT_LIMIT < now) {
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

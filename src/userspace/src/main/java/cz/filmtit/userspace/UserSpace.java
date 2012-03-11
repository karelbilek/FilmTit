package cz.filmtit.userspace;

/*
    - time out limit to a configuration file
    - should pass forward the JSON communication for existing sessions
    - be able to start new sessions
 */

import java.util.*;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * A singleton class which represents the running User Space
 * @author Jindřich Libovický
 */
public class UserSpace {
    static final long TIME_OUT_LIMIT = 1000 * 60 * 20;
    private Map<Integer, Session> runningSessions;
    private Set<User> loggedInUsers;

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

    // Hibernate utils start here
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
            return new Configuration().configure().buildSessionFactory();
        }
        catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}

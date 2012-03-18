package cz.filmtit.userspace;

/*
    - time out limit to a configuration file
    - should pass forward the JSON communication for existing sessions
    - be able to start new sessions
 */

import java.io.*;
import java.util.*;
import java.net.*;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.json.*;

/**
 * A singleton class which represents the running User Space
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
        new Server().run();
        new WatchingTimeOut();
    } 

    /**
     * The time limit when a session should be terminated as timed out. [in millisecond]
     */
    static final long SESSION_TIME_OUT_LIMIT = 1000 * 60 * 20;
    /**
     * Number of port where the User Space service runs.
      */
    static final int PORT_NUMBER = 6789;
    /**
     * The table of active sessions. Contains mapping fom session IDs to the Session objects.
     */
    private Map<Long, Session> runningSessions;
    /**
     * The set of currently logged-in users. Necessary because a user can be logged in just once.
     */
    private Set<User> loggedInUsers;

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

    // ===============================
    // Server application starts here
    // ===============================

    public class Server extends Thread {
        protected ServerSocket listen_socket;

        // Exit with an error message, when an exception occurs.
        public void fail(Exception e, String msg) {
            System.err.println(msg + ": " +  e);
            System.exit(1);
        }

        public Server() {
            try { listen_socket = new ServerSocket(PORT_NUMBER); }
            catch (IOException e) { fail(e, "Exception creating server socket"); }
            System.out.println("Server: listening on port " + PORT_NUMBER);
            this.start();
        }

        public void run() {
            try {
                while(true) {
                    Socket client_socket = listen_socket.accept();
                    Connection c = new Connection(client_socket);
                }
            }
            catch (IOException e) {
                fail(e, "Exception while listening for connections");
            }
        }
    }

    // This class is the thread that handles all communication with a client
    class Connection extends Thread {
        protected Socket client;
        protected DataInputStream in;
        protected PrintStream out;

        // Initialize the streams and start the thread
        public Connection(Socket client_socket) {
            client = client_socket;
            try {
                in = new DataInputStream(client.getInputStream());
                out = new PrintStream(client.getOutputStream());
            }
            catch (IOException e) {
                try { client.close(); } catch (IOException e2) { ; }
                System.err.println("Exception while getting socket streams: " + e);
                return;
            }
            this.start();
        }

         public void run() {
            String line;
            int len;
            try {
                for(;;) {
                    JSONObject request = new JSONObject(in.readUTF());
                }
            }
            catch (Exception e) { ; }
            finally { try {client.close();} catch (IOException e2) {;} }
        }
    }


}

package cz.filmtit.userspace;

import cz.filmtit.share.User;
import org.hibernate.Session;

import java.util.*;

/**
 * Represents a user of the application in the user space. It is a wrapper of the User
 * class from the share namespace.
 * @author Jindřich Libovický
 */
public class USUser extends DatabaseObject {
    User user;
    String userName;
    String password;
    String email;
    String openId;
    boolean permanentlyLoggedId;
    /**
     * Creates a new user given his user name. It is used in cases a user logs for the first time
     * in the application.
     * @param userName The name of the new user.
     * @param password - The new password
     * @param email - The email of new user
     */

    public USUser(String userName, String password ,String email, String openId) {


        this.user = new User();
        user.setName(userName);


        this.userName = userName;
        this.password = password;
        this.email = email;
        this.openId = openId;

        ownedDocuments = Collections.synchronizedSet(new HashSet<USDocument>());
        activeDocumentIDs = Collections.synchronizedSet(new HashSet<Long>());
    }
    /**
     * Creates a new user given his user name. It is used in cases a user logs for the first time
     * in the application.
     * @param userName The name of the new user.

     */

    public USUser(String userName) {


        this.user = new User();
        user.setName(userName);


        this.userName = userName;
        this.password = null;
        this.email = null;
        this.openId =null;

        ownedDocuments = Collections.synchronizedSet(new HashSet<USDocument>());
        activeDocumentIDs = Collections.synchronizedSet(new HashSet<Long>());
    }

    /**
     * Default constructor used by Hibernate.
     */
    private USUser() {
        this.user = new User();
        ownedDocuments = null; //new ArrayList<USDocument>();
        activeDocumentIDs = new HashSet<Long>();
    }

    /**
     * A list of the documents owned by the user stored as the User Space wrappers of the
     * Document objects from the share namespace.
     */
    private Set<USDocument> ownedDocuments = null;
    /**
     * A set of IDs of documents which were active at the moment the user logged out (or was logged
     * out) last time. It is not kept up to date while a Session exists. It is updated at the moment
     * the session is terminated and everything is stored to the database.
     */
    private Set<Long> activeDocumentIDs;


    /**
     * Gets the list of documents owned by this user.
     * @return List of USDocument objects wrapping the Document objects, but with empty suggestion lists
     */
    public Set<USDocument> getOwnedDocuments() {
        //  if the list of owned documents is empty...

        if (ownedDocuments == null) {
            ownedDocuments = Collections.synchronizedSet(new HashSet<USDocument>());
            org.hibernate.Session session = usHibernateUtil.getSessionWithActiveTransaction();

            // query the documents owned by the user
            List result = session.createQuery("select d from USDocument d where d.ownerDatabaseId = :uid " +
                    "and d.toBeDeleted = false").setParameter("uid", getDatabaseId()).list();

            // store it to the variable
            for (Object o : result) { ownedDocuments.add((USDocument)o); }
            
            usHibernateUtil.closeAndCommitSession(session);
        }
        return ownedDocuments;
    }

    protected void setSharedClassDatabaseId(long id) { }
    protected long getSharedClassDatabaseId() { return databaseId; }

    public String getUserName() {
        return user.getName();
    }
    private void setUserName(String name) {
        user.setName(name);
    }

    public String getOpenId() {
        return openId;
    }
    private void setOpenId(String id) {
        openId = id;
    }

    public Set<Long> getActiveDocumentIDs() {
        return activeDocumentIDs;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }
    private void setEmail(String email) {
        this.email = email;
    }

    public boolean isPermanentlyLoggedId() {
        return permanentlyLoggedId;
    }

    public void setPermanentlyLoggedId(boolean permanentlyLoggedId) {
        this.permanentlyLoggedId = permanentlyLoggedId;
    }

    /**
     * Sets the set of document ID which are active at the time a session is terminated.
     * Used by the Hibernate mapping.
     * @param activeDocumentIDs
     */
    private void setActiveDocumentIDs(Set<Long> activeDocumentIDs) {
        this.activeDocumentIDs = activeDocumentIDs;
    }

    //adds document into server memory
    //it doesn't add it into database, it is added into database in document constructor
    public void addDocument(USDocument document) {
        ownedDocuments.add(document);
    //    document.setOwnerDatabaseId(databaseId);
    }

    public void saveToDatabase(Session dbSession) {
        saveJustObject(dbSession);
    }

    public void deleteFromDatabase(Session dbSession) {
        deleteJustObject(dbSession);
        for (USDocument document : ownedDocuments) {
            document.deleteFromDatabase(dbSession);
        }
    }
}

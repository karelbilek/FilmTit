package cz.filmtit.userspace;

import cz.filmtit.share.User;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a user of the application in the user space. It is a wrapper of the User
 * class from the share namespace.
 * @author Jindřich Libovický
 */
public class USUser extends DatabaseObject {

    User user;

    /**
     * Creates a new user given his user name. It is used in cases a user logs for the first time
     * in the application.
     * @param userName The name of the new user.
     */
    public USUser(String userName) {
        this.user = new User();
        user.setName(userName);
        ownedDocuments = new ArrayList<USDocument>();
        activeDocumentIDs = new HashSet<Long>();
    }

    /**
     * Default constructor used by Hibernate.
     */
    private USUser() {}

    /**
     * A list of the documents owned by the user stored as the User Space wrappers of the
     * Document objects from the share namespace.
     */
    private List<USDocument> ownedDocuments;
    /**
     * A set of IDs of documents which were active at the moment the user logged out (or was logged
     * out) last time. It is not kept up to date while a Session exists. It is updated at the moment
     * the session is terminated and everything is stored to the database.
     */
    private Set<Long> activeDocumentIDs;


    /**
     * Gets the list of documents owned by this user.
     * @return List of USDocument objects wrapping the Document objects
     */
    public List<USDocument> getOwnedDocuments() {
        //  if the list of owned documents is empty...
        if (ownedDocuments == null) {

            ownedDocuments = new ArrayList<USDocument>();
            org.hibernate.Session session = HibernateUtil.getSessionWithActiveTransaction();

            // query the documents owned by the user
            List result = session.createQuery("select d from USDocument d where d.ownerDatabaseId = :uid")
                    .setParameter("uid", getDatabaseId()).list();

            // store it to the variable
            for (Object o : result) { ownedDocuments.add((USDocument)o); }
            
            HibernateUtil.closeAndCommitSession(session);
        }
        return ownedDocuments;
    }

    protected void setSharedDatabaseId(long id) { }
    protected long getSharedDatabaseId() { return databaseId; }

    public String getUserName() {
        return user.getName();
    }
    private void setUserName(String name) {
        user.setName(name);
    }

    public String getOpenId() {
        return user.getId();
    }
    private void setOpenId(String id) {
        user.setId(id);
    }

    public Set<Long> getActiveDocumentIDs() {
        return activeDocumentIDs;
    }

    /**
     * Sets the set of document ID which are active at the time a session is terminated.
     * Used by the Hibernate mapping.
     * @param activeDocumentIDs
     */
    private void setActiveDocumentIDs(Set<Long> activeDocumentIDs) {
        this.activeDocumentIDs = activeDocumentIDs;
    }

    public void addDocument(USDocument document) {
        ownedDocuments.add(document);
        document.setOwnerDatabaseId(databaseId);
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

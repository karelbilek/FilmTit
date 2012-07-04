package cz.filmtit.userspace;

import cz.filmtit.share.User;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/* what may come from the interface
    - administrating the account =
        - change password
        - change username
        - change the way of authorization
    - get the list of existing documents
    - create a new document
    - choosing a document to edit (and save the previous to database)
    - pass further the editing of an active document
 */

/**
 * Represents a user of the application in the user space
 * @author Jindřich Libovický
 */
public class USUser extends DatabaseObject {

    User user;

    public USUser(String userName) {
        this.user = new User();
        user.setName(userName);
        ownedDocuments = new ArrayList<USDocument>();
        activeDocumentIDs = new HashSet<Long>();
    }

    /**
     * Default constructor for Hibernate.
     */
    private USUser() {}

    private List<USDocument> ownedDocuments;
    private Set<Long> activeDocumentIDs;
    /**
     * Sign if the active document was created in the current session and therefore is not yet in the database.
     */
    private boolean activeIsNew;  // is it necessary ???  I guess it's not

    /**
     * Gets the list of documents owned by this user.
     * @return List of USDocument objects
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

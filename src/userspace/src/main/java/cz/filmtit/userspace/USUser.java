package cz.filmtit.userspace;

import cz.filmtit.core.ConfigurationSingleton;
import cz.filmtit.share.User;
import org.hibernate.Session;

import java.util.*;

/**
 * Represents a user of the application in the user space. It is a wrapper of the User
 * class from the share namespace.
 * @author Jindřich Libovický
 */
public class USUser extends DatabaseObject {
    /**
     * The wrapped shared object.
     */
    private User user;

    /**
     * Hash of the user password. (In case user doesn't use open id.)
     */
    private volatile String password;
    /**
     * Open ID identifier of the user.
     */
    private volatile String openId;

    /**
     * Creates a new user given his user name and other credits. It is used in cases a user logs for the first time
     * in the application.
     * @param userName The name of the new user.
     * @param password The new password
     * @param email The email of new user
     */
    public USUser(String userName, String password, String email, String openId) {
        this.user = new User();
        user.setName(userName);
        this.password = password;
        user.setEmail(email);
        this.openId = openId;

        setUseMoses(true);
        setPermanentlyLoggedId(false);
        setMaximumNumberOfSuggestions(ConfigurationSingleton.conf().maximumSuggestionsCount());

        ownedDocuments = Collections.synchronizedMap(new HashMap<Long, USDocument>());
    }

    /**
     * Creates a new user given his user name. It is used in cases a user logs for the first time
     * in the application.
     * @param userName The name of the new user.
     */
    public USUser(String userName) {
        this.user = new User();
        user.setName(userName);

        this.password = null;
        this.openId = null;

        setUseMoses(true);
        setPermanentlyLoggedId(false);
        setMaximumNumberOfSuggestions(ConfigurationSingleton.conf().maximumSuggestionsCount());

        ownedDocuments = Collections.synchronizedMap(new HashMap<Long, USDocument>());
    }

    /**
     * Private default constructor used by Hibernate.
     */
    private USUser() {
        this.user = new User();
        ownedDocuments = null;
    }

    /**
     * A list of the documents owned by the user stored as the User Space wrappers of the
     * Document objects from the share namespace.
     */
    private Map<Long, USDocument> ownedDocuments = null;

    /**
     * Gets the list of documents owned by this user.
     * @return List of USDocument objects wrapping the Document objects, but with empty suggestion lists
     */
    public synchronized Map<Long, USDocument> getOwnedDocuments() {
        //  if the list of owned documents is empty...
        if (ownedDocuments == null) {
            ownedDocuments = Collections.synchronizedMap(new HashMap<Long, USDocument>());
            org.hibernate.Session session = usHibernateUtil.getSessionWithActiveTransaction();

            // query the documents owned by the user
            List result = session.createQuery("select d from USDocument d where d.ownerDatabaseId = :uid " +
                    "and d.toBeDeleted = false").setParameter("uid", getDatabaseId()).list();

            // store it to the variable
            for (Object o : result) {
                USDocument doc = (USDocument)o;
                doc.setOwner(this);
                ownedDocuments.put(doc.getDatabaseId(), doc);
            }
            
            usHibernateUtil.closeAndCommitSession(session);
        }
        return ownedDocuments;
    }

    protected void setSharedClassDatabaseId(long id) { user.setId(id); }
    protected long getSharedClassDatabaseId() { return user.getId(); }

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

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return user.getEmail();
    }

    public void setEmail(String email) {
        user.setEmail(email);
    }

    public boolean isPermanentlyLoggedId() {
        return user.isPermanentlyLoggedIn();
    }

    public void setPermanentlyLoggedId(boolean permanentlyLoggedId) {
        user.setPermanentlyLoggedIn(permanentlyLoggedId);
    }

    public int getMaximumNumberOfSuggestions() {
        return user.getMaximumNumberOfSuggestions();
    }

    public void setMaximumNumberOfSuggestions(int maximumNumberOfSuggestions) {
        user.setMaximumNumberOfSuggestions(maximumNumberOfSuggestions);
    }

    /**
     * Gets the flag if the wants to include results of machine translation together with the suggestions.
     * @return  Flag if the user wants machine translation.
     */
    public boolean getUseMoses() {
         return user.getUseMoses();
    }

    /**
     * Sets the flag if the wants to include results of machine translation together with the suggestions.
     * @param useMoses Flag if the user wants machine translation.
     */
    public void setUseMoses(boolean useMoses) {
        user.setUseMoses(useMoses);
    }

    /**
     * Adds a document to the in-memory collection of documents owned by the user. The document itself including
     * it's connection to the user are saved to the already in the document constructor.
     * @param document
     */
    public void addDocument(USDocument document) {
        getOwnedDocuments().put(document.getDatabaseId(), document);
    }

    /**
     * Saves the user (not the document he owns) to the database.
     * @param dbSession An active database session.
     */
    public void saveToDatabase(Session dbSession) {
        saveJustObject(dbSession);
    }

    /**
     * Deletes the user from the database and marks all the document he own as ready to be deleted.
     * @param dbSession An active database session.
     */
    public void deleteFromDatabase(Session dbSession) {
        deleteJustObject(dbSession);
        for (USDocument document : ownedDocuments.values()) {
            document.setToBeDeleted(true);
            document.saveToDatabase(dbSession);
        }
    }

    /**
     * Gets the wrapped user object clone without documents. It is used to send the client information about the
     * user after user logs in.
     * @return User object without documents.
     */
    public User sharedUserWithoutDocuments() {
        return user.getCloneWithoutDocuments();
    }
}

/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/
package cz.filmtit.userspace;

import cz.filmtit.core.ConfigurationSingleton;
import cz.filmtit.share.User;
import org.hibernate.Session;

import java.util.*;
import org.hibernate.Query;

/**
 * Represents a user of the application in the user space. It is a wrapper of
 * the User class from the share namespace.
 *
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
     * Creates a new user given his user name and other credits. It is used in
     * cases a user logs for the first time in the application.
     *
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
     * Creates a new user given his user name. It is used in cases a user logs
     * for the first time in the application.
     *
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
     * Public default constructor used by Hibernate.
     */
    public USUser() {
        this.user = new User();
        ownedDocuments = null;
    }

    /**
     * A list of the documents owned by the user stored as the User Space
     * wrappers of the Document objects from the share namespace.
     */
    private Map<Long, USDocument> ownedDocuments = null;

    /**
     * Gets the list of documents owned by this user.
     *
     * @return List of USDocument objects wrapping the Document objects, but
     * with empty suggestion lists
     */
    public synchronized Map<Long, USDocument> getOwnedDocuments() {
        //  if the list of owned documents is empty...
        if (ownedDocuments == null) {
            ownedDocuments = Collections.synchronizedMap(new HashMap<Long, USDocument>());
            org.hibernate.Session session = usHibernateUtil.getSessionWithActiveTransaction();

            // query the documents owned by the user
            List result = session.createQuery("select d from USDocument d where d.ownerDatabaseId = :uid "
                    + "and d.toBeDeleted = false").setParameter("uid", getDatabaseId()).list();

            // store it to the variable
            for (Object o : result) {
                USDocument doc = (USDocument) o;
                doc.setOwner(this);
                ownedDocuments.put(doc.getDatabaseId(), doc);
            }

            usHibernateUtil.closeAndCommitSession(session);
        }
        return ownedDocuments;
    }

    private List<USDocument> accessibleDocuments;

    /**
     * @return the accessibleDocuments
     */
    public List<USDocument> getAccessibleDocuments() {
        accessibleDocuments = new ArrayList<USDocument>();

        org.hibernate.Session session = usHibernateUtil.getSessionWithActiveTransaction();

        Query query = session.createQuery("SELECT d FROM USDocument d RIGHT JOIN d.documentUsers DocumentUsers WHERE DocumentUsers.userId = :userId");
        query.setParameter("userId", getDatabaseId());
        
        List list = query.list();

        for (Object o : list) {
            accessibleDocuments.add((USDocument) o);
        }

        usHibernateUtil.closeAndCommitSession(session);

        return accessibleDocuments;
    }

    /**
     * Propagates setting the database ID from the setDatabaseId setter to the
     * wrapped object.
     *
     * @param id Database ID.
     */
    protected void setSharedClassDatabaseId(long id) {
        getUser().setId(id);
    }

    /**
     * Supplies the id value from the wrapped object to the getDatabaseId getter
     * of the parent DatabaseObject.
     *
     * @return Identifier from the wrapped object.
     */
    protected long getSharedClassDatabaseId() {
        return getUser().getId();
    }

    /**
     * Gets the name of the user. (Calls the wrapped object.)
     *
     * @return User name.
     */
    public String getUserName() {
        return getUser().getName();
    }

    /**
     * Sets the user name. (Calls the wrapped object.) Used by Hibernate only.
     *
     * @param name User name
     */
    public void setUserName(String name) {
        getUser().setName(name);
    }

    /**
     * Gets the user's openId identifier. Used by Hibernate only.
     *
     * @return OpenID
     */
    private String getOpenId() {
        return openId;
    }

    /**
     * Sets the user's openId identifier. User by Hibernate only.
     *
     * @param id User's openID.
     */
    private void setOpenId(String id) {
        openId = id;
    }

    /**
     * Gets the hash of user's password.
     *
     * @return Hash of the user's password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets hash of the user's password.
     *
     * @param password Hash of the user's password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the user's email. (Calls the wrapped object.)
     *
     * @return User's email address.
     */
    public String getEmail() {
        return getUser().getEmail();
    }

    /**
     * Set user's email. (Calls the wrapped object.)
     *
     * @return User's email address.
     */
    public void setEmail(String email) {
        getUser().setEmail(email);
    }

    /**
     * Gets the flag if the user is permanently logged in. (Calls the wrapped
     * object.)
     *
     * @return Flag if the user is permanently logged in.
     */
    public boolean isPermanentlyLoggedId() {
        return getUser().isPermanentlyLoggedIn();
    }

    /**
     * Sets the flag if the user is permanently logged in. (Calls the wrapped
     * object.)
     *
     * @param permanentlyLoggedId Flag if the user is permanently logged in.
     */
    public void setPermanentlyLoggedId(boolean permanentlyLoggedId) {
        getUser().setPermanentlyLoggedIn(permanentlyLoggedId);
    }

    /**
     * Gets the maximum number of suggestions user want to receive during a
     * document editing. (Calls the wrapped object.)
     *
     * @return Maximum number of suggestions displayed in the app.
     */
    public int getMaximumNumberOfSuggestions() {
        return getUser().getMaximumNumberOfSuggestions();
    }

    /**
     * Sets the maximum number of suggestions user want to receive during a
     * document editing. (Calls the wrapped object.)
     *
     * @param maximumNumberOfSuggestions Maximum number of suggestions displayed
     * in the app.
     */
    public void setMaximumNumberOfSuggestions(int maximumNumberOfSuggestions) {
        getUser().setMaximumNumberOfSuggestions(maximumNumberOfSuggestions);
    }

    /**
     * Gets the flag if the wants to include results of machine translation
     * together with the suggestions.
     *
     * @return Flag if the user wants machine translation.
     */
    public boolean getUseMoses() {
        return getUser().getUseMoses();
    }

    /**
     * Sets the flag if the wants to include results of machine translation
     * together with the suggestions.
     *
     * @param useMoses Flag if the user wants machine translation.
     */
    public void setUseMoses(boolean useMoses) {
        getUser().setUseMoses(useMoses);
    }

    /**
     * Adds a document to the in-memory collection of documents owned by the
     * user. The document itself including it's connection to the user are saved
     * to the already in the document constructor.
     *
     * @param document
     */
    public void addDocument(USDocument document) {
        getOwnedDocuments().put(document.getDatabaseId(), document);
    }

    /**
     * Saves the user (not the document he owns) to the database.
     *
     * @param dbSession An active database session.
     */
    public void saveToDatabase(Session dbSession) {
        saveJustObject(dbSession);
    }

    /**
     * Deletes the user from the database and marks all the document he own as
     * ready to be deleted.
     *
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
     * Gets the wrapped user object clone without documents. It is used to send
     * the client information about the user after user logs in.
     *
     * @return User object without documents.
     */
    public User sharedUserWithoutDocuments() {
        return getUser().getCloneWithoutDocuments();
    }

    /**
     * @return the user
     */
    public User getUser() {
        return user;
    }
}

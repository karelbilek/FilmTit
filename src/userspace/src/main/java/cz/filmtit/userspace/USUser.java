package cz.filmtit.userspace;

import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;

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

    private int databaseId;
    private String userName;
    private String login;
    private String openId;
    private String fcbId;
    private Boolean active;
    private String lastSession;
    private String email;

    private List<USDocument> ownedDocuments;
    private USDocument activeDocument;
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
            org.hibernate.Session session = HibernateUtil.getSessionFactory().getCurrentSession();
            session.beginTransaction();

            // query the documents owned by the user
            List result = session.createQuery("select d from USDocument d where d.ownerDatabaseId = :uid")
                    .setParameter("uid", getDatabaseId()).list();

            // store it to the variable
            for (Object o : result) { ownedDocuments.add((USDocument)o); }
            
            session.getTransaction().commit();
        }
        return ownedDocuments;
    }

    protected void setSharedDatabaseId(long id) { }
    protected long getSharedDatabaseId() { return databaseId; }

    public void saveToDatabase(Session dbSession) {
        saveJustObject(dbSession);
        activeDocument.saveToDatabase(dbSession);
    }

    public void deleteFromDatabase(Session dbSession) {
        deleteJustObject(dbSession);
        for (USDocument document : ownedDocuments) {
            document.deleteFromDatabase(dbSession);
        }
    }

    public static int CreateUser(String name, String email,String openId , String fbId )
    {
        org.hibernate.Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        return -1;
    }


}

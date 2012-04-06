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
 * Represents a user of the application
 * @author Jindřich Libovický
 */
public class UserUS extends DatabaseObject {
    private String name;
    private String passwordHash;
    private String fcbId;
    private List<DocumentUS> ownedDocuments;
    private DocumentUS activeDocument;
    /**
     * Sign if the active document was created in the current session and therefore is not yet in the database.
     */
    private boolean activeIsNew;  // is it necessary ???  I guess it's not

    /**
     * Gets the list of documents owned by this user.
     * @return List of DocumentUS objects
     */
    public List<DocumentUS> getOwnedDocuments() {
        //  if the list of owned documents is empty...
        if (ownedDocuments == null) {
            ownedDocuments = new ArrayList<DocumentUS>();
            org.hibernate.Session session = HibernateUtil.getSessionFactory().getCurrentSession();
            session.beginTransaction();

            // query the documents owned by the user
            List result = session.createQuery("select d from Documents where d.UserId = :uid")
                    .setParameter("uid", getDatabaseId()).list();

            // store it to the variable
            for (Object o : result) { ownedDocuments.add((DocumentUS)o); }
            
            session.getTransaction().commit();
        }
        return ownedDocuments;
    }

    public void saveToDatabase(Session dbSession) {
        saveJustObject(dbSession);
        activeDocument.saveToDatabase(dbSession);
    }

    public void deleteFromDatabase(Session dbSession) {
        deleteJustObject(dbSession);
        for (DocumentUS document : ownedDocuments) {
            document.deleteFromDatabase(dbSession);
        }
    }
    
}

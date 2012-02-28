package cz.filmtit.userspace;

import java.util.*;

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
public class User {
    private int databaseId;
    private String name;
    private String passwordHash;
    private String fcbId;
    private List<Document> ownedDocuments; // if null, getter will generate it, setter is not necessary
    private Document activeDocument;
    /**
     * Sign if the active document was created in the current session and therefore is not yet in the database.
     */
    private boolean activeIsNew;
    
}

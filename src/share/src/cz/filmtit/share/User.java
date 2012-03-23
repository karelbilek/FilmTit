package cz.filmtit.share;

import java.util.List;

public class User {
    public String name;
    public String passwordHash;
    public String fcbId;
    public List<Document> ownedDocuments;
    public Document activeDocument;
}

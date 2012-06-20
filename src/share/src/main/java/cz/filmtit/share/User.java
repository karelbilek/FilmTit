package cz.filmtit.share;

import java.util.List;

public class User {
    private String name;
    public String id;
    public List<Document> ownedDocuments;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Document> getOwnedDocuments() {
        return ownedDocuments;
    }
}

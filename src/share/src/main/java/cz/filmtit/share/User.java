package cz.filmtit.share;

import java.util.List;

public class User {
    private String name;
    private String email;
    private boolean permanentlyLoggedIn;
    private int maximumNumberOfSuggestions;
    private boolean useMoses;

    public List<Document> ownedDocuments;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Document> getOwnedDocuments() {
        return ownedDocuments;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String emaString) {
        this.email = emaString;
    }

    public boolean isPermanentlyLoggedIn() {
        return permanentlyLoggedIn;
    }

    public void setPermanentlyLoggedIn(boolean permanentlyLoggedIn) {
        this.permanentlyLoggedIn = permanentlyLoggedIn;
    }

    public int getMaximumNumberOfSuggestions() {
        return maximumNumberOfSuggestions;
    }

    public void setMaximumNumberOfSuggestions(int maximumNumberOfSuggestions) {
        this.maximumNumberOfSuggestions = maximumNumberOfSuggestions;
    }

    public boolean isUseMoses() {
        return useMoses;
    }

    public void setUseMoses(boolean useMoses) {
        this.useMoses = useMoses;
    }
}

package cz.filmtit.share;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable, IsSerializable {
    private volatile long id = Long.MIN_VALUE;
    private volatile String name;
    private volatile String email;
    private volatile boolean permanentlyLoggedIn;
    private volatile int maximumNumberOfSuggestions;
    private volatile boolean useMoses;

    public List<Document> ownedDocuments;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        if (this.id == id) { return; }
        if (this.id != Long.MIN_VALUE) {
            throw new UnsupportedOperationException("Once the document ID is set, it cannot be changed.");
        }
        this.id = id;
    }

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

    public boolean getUseMoses() {
        return useMoses;
    }

    public void setUseMoses(boolean useMoses) {
        this.useMoses = useMoses;
    }

    public User getCloneWithoutDocuments() {
        User clone = new User();

        clone.name = name;
        clone.email = email;
        clone.permanentlyLoggedIn = permanentlyLoggedIn;
        clone.maximumNumberOfSuggestions = maximumNumberOfSuggestions;
        clone.useMoses = useMoses;

        return clone;
    }
}

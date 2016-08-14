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

package cz.filmtit.share;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a user of the application. Contains it settings and a list of
 * documents the user owns. The authentication details as OpenId identifier
 * or password are User Space specific and therefore are not part of the
 * shared class.
 *
 * @author Jindřich Libovický
 */
public class User implements Serializable, IsSerializable {
    /**
     * Database ID of the user
     */
    private volatile long id = Long.MIN_VALUE;
    /**
     * The user name
     */
    private volatile String name;
    /**
     * Email of the user
     */
    private volatile String email;
    /**
     * Flag if the user is permanently logged in
     */
    private volatile boolean permanentlyLoggedIn;
    /**
     * Maximum number of translation suggestion the user want to receive.
     */
    private volatile int maximumNumberOfSuggestions;
    /**
     * Flag if the Moses translation should be included into translation suggestions.
     */
    private volatile boolean useMoses;

    /**
     * List of documents the user owsn.
     */
    public List<Document> ownedDocuments;

    /**
     * Gets the database ID of the user.
     * @return Database ID of the user.
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the database ID of the user if it has not been set before. Otherwise throws an excpetion.
     * @param id  Database ID assigned to the user object.
     */
    public void setId(long id) {
        if (this.id == id) { return; }
        if (this.id != Long.MIN_VALUE) {
            throw new UnsupportedOperationException("Once the document ID is set, it cannot be changed.");
        }
        this.id = id;
    }

    /**
     * Gets the user name.
     * @return User name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the user name
     * @param name User name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the list of documents owned by the user.
     * @return List of documents owned by the user.
     */
    public List<Document> getOwnedDocuments() {
        return ownedDocuments;
    }

    /**
     * Gets user's email
     * @return User's email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets user's email
     * @param emailString User's email
     */
    public void setEmail(String emailString) {
        this.email = emailString;
    }

    /**
     * Gets a flag if the user want to be logged in permanently.
     * @return Flag if the user want to be logged in permanently.
     */
    public boolean isPermanentlyLoggedIn() {
        return permanentlyLoggedIn;
    }

    /**
     * Sets the flag if the user want to be logged in permanently.
     * @param permanentlyLoggedIn Flag if the user want to be logged in permanently.
     */
    public void setPermanentlyLoggedIn(boolean permanentlyLoggedIn) {
        this.permanentlyLoggedIn = permanentlyLoggedIn;
    }

    /**
     * Gets maximum number of translation suggestions provided to one chunk.
     * @return Maximum number of translation suggestions provided to one chunk
     */
    public int getMaximumNumberOfSuggestions() {
        return maximumNumberOfSuggestions;
    }

    /**
     * Sets maximum number of translation suggestions provided to one chunk.
     * @return Maximum number of translation suggestions provided to one chunk
     */
    public void setMaximumNumberOfSuggestions(int maximumNumberOfSuggestions) {
        this.maximumNumberOfSuggestions = maximumNumberOfSuggestions;
    }

    /**
     * Gets the flag if the user wants to use Moses translation.
     * @return Flag if the Moses translation should be used.
     */
    public boolean getUseMoses() {
        return useMoses;
    }

    /**
     * Sets the flag if the user wants to use Moses translation.
     * @param useMoses Flag if the Moses translation should be used.
     */
    public void setUseMoses(boolean useMoses) {
        this.useMoses = useMoses;
    }

    /**
     * Gets a surface clone of the object that does not contain
     * the map of the owned document.
     * @return Clone without owned documents.
     */
    public User getCloneWithoutDocuments() {
        User clone = new User();

        clone.id = id;
        clone.name = name;
        clone.email = email;
        clone.permanentlyLoggedIn = permanentlyLoggedIn;
        clone.maximumNumberOfSuggestions = maximumNumberOfSuggestions;
        clone.useMoses = useMoses;

        return clone;
    }
    
    public User() {
        //nothing
    }
}

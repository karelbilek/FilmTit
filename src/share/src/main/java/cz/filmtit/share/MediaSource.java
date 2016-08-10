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

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * The source of a subtitle chunk. This may be a movie, TV series etc.
 *
 * @author Joachim Daiber
 */
public class MediaSource implements com.google.gwt.user.client.rpc.IsSerializable, Serializable {

    private static final long serialVersionUID = 5899275001958847886L;
    /**
     * Long identificator
     */
    private Long id = Long.MIN_VALUE;
    /**
     * title of film
     */
    private String title;
    /**
     * release year
     */
    private String year;
    /**
     * Genres, which characterize each film
     */
    private Set<String> genres;
    /**
     * Url of icon
     */
    private String thumbnailURL;

    public MediaSource() {
        // nothing
    }

    /**
     * Sets identificator
     *
     * @param id identificator
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets title of MediaSource
     *
     * @return String
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets title of MediaSource
     *
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets year of MediaSource
     *
     * @return String year
     */
    public String getYear() {
        return year;
    }

    /**
     * Sets year of MediaSource
     *
     * @param year
     */
    public void setYear(String year) {
        this.year = year;
    }

    /**
     * Gets genres
     *
     * @return all genres
     */
    public Set<String> getGenres() {
        if (genres == null) {
            return new HashSet<String>();
        } else {
            return genres;
        }
    }

    /**
     * Gets MediaSources identificator
     *
     * @return identificator
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets genres
     *
     * @param genres
     */
    public void setGenres(Set<String> genres) {
        this.genres = genres;
    }

    /**
     * Creates string representation.
     *
     * @return If is year non-empty reates "title (year)" otherwise "title"
     */
    @Override
    public String toString() {
        String str = title;
        if (year != null && !year.equals("")) {
            str += " (" + year + ")";
        }

        return str;
    }

    /**
     * Parses and sets a list genres provided as a comma separated list of
     * string. Used by Hibernate.
     *
     * @param genres
     */
    public void setGenresString(String genres) {
        this.genres = new HashSet<String>();
        String[] genresStrings = genres.split(",");
        for (String genre : genresStrings) {
            this.getGenres().add(genre);
        }
    }

    /**
     * Gets the genres as a comma separated list of strings. Used by Hibernate.
     *
     * @return Comma separated list of genres.
     */
    private String getGenresString() {
        StringBuilder genresBuilder = new StringBuilder("");
        if (genres != null) {
            for (String genre : genres) {
                genresBuilder.append(genre + ",");
            }
            genresBuilder.deleteCharAt(genresBuilder.length() - 1); // removes last comma
        }
        return genresBuilder.toString();
    }

    /**
     * Creates instance
     *
     * @param title Title of MediaSource
     * @param year Year of MediaSource
     * @param genres Genres of MediaSource separated by space
     */
    public MediaSource(String title, String year, String genres) {
        this.title = title;
        this.year = year;
        this.genres = new HashSet<String>();
        this.genres.addAll(Arrays.asList(genres.split(",[ ]*")));
    }

    /**
     * Instance withou genres
     *
     * @param title Title of MediaSource
     * @param year Year of MediaSource
     */
    public MediaSource(String title, String year) {
        this(title, year, "");
    }

    /**
     * Compares two MediaSource objects if they are same
     *
     * @param o obect which should be MediaSource
     * @return True if are the same object or have same properties otherwise
     * false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MediaSource)) {
            return false;
        }

        MediaSource that = (MediaSource) o;

        return !(genres != null ? !genres.equals(that.genres) : that.genres != null)
                && !(id != null ? !id.equals(that.id) : that.id != null)
                && title.equals(that.title)
                && !(year != null ? !year.equals(that.year) : that.year != null);

    }

    /**
     * Generates hasCodes for instance MediaSource
     *
     * @return Int Combinations of hashCodes of properties
     */
    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + (year != null ? year.hashCode() : 0);
        result = 31 * result + (genres != null ? genres.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    /**
     * Sets url of thumbnail
     *
     * @param thumbnailURL url
     */
    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    /**
     * Gets url of thumbnail
     *
     * @return url
     */
    public String getThumbnailURL() {
        return thumbnailURL;
    }
}

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

    private Long id = Long.MIN_VALUE;
    private String title;
    private String year;
    private Set<String> genres;
    private String thumbnailURL;

    public MediaSource() {
		// nothing
	}

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public Set<String> getGenres() {
        return genres;
    }

    public Long getId() {
        return id;
    }

    public void setGenres(Set<String> genres) {
        this.genres = genres;
    }

    @Override
    public String toString() {
        String str = title;
        if (year != null && !year.equals("")) {
            str += " (" + year + ")";
        }

        return str;
    }

    /**
     * Parses and sets a list genres provided as a comma separated list of string.
     * Used by Hibernate.
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
     * Gets the genres as a comma separated list of strings.
     * Used by Hibernate.
     * @return Comma separated list of genres.
     */
    private String getGenresString() {
        StringBuilder genresBuilder = new StringBuilder();
        for (String genre : genres) {
            genresBuilder.append(genre + ",");
        }
        genresBuilder.deleteCharAt(genresBuilder.length() - 1); // removes last comma
        return genresBuilder.toString();
    }

    public MediaSource(String title, String year, String genres) {
        this.title = title;
        this.year = year;
        this.genres = new HashSet<String>();
        this.genres.addAll(Arrays.asList(genres.split(",[ ]*")));
    }

    public MediaSource(String title, String year) {
        this(title, year, "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MediaSource)) return false;

        MediaSource that = (MediaSource) o;

        return !(genres != null ? !genres.equals(that.genres) : that.genres != null)
                && !(id != null ? !id.equals(that.id) : that.id != null)
                && title.equals(that.title)
                && !(year != null ? !year.equals(that.year) : that.year != null);

    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + (year != null ? year.hashCode() : 0);
        result = 31 * result + (genres != null ? genres.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }
}



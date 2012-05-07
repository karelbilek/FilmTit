package cz.filmtit.share;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;

/**
 * The source of a subtitle chunk. This may be a movie, TV series etc.
 *
 * @author Joachim Daiber
 */
public class MediaSource implements Serializable {

    private static final long serialVersionUID = 5899275001958847885L;

    private Long id;
    private String title;
    private String year;
    private HashSet<String> genres;

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getYear() {
        return year;
    }

    public HashSet<String> getGenres() {
        return genres;
    }

    public Long getId() {
        return id;
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
}



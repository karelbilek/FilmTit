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

    private String title;
    private String year;

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

    private HashSet<String> genres;
    private Long id;

    public MediaSource(String title, String year, String genres) {
        this.title = title;
        this.year = year;
        this.genres = new HashSet<String>();
        this.genres.addAll(Arrays.asList(genres.split(",[ ]*")));
    }

    public MediaSource(String title, String year) {
        this(title, year, "");
    }

}



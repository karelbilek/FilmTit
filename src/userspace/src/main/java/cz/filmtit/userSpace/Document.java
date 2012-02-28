package cz.filmtit.userSpace;

import cz.filmtit.core.model.Language;
import cz.filmtit.core.model.data.*;

import java.util.*;

/**
 * Represents a subtitle file the user work with.
 * @author Jindřich Libovický
 */
public class Document {
    private int databaseId;
    private String movieTitle;
    private int year;
    private List<Chunk> chunks;
    private long workStartTime;
    private long spentOnThisTime;
    private Language language; // different getter and setter with string only
    private MediaSource mediaSource; // regenerate it
    private boolean finished;

    public int getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(int databaseId) {
        this.databaseId = databaseId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    /**
     * Sets the title of the movie. If the year of publishing the movie has been defined,
     * it also updates the IMDB information.
     * @param movieTitle New movie title.
     */
    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
        if (year != 0) {
            mediaSource = cz.filmtit.core.model.data.MediaSource.fromIMDB(movieTitle, Integer.toString(year));
        }
    }

    public int getYear() {
        return year;
    }

    /**
     * Sets the year when the movie was published. If the title has been defined, it also updates the IMDB information.
     * @param year New value of year.
     * @throws IllegalArgumentException
     */
    public void setYear(int year) {
        // the movie should be from some reasonable
        if (year < 1850 || year > Calendar.getInstance().get(Calendar.YEAR + 5) ) {
            throw new IllegalArgumentException("Value of year should from 1850 to the current year + 5.");
        }
        this.year = year;
        if (movieTitle != null) {
            mediaSource = cz.filmtit.core.model.data.MediaSource.fromIMDB(movieTitle, Integer.toString(year));
        }
    }

    /**
     * Gets the time spent on translating this subtitles valid right now.
     * @return The time spent on this subtitles in milliseconds.
     */
    public long getSpentOnThisTime() {
        return spentOnThisTime + (new Date()).getTime() - workStartTime;
    }

    /**
     * Sets the new time which was spent on translating this subtitles.
     * If this is set, the work start time reset to current time.
     * @param spentOnThisTime New value of time spent on this document.
     */
    public void setSpentOnThisTime(long spentOnThisTime) {
        this.spentOnThisTime = spentOnThisTime;
        workStartTime = new Date().getTime();
    }

    public Language getLanguage() {
        return language;
    }

    public String getLanguageString() {
        return language.toString();
    }
    
    public void setLanguageString(String languageCode) {
        // TODO: solve this so that the languge will get both the name and code of the languge
        this.language = cz.filmtit.core.model.Language.apply("", languageCode);
    }

    public MediaSource getMediaSource() {
        return mediaSource;
    }

}

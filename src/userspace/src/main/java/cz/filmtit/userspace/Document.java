package cz.filmtit.userspace;

import cz.filmtit.core.model.Language;
import cz.filmtit.core.model.data.*;

import java.util.*;

/* Functionality ... what may happen
    - changing the title / year or even language of the document => regenerate no finished translations
    - changing the original language chunk => regenerate this chunk translations
    - changing the timing of the chunk
    - adding new chunk, deleting a chunk
    - claim it's finished => delete matches, ?? where the result file will be generated
                               update the TM
 */

/**
 * Represents a subtitle file the user work with.
 * @author Jindřich Libovický
 */
public class Document extends DatabaseObject {
    private static final int MINIMUM_MOVIE_YEAR = 1850;
    private static final int ALLOWED_FUTURE_FOR_YEARS = 5;
    private static final long RELOAD_TRANSLATIONS_TIME = 86400000;
    
    public Document(String movieTitle, int year, Language language) {
        this.movieTitle = movieTitle;
        this.year = year;
        this.language = language;
        workStartTime = new Date().getTime();
        spentOnThisTime = 0;
        chunks = new ArrayList<Chunk>();
    }

    /**
     * Default constructor (for Hibernate).
     */
    public Document() {
    }

    private String movieTitle;
    private int year;
    private List<Chunk> chunks;
    private long workStartTime;
    private long spentOnThisTime;
    private Language language;
    private long translationGenerationTime;
    /**
     * More complex movie annotation required by the TM.
     */
    private MediaSource mediaSource;
    private boolean finished;

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
        // the movie should be from a reasonable time period
        if (year < MINIMUM_MOVIE_YEAR || year > Calendar.getInstance().get(Calendar.YEAR + ALLOWED_FUTURE_FOR_YEARS) ) {
            throw new IllegalArgumentException("Value of year should from 1850 to the current year + 5.");
        }
        this.year = year;
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
        // TODO: solve this so that the language will get both the name and code of the language
        this.language = cz.filmtit.core.model.Language.apply("", languageCode);
    }

    public MediaSource getMediaSource() {
        if (mediaSource == null) {
            mediaSource = cz.filmtit.core.model.data.MediaSource.fromIMDB(movieTitle, Integer.toString(year));
        }
        return mediaSource;
    }

    public long getTranslationGenerationTime() {
        return translationGenerationTime;
    }

    public void setTranslationGenerationTime(long translationGenerationTime) {
        this.translationGenerationTime = translationGenerationTime;
    }

    /**
     * Loads the chunks from User Space database.
     */
    public void loadChunksFromDb() {
        org.hibernate.Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
    
        // query the database for the chunks
        List foundChunks = session.createQuery("select c from Chunks where c.documentId = :did")
                .setParameter("did", getDatabaseId()).list();

        chunks = new ArrayList<Chunk>();
        for (Object o : foundChunks) {
            chunks.add((Chunk)o);
        }
    
        session.getTransaction().commit();

        // if the chunks have old translations, regenerate them
        if (new Date().getTime() > this.translationGenerationTime + RELOAD_TRANSLATIONS_TIME)  {
            for (Chunk chunk : chunks) {
                chunk.renewMTSuggestions();
            }
        }
        else { // otherwise just load them from the database
            for (Chunk chunk : chunks) {
                chunk.loadMatchesFromDatabase();
            }
        }

    }

    public void saveToDatabase() {
        saveJustObject();

        for (Chunk chunk : chunks) {
            chunk.saveToDatabase();
        }
    }

    public void deleteFromDatabase() {
        deleteJustObject();

    }
}

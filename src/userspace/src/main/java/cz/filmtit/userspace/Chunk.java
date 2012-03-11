package cz.filmtit.userspace;

import java.util.*;
import cz.filmtit.core.*;
import cz.filmtit.core.model.*;
import cz.filmtit.core.model.data.*;

/**
 * Represents a subtitle chunk.
 * @author Jindřich Libovický
 */
public class Chunk {
    /**
     * Creates a new chunk of given properties.
     * @param documentDatabaseId
     * @param startTime
     * @param endTime
     * @param text
     */
    public Chunk(Long documentDatabaseId, String startTime, String endTime, String text) {
        this.documentDatabaseId = documentDatabaseId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.text = text;
        done = false;
    }

    /**
     * Default constructor for Hibernate.
     */
    public Chunk() {
    }

    private Long databaseId;
    private Long documentDatabaseId;
    private String startTime;
    private String endTime;
    private String text;
    private String userTranslation;
    private int partNumber;
    private boolean done;
    private Document parent;
    private List<Match> matches;
    /**
     * Id of the selected tranlsation in the translation memory.
     */
    private int selectedTranslation;

    public Long getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(Long databaseId) {
        this.databaseId = databaseId;
    }

    public Long getDocumentDatabaseId() {
        return documentDatabaseId;
    }

    public void setDocumentDatabaseId(Long documentDatabaseId) {
        this.documentDatabaseId = documentDatabaseId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        // TODO: check the timing format
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        // TODO: check the timing format
        this.endTime = endTime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUserTranslation() {
        return userTranslation;
    }

    public void setUserTranslation(String userTranslation) {
        this.userTranslation = userTranslation;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public int getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
    }

    // TODO: solve the situation that the found matches are out of date and must be regenerated
    //  ... we must be careful about the ID of already used translation
    //  ... the old ones has to be deleted probably

    public void LoadMatchesFromDatabase() {
        org.hibernate.Session session = UserSpace.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        // query the matches from the database
        List foundMatches = session.createQuery("select m from Matches where m.chunkId = :cid")
                .setParameter("cid", databaseId).list();
        
        // store them in this object
        matches = new ArrayList<Match>();
        for (Object m : foundMatches) {
            matches.add((Match)m);
        }

        session.getTransaction().commit();

        // once the matches ar loaded, load translations for them
        for (Match m : matches) {
            m.loadTranslationsFromDatabase();
        }
    }

    public void LoadMTSuggestions() {        
        matches = new ArrayList<Match>(); // throws away previous matches if there were any
        cz.filmtit.core.model.data.Chunk tmChunk = new cz.filmtit.core.model.data.Chunk(text);
        TranslationMemory TM = Factory.createTM();
        
        scala.collection.immutable.List<ScoredTranslationPair> TMResults =
                TM.nBest(tmChunk, parent.getLanguage(), parent.getMediaSource(), 10);
        // the retrieved Scala collection must be transformed to a Java collection
        Collection<ScoredTranslationPair> javaList =
                scala.collection.JavaConverters.asJavaCollectionConverter(TMResults).asJavaCollection();

        // table of matched string and corresponding translations
        Map<String, List<Translation>> matchesTable = new HashMap<String, List<Translation>>();
        for(ScoredTranslationPair pair : javaList) {
            // if a match does not exist, create its entry
            if (matchesTable.get(pair.getStringL1()) == null) {
                matchesTable.put(pair.getStringL1(), new ArrayList<Translation>());
            }
            matchesTable.get(pair.getStringL1()).add(new Translation(pair.getStringL1(), pair.getScore()));
        }

        // creates the match objects
        for (String matchString : matchesTable.keySet()) {
            matches.add(new Match(matchString, matchesTable.get(matchString)));
        }

        // here the JSON response will be generated
    }
}

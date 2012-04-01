package cz.filmtit.userspace;

import java.util.*;
import cz.filmtit.core.*;
import cz.filmtit.core.model.*;
import cz.filmtit.core.model.data.*;

/**
 * Represents a subtitle chunk.
 * @author Jindřich Libovický
 */
public class Chunk extends DatabaseObject {
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

    public void loadMatchesFromDatabase() {
        org.hibernate.Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        // query the matches from the database
        List foundMatches = session.createQuery("select m from Matches where m.chunkId = :cid")
                .setParameter("cid", databaseId).list();
        
        // store them in this object
        List<Match> newMatches = new ArrayList<Match>();
        for (Object m : foundMatches) {
            newMatches.add((Match)m);
        }
        matches = Collections.synchronizedList(newMatches);

        session.getTransaction().commit();

        // once the matches are loaded, load translations for them
        for (Match m : matches) {
            m.loadTranslationsFromDatabase();
        }
    }

    public void loadMTSuggestions() {
        // TODO: Parallel this method


        cz.filmtit.core.model.data.Chunk tmChunk = new cz.filmtit.core.model.data.Chunk(text);
        TranslationMemory TM = Factory.createTM(true);
        
        scala.collection.immutable.List<ScoredTranslationPair> TMResults =
                TM.nBest(tmChunk, parent.getLanguage(), parent.getMediaSource(), 10, false);

        // the retrieved Scala collection must be transformed to a Java collection
        // otherwise it cannot be iterated by the for loop
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
        List<Match> newMatches = new ArrayList<Match>();
        for (String matchString : matchesTable.keySet()) {
            newMatches.add(new Match(matchString, matchesTable.get(matchString)));
        }
        matches = Collections.synchronizedList(newMatches); // throws away previous matches if there were any

        // here the JSON response will be generated
    }

    public void renewMTSuggestions() {
        deleteMatches();
        loadMTSuggestions();
    }

    /**
     * Deletes the matches for this chunk. (In case of regenerating translations or deleting the chunk.)
     */
    void deleteMatches() {
        for (Match match : matches) {
            match.deleteFromDatabase();
        }
        matches = new ArrayList<Match>();
    }

    public void saveToDatabase() {
        saveJustObject();

        // save or update also all dependent matches
        for (Match match : matches) {
            match.saveToDatabase();
        }
    }

    public void deleteFromDatabase() {
        deleteJustObject();
        deleteMatches();
    }
}

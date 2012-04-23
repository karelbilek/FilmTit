package cz.filmtit.userspace;

import cz.filmtit.core.Factory;
import cz.filmtit.core.model.TranslationMemory;
import cz.filmtit.core.model.data.ScoredTranslationPair;
import cz.filmtit.share.Chunk;
import org.hibernate.Session;

import java.util.*;

/**
 * Represents a subtitle chunk.
 * @author Jindřich Libovický
 */
public class USChunk extends DatabaseObject {
    private long documentDatabaseId;
    private Chunk chunk;
    private USDocument parent;
    private List<USMatch> matches;

    /**
     * Creates a new chunk of given properties. ... porably never needed ???
     * @param documentDatabaseId
     * @param startTime
     * @param endTime
     * @param text
     */
    public USChunk(long documentDatabaseId, String startTime, String endTime, String text) {
        this.documentDatabaseId = documentDatabaseId;
        chunk = new Chunk();

        chunk.startTime = startTime;
        chunk.endTime = endTime;
        chunk.text = text;
        chunk.done = false;
    }

    /**
     * Default constructor for Hibernate.
     */
    public USChunk() {
        chunk = new Chunk();
    }

    /**
     * Creates an instance of User Space Chunk from the shared Match.
     *
     * It just assigns it to the inner variable, User Space objects
     * wrapping the contained translations are created when necessary.
     * @param c
     */
    public USChunk(Chunk c) {
        chunk = c;
    }
    
    public Chunk getSharedChunk() {
        return chunk;
    }
    
    public int hashCode() {
        return chunk.hashCode();
    }    
    
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass()) { return false; }
        return  chunk.equals(((USChunk)obj).chunk);
    }

    public long getDocumentDatabaseId() {
        return documentDatabaseId;
    }

    public void setDocumentDatabaseId(long documentDatabaseId) {
        this.documentDatabaseId = documentDatabaseId;
    }

    public String getStartTime() {
        return chunk.startTime;
    }

    public void setStartTime(String startTime) {
        // TODO: check the timing format
        chunk.startTime = startTime;
    }

    public String getEndTime() {
        return chunk.endTime;
    }

    public void setEndTime(String endTime) {
        // TODO: check the timing format
        chunk.endTime = endTime;
    }

    public String getText() {
        return chunk.text;
    }

    public void setText(String text) {
        chunk.text = text;
    }

    public String getUserTranslation() {
        return chunk.userTranslation;
    }

    public void setUserTranslation(String userTranslation) {
        chunk.userTranslation = userTranslation;
    }

    public boolean isDone() {
        return chunk.done;
    }

    public void setDone(boolean done) {
        chunk.done = done;
    }

    public int getPartNumber() {
        return chunk.partNumber;
    }

    public void setPartNumber(int partNumber) {
        chunk.partNumber = partNumber;
    }

    public List<USMatch> getMatches() {
        // create it if it does not exist
        if (matches == null && chunk.matches != null) {
            List<USMatch> newMatches = new ArrayList<USMatch>();
            for (Match m : chunk.matches) {
                USMatch newMatch =  new USMatch(m);
                newMatch.setChunkDatabaseId(getDatabaseId());
                newMatches.add(newMatch);
            }

            matches = Collections.synchronizedList(newMatches);
        }
        return matches;
    }

    public void loadMatchesFromDatabase(Session dbSession) {
        // query the matches from the database
        List foundMatches = dbSession.createQuery("select m from USMatch m where m.chunkDatabaseId = :cid")
                .setParameter("cid", getDatabaseId()).list();
        
        // store them in this object and also in the shared chunk object
        List<USMatch> newMatches = new ArrayList<USMatch>();
        chunk.matches = new ArrayList<Match>();
        for (Object m : foundMatches) {
            newMatches.add((USMatch)m);
            chunk.matches.add(((USMatch)m).getSharedMatch());
        }
        matches = Collections.synchronizedList(newMatches);

        // once the matches are loaded, load translations for them
        for (USMatch m : matches) {
            m.loadTranslationsFromDatabase(dbSession);
        }
    }

    public void loadMTSuggestions() {
        // TODO: Make this method parallel

        cz.filmtit.core.model.data.Chunk tmChunk = new cz.filmtit.core.model.data.Chunk(chunk.text);
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
        List<USMatch> newMatches = new ArrayList<USMatch>();
        for (String matchString : matchesTable.keySet()) {
            Match newMatch = new Match();
            newMatch.text = matchString;
            newMatch.translations = matchesTable.get(matchString);
            chunk.matches.add(newMatch);
        }
    }

    public void renewMTSuggestions() {
        // deleting the old matches is standalone database transaction
        Session dbSession = HibernateUtil.getSessionFactory().getCurrentSession();
        dbSession.beginTransaction();

        deleteMatches(dbSession);

        dbSession.getTransaction().commit();

        loadMTSuggestions();
    }

    /**
     * Deletes the matches for this chunk.
     * (In case of regenerating translations or deleting the chunk.)
     */
    void deleteMatches(Session dbSession) {
        for (USMatch match : getMatches()) {
            match.deleteFromDatabase(dbSession);
        }
        matches = null;
        chunk.matches = new ArrayList<Match>();
    }

    public void saveToDatabase(Session dbSession) {
        saveJustObject(dbSession);

        // save or update also all dependent matches
        for (USMatch match : getMatches()) {
            match.saveToDatabase(dbSession);
        }
    }

    public void deleteFromDatabase(Session dbSession) {
        deleteJustObject(dbSession);
        deleteMatches(dbSession);
    }
}

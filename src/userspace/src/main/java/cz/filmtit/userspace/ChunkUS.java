package cz.filmtit.userspace;

import cz.filmtit.core.Factory;
import cz.filmtit.core.model.TranslationMemory;
import cz.filmtit.core.model.data.ScoredTranslationPair;
import cz.filmtit.share.Chunk;
import cz.filmtit.share.Match;
import org.hibernate.Session;

import java.util.*;

/**
 * Represents a subtitle chunk.
 * @author Jindřich Libovický
 */
public class ChunkUS extends DatabaseObject {
    private Long documentDatabaseId;
    private Chunk chunk;
    private DocumentUS parent;
    private List<MatchUS> matches;

    /**
     * Creates a new chunk of given properties. ... porably never needed ???
     * @param documentDatabaseId
     * @param startTime
     * @param endTime
     * @param text
     */
    public ChunkUS(Long documentDatabaseId, String startTime, String endTime, String text) {
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
    public ChunkUS() {
        chunk = new Chunk();
    }

    /**
     * Creates an instance of User Space Chunk from the shared Match.
     *
     * It just assigns it to the inner variable, User Space objects
     * wrapping the contained translations are created when necessary.
     * @param c
     */
    public ChunkUS(Chunk c) {
        chunk = c;
    }
    
    public int hashCode() {
        return chunk.hashCode();
    }    
    
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass()) { return false; }
        return  chunk.equals(((ChunkUS)obj).chunk);
    }

    public Long getDocumentDatabaseId() {
        return documentDatabaseId;
    }

    public void setDocumentDatabaseId(Long documentDatabaseId) {
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

    public void loadMatchesFromDatabase(Session dbSession) {
        // query the matches from the database
        List foundMatches = dbSession.createQuery("select m from Matches where m.chunkId = :cid")
                .setParameter("cid", getDatabaseId()).list();
        
        // store them in this object and also in the shared chunk object
        List<MatchUS> newMatches = new ArrayList<MatchUS>();
        chunk.matches = new ArrayList<Match>();
        for (Object m : foundMatches) {
            newMatches.add((MatchUS)m);
            chunk.matches.add(((MatchUS)m).getSharedMatch());
        }
        matches = Collections.synchronizedList(newMatches);

        // once the matches are loaded, load translations for them
        for (MatchUS m : matches) {
            m.loadTranslationsFromDatabase(dbSession);
        }
    }

    public void loadMTSuggestions() {
        // TODO: Parallelize this method

        cz.filmtit.core.model.data.Chunk tmChunk = new cz.filmtit.core.model.data.Chunk(chunk.text);
        TranslationMemory TM = Factory.createTM(true);
        
        scala.collection.immutable.List<ScoredTranslationPair> TMResults =
                TM.nBest(tmChunk, parent.getLanguage(), parent.getMediaSource(), 10, false);

        // the retrieved Scala collection must be transformed to a Java collection
        // otherwise it cannot be iterated by the for loop
        Collection<ScoredTranslationPair> javaList =
                scala.collection.JavaConverters.asJavaCollectionConverter(TMResults).asJavaCollection();

        // table of matched string and corresponding translations
        Map<String, List<TranslationUS>> matchesTable = new HashMap<String, List<TranslationUS>>();
        for(ScoredTranslationPair pair : javaList) {
            // if a match does not exist, create its entry
            if (matchesTable.get(pair.getStringL1()) == null) {
                matchesTable.put(pair.getStringL1(), new ArrayList<TranslationUS>());
            }
            matchesTable.get(pair.getStringL1()).add(new TranslationUS(pair.getStringL1(), pair.getScore()));
        }

        // creates the match objects
        List<MatchUS> newMatches = new ArrayList<MatchUS>();
        for (String matchString : matchesTable.keySet()) {
            newMatches.add(new MatchUS(matchString, matchesTable.get(matchString)));
        }
        matches = Collections.synchronizedList(newMatches); // throws away previous matches if there were any
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
        for (MatchUS match : matches) {
            match.deleteFromDatabase(dbSession);
        }
        matches = null;
    }

    public void saveToDatabase(Session dbSession) {
        saveJustObject(dbSession);

        // save or update also all dependent matches
        for (MatchUS match : matches) {
            match.saveToDatabase(dbSession);
        }
    }

    public void deleteFromDatabase(Session dbSession) {
        deleteJustObject(dbSession);
        deleteMatches(dbSession);
    }
}

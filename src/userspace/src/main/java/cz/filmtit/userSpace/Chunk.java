package cz.filmtit.userSpace;

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
     * @param documentDbId
     * @param startTime
     * @param endTime
     * @param text
     */
    public Chunk(int documentDbId, String startTime, String endTime, String text) {
        this.documentDbId = documentDbId;
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

    private int databaseId;
    private int documentDbId;
    private String startTime;
    private String endTime;
    private String text;
    private String translation;
    private int partNumber;
    private boolean done;
    private Document parent;
    private List<Match> matches;

    public int getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(int databaseId) {
        this.databaseId = databaseId;
    }

    public int getDocumentDbId() {
        return documentDbId;
    }

    public void setDocumentDbId(int documentDbId) {
        this.documentDbId = documentDbId;
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

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
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

    /**
     * Fake setter for distance of the translation from the closest matched translation.
     * It's necessary for the Hibernate to have it.
     */
    public void setTranslationDistance(float tranlationDistance) {}

    /**
     * Gets the minimum distance of the user's translation from the TM-generated translations.
     */
    public float getTranslationDistance() {
        //TODO: getter for the translation distance
        return 0;
    }

    // TODO: method that loads the Chunk matches from the User Space database

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

        // here probably a JSON response will be generated
    }
}

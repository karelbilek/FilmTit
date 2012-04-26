package cz.filmtit.userspace;

import cz.filmtit.core.Factory;
import cz.filmtit.core.model.TranslationMemory;
import cz.filmtit.share.Chunk;
import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.TranslationPair;
import cz.filmtit.share.TranslationResult;
import org.hibernate.Session;

import java.util.*;

/**
 * Represents a subtitle chunk together with its timing, translation suggestions from the translation memory
 * and also the user translation.
 * @author Jindřich Libovický
 */
public class USTranslationResult extends DatabaseObject {
    private long documentDatabaseId;
    private TranslationResult translationResult;
    private USDocument parent;

    public USTranslationResult(TimedChunk chunk) {
        translationResult = new TranslationResult();
        generateMTSuggestions();
        // TODO: and save to the database as soon as possible to have the ID
    }

    /**
     * Default constructor for Hibernate.
     */
    public USTranslationResult() {
        // TODO: do the constructor
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Creates an instance of User Space Chunk from the shared Match.
     *
     * It just assigns it to the inner variable, User Space objects
     * wrapping the contained translations are created when necessary.
     * @param c
     */
    public USTranslationResult(TranslationResult c) {
        translationResult = c;
    }
    
    public TranslationResult getTranslationResult() {
	    return translationResult;
	}

    public int hashCode() {
        return translationResult.hashCode();
    }    
    
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass()) { return false; }
        return  translationResult.equals(((USTranslationResult)obj).translationResult);
    }

    public long getDocumentDatabaseId() {
        return documentDatabaseId;
    }

    public void setDocumentDatabaseId(long documentDatabaseId) {
        this.documentDatabaseId = documentDatabaseId;
    }

    public String getStartTime() {
        return translationResult.getSourceChunk().getStartTime();
    }

    public void setStartTime(String startTime) {
        translationResult.getSourceChunk().setStartTime(startTime);
    }

    public String getEndTime() {
        return translationResult.getSourceChunk().getEndTime();
    }

    public void setEndTime(String endTime) {
        translationResult.getSourceChunk().setEndTime(endTime);
    }

    public String getText() {
        return translationResult.getSourceChunk().getSurfaceform();
    }

    public void setText(String text) throws IllegalAccessException {
        translationResult.getSourceChunk().setSurfaceform(text);
    }

    public String getUserTranslation() {
        return translationResult.getUserTranslation();
    }

    public void setUserTranslation(String userTranslation) {
        translationResult.setUserTranslation(userTranslation);
    }

    public int getPartNumber() {
        return translationResult.getSourceChunk().getPartNumber();
    }

    public void setPartNumber(int partNumber) {
        translationResult.getSourceChunk().setPartNumber(partNumber);
    }

    public void generateMTSuggestions() {
        // TODO: Make this method parallel

        // dereference of current suggestion will force hibernate to
        translationResult.setTmSuggestions(null);

        TranslationMemory TM = Factory.createTM(true);
        
        scala.collection.immutable.List<TranslationPair> TMResults =
                TM.nBest(translationResult.getSourceChunk(), parent.getLanguage(), parent.getMediaSource(), 10, false);

        // the retrieved Scala collection must be transformed to a Java collection
        // otherwise it cannot be iterated by the for loop
        Collection<TranslationPair> javaList =
                scala.collection.JavaConverters.asJavaCollectionConverter(TMResults).asJavaCollection();

        // the list of suggestions will be stored as a synchronized list
        translationResult.setTmSuggestions(Collections.synchronizedList(new ArrayList<TranslationPair>(javaList)));
    }

    public void saveToDatabase(Session dbSession) {
        saveJustObject(dbSession);
    }

    public void deleteFromDatabase(Session dbSession) {
        deleteJustObject(dbSession);
    }
}

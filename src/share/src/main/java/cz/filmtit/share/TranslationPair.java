package cz.filmtit.share;

import org.hibernate.annotations.Type;

import java.io.Serializable;
import java.util.*;

/**
* Wrapper class for chunks in the parallel data. In the most basic case,
* a chunk only consists of the the surface form in a particular language.
* Chunks can also have annotations, e.g. Named Entities.
*
* @author Joachim Daiber
*/
public class TranslationPair implements Comparable<TranslationPair>, com.google.gwt.user.client.rpc.IsSerializable, Serializable {

    private Long id;
    private Chunk chunkL1;
    private Chunk chunkL2;
    private TranslationSource source;
    private List<MediaSource> mediaSources = new LinkedList<MediaSource>();
    private Double score;
    private int count = 1;

    public TranslationPair() {
    	// nothing
    }
    
    public TranslationPair(String chunkL1, String chunkL2) {
        this(new Chunk(chunkL1), new Chunk(chunkL2), TranslationSource.UNKNOWN);
    }

    public TranslationPair(Chunk chunkL1, Chunk chunkL2) {
        this(chunkL1, chunkL2, TranslationSource.UNKNOWN);
    }

    public TranslationPair(Chunk chunkL1, Chunk chunkL2, TranslationSource source, List<MediaSource> mediaSources) {
        this(chunkL1, chunkL2, source);
        this.mediaSources = mediaSources;
    }

    public TranslationPair(Chunk chunkL1, Chunk chunkL2, TranslationSource source) {
        this.chunkL1 = chunkL1;
        this.chunkL2 = chunkL2;
        this.source = source;
    }

    public TranslationPair(Chunk chunkL1, Chunk chunkL2, TranslationSource source, Double score) {
        this.chunkL1 = chunkL1;
        this.chunkL2 = chunkL2;
        this.source = source;
        this.score = score;
    }


    public boolean hasMediaSource() {
        return !(mediaSources.isEmpty());
    }

    public MediaSource getMediaSource() {
        if (mediaSources.isEmpty()) {
            return null;

        }
        return mediaSources.get(0);
    }

    public List<MediaSource> getMediaSources() {
        return mediaSources;
    }

    /**
     * Getter that return the media source as a set. It is used by Hibernate only.
     * @return The set of media sources.
     */
    private Set<MediaSource> getMediaSourcesSet() {
        return new HashSet<MediaSource>(mediaSources);
    }

    /**
     * Setter that sets the list of media sources given in the form of set. It is used by Hibernate only.
     * @param sources A set of media sources.
     */
    private void setMediaSourcesSet(Set<MediaSource> sources) {
        this.mediaSources = new ArrayList<MediaSource>(sources);
    }

    public void addMediaSource(MediaSource mediaSource) {
        this.mediaSources.add(mediaSource);
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Chunk getChunkL1() {
        return chunkL1;
    }

    public Chunk getChunkL2() {
        return chunkL2;
    }

    public TranslationSource getSource() {
        return source;
    }

    public Double getScore() {
        return score;
    }

    public int compareTo(TranslationPair that) {
        return (int) Math.signum(that.score - this.score);
    }

    public String toExternalString() {
        return chunkL1 + "\t" + chunkL2;
    }

    @Type(type="text")
    public String getStringL1() {
        if (chunkL1 == null) {return "";}
        return chunkL1.getSurfaceForm();
    }

    @Type(type="text")
    public void setStringL1(String stringL1) {
        if (chunkL1 != null) { chunkL1.setSurfaceForm(stringL1); }
        else { chunkL1 = new Chunk(stringL1); }
    }

    @Type(type="text")
    public String getStringL2() {
        if (chunkL2 == null) {return "";}
        return chunkL2.getSurfaceForm();
    }

    @Type(type="text")
    public void setStringL2(String stringL2) {
        if (chunkL2 != null) { chunkL2.setSurfaceForm(stringL2); }
        else { chunkL2 = new Chunk(stringL2); }
    }

    private Long getId() {
        return id;
    }

    private void setId(Long id) {
        this.id = id;
    }

    public static TranslationPair fromString(String string) {
        String[] split = string.trim().split("\t");

        switch(split.length) {
            case 2: return new TranslationPair(split[0], split[1]);
            default: return null;
        }
    }

    @Override
    public String toString() {
        if (this.score != null) {
            //return String.format("TP[Score: %2f, %s, %s]", score, chunkL1.getSurfaceform(), chunkL2.getSurfaceform());
        	// GWT does not know String.format - rewritten:
        	return ("TP[Score: " + score + ", " + chunkL1.getSurfaceForm() + ", " + chunkL2.getSurfaceForm() + "]");
        } else {
            //return String.format("TP[%s, %s]", chunkL1.getSurfaceform(), chunkL2.getSurfaceform());
        	// GWT does not know String.format - rewritten:
        	return ("TP[" + chunkL1.getSurfaceForm() + ", " + chunkL2.getSurfaceForm() + "]");
        }
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }


}

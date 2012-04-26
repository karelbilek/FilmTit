package cz.filmtit.share;

import java.util.LinkedList;
import java.util.List;

/**
* Wrapper class for chunks in the parallel data. In the most basic case,
* a chunk only consists of the the surface form in a particular language.
* Chunks can also have annotations, e.g. Named Entities.
*
* @author Joachim Daiber
*/
public class TranslationPair implements Comparable<TranslationPair> {

    private Chunk chunkL1;
    private Chunk chunkL2;
    private TranslationSource source;
    private List<MediaSource> mediaSources = new LinkedList<MediaSource>();
    private Double score;

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

    public MediaSource getMediaSource() {
        return mediaSources.get(0);
    }

    public List<MediaSource> getMediaSources() {
        return mediaSources;
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

    public String getStringL1() {
        return chunkL1.getSurfaceform();
    }

    public String getStringL2() {
        return chunkL2.getSurfaceform();
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
            return String.format("TP[Score: %2f, %s, %s]", score, chunkL1.getSurfaceform(), chunkL2.getSurfaceform());
        } else {
            return String.format("TP[%s, %s]", chunkL1.getSurfaceform(), chunkL2.getSurfaceform());
        }
    }



}

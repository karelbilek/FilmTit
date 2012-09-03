/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/

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

    /**
     * Id of TranslationPair
     */
    private Long id = (long) -1;
    /**
     * Chunk from source language
     */
    private Chunk chunkL1;
    /**
     * Chunk from target language
     */
    private Chunk chunkL2;
    /**
     * Source of suggestion translation
     */
    private TranslationSource source;
    /**
     *
     */
    private List<MediaSource> mediaSources = new LinkedList<MediaSource>();
    /**
     * Score of translations
     */
    private Double score;
    /**
     * Count of occurrences
     */
    private int count = 1;

    public TranslationPair() {
    	// nothing
    }

    /**
     * Creates TranslationPair from chunks
     * @param chunkL1  Source surface form
     * @param chunkL2  Target surface form
     */
    public TranslationPair(String chunkL1, String chunkL2) {
        this(new Chunk(chunkL1), new Chunk(chunkL2), TranslationSource.UNKNOWN);
    }

    /**
     * Creates TranslationPair from chunks
     * @param chunkL1 Chunk from source language
     * @param chunkL2 Chunk from target language
     */
    public TranslationPair(Chunk chunkL1, Chunk chunkL2) {
        this(chunkL1, chunkL2, TranslationSource.UNKNOWN);
    }

    /**
     * Creates TranslatinoPair from chunks with source and mediasurces
     * @param chunkL1 Chunk from source language
     * @param chunkL2 Chunk from target language
     * @param source  Searcher, which found TranslationPair
     * @param mediaSources
     */
    public TranslationPair(Chunk chunkL1, Chunk chunkL2, TranslationSource source, List<MediaSource> mediaSources) {
        this(chunkL1, chunkL2, source);
        this.mediaSources = mediaSources;
    }

    /**
     * Creates TranslatinoPair from chunks with source
     * @param chunkL1 Chunk from source language
     * @param chunkL2 Chunk from target language
     * @param source  Searcher, which found TranslationPair
     */
    public TranslationPair(Chunk chunkL1, Chunk chunkL2, TranslationSource source) {
        this.chunkL1 = chunkL1;
        this.chunkL2 = chunkL2;
        this.source = source;
    }
    /**
     * Creates TranslatinoPair from chunks with source and score
     * @param chunkL1 Chunk from source language
     * @param chunkL2 Chunk from target language
     * @param source  Searcher, which found TranslationPair
     * @param score   Probability that translation pair is right
     */
    public TranslationPair(Chunk chunkL1, Chunk chunkL2, TranslationSource source, Double score) {
        this.chunkL1 = chunkL1;
        this.chunkL2 = chunkL2;
        this.source = source;
        this.score = score;
    }

    /**
     * Contains mediaSources
     * @return  Boolean
     */
    public boolean hasMediaSource() {
        return !(mediaSources.isEmpty());
    }

    /**
     * Gets first MediaSource
     * @return  MediaSource
     */
    public MediaSource getMediaSource() {
        if (mediaSources.isEmpty()) {
            return null;

        }
        return mediaSources.get(0);
    }

    /**
     * Gets all MediaSources
     * @return  Sll MediaSources
     */
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

    /**
     * Add new mediaSource
     * @param mediaSource
     */
    public void addMediaSource(MediaSource mediaSource) {
        this.mediaSources.add(mediaSource);
    }

    /**
     * Sets score
     * @param score
     */
    public void setScore(Double score) {
        this.score = score;
    }

    /**
     * Gets chunk from source language
     * @return  Chunk from source language
     */
    public Chunk getChunkL1() {
        return chunkL1;
    }

    /**
     * Gets chunk from target language
     * @return  Chunk from target language
     */
    public Chunk getChunkL2() {
        return chunkL2;
    }

    /**
     * Gets source searcher
     * @return  TranslationSource
     */
    public TranslationSource getSource() {
        return source;
    }

    /**
     * Gets score
     * @return Double
     */
    public Double getScore() {
        return score;
    }

    /**
     * Compares score of two TranslationsPairs
     * @param that  Other TranslationPair
     * @return  int  signum from (that.score - this.score)
     */
    public int compareTo(TranslationPair that) {
        return (int) Math.signum(that.score - this.score);
    }

    /**
     * Gets whole pair
     * @return  Strings created from chunkL1 and chunkL2
     */
    public String toExternalString() {
        return chunkL1 + "\t" + chunkL2;
    }

    /**
     * Gets surface form of source language
     * @return Surface form of source language chunk
     */
    @Type(type="text")
    public String getStringL1() {
        if (chunkL1 == null) {return "";}
        return chunkL1.getSurfaceForm();
    }

    /**
     * Sets chunk of source language
     * @param stringL1 Surface form of source language chunk
     */
    @Type(type="text")
    public void setStringL1(String stringL1) {
        if (chunkL1 != null) { chunkL1.setSurfaceForm(stringL1); }
        else { chunkL1 = new Chunk(stringL1); }
    }

    /**
     * Gets surface form of target language
     * @return Surface form of target language chunk
     */
    @Type(type="text")
    public String getStringL2() {
        if (chunkL2 == null) {return "";}
        return chunkL2.getSurfaceForm();
    }

    /**
     * Sets chunk of target language
     * @param stringL2  Surface form of target language chunk
     */
    @Type(type="text")
    public void setStringL2(String stringL2) {
        if (chunkL2 != null) { chunkL2.setSurfaceForm(stringL2); }
        else { chunkL2 = new Chunk(stringL2); }
    }

    /**
     * Gets id of TranslationPair
     * @return  Identificator
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets id of TranslationPair
     * @param id  Identificator
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Creates TranslationPair from string like "w1\tw2"
     * @param string
     * @return  TranslationPair
     */
    public static TranslationPair fromString(String string) {
        String[] split = string.trim().split("\t");

        switch(split.length) {
            case 2: return new TranslationPair(split[0], split[1]);
            default: return null;
        }
    }

    /**
     * Gets string represents TranslationPair
     * @return if have score prits score too otherwise only surface forms
     */
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

    /**
     * Sets count of occurrences
     * @param count
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * Gets count of occurrences
     * @return
     */
    public int getCount() {
        return count;
    }


}

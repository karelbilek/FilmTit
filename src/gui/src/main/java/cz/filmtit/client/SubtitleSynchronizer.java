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

package cz.filmtit.client;
import java.util.*;
import cz.filmtit.share.*;

/**
 * Class, that holds the currently displayed subtitle chunks (both source and targets)
 * and helps to get them quickly according to both ChunkIndex and time.
 *
 * It was created because the various maps were used throughout the TranslationWorkspace,
 * but we found out that it's more clean to move the functionality connected with retrieving
 * chunks to special class.
 *
 * @author Karel Bílek
 */
public class SubtitleSynchronizer {

    private Map<ChunkIndex, TimedChunk> chunkByIndex = new HashMap<ChunkIndex, TimedChunk>();
    private Map<ChunkIndex, Integer> indexesOfDisplayed = new HashMap<ChunkIndex, Integer>();
    private TreeMap<ChunkTimePosition, TranslationResult> resultsByTime = 
        new TreeMap<ChunkTimePosition, TranslationResult>();

    /**
     * Returns translation results that start between two time points.
     * It should be in O(log n) time, according to Java documentation of TreeMap.
     * @param start Start time in milliseconds
     * @param end End time in milliseconds
     * @return translation results that start between two time points
     */
    public Collection<TranslationResult> getTranslationResultsByTime(double start, double end) {
        ChunkTimePosition posStart = new ChunkTimePosition(start, 0);
        ChunkTimePosition posEnd = new ChunkTimePosition(end, 100);

        return resultsByTime.subMap(posStart, posEnd).values();
    }
    
    /**
     * Return all chunks with the given id,
     * order by their partNumbers.
     * Returns an empty collection if there is no such chunk.
     * In the current implementation, it iteratively tries
     * to get a chunk by a generated ChunkIndex,
     * starting with partNumber = 1,
     * until there is no such chunk.
     * This might be made more efficient by having a storage of lists of chunks
     * indexed by ids; however, it is not very slow, given that every chunk usually has
     * maximally 3 partnumbers.
     * @param id
     * @return
     */
    public List<TimedChunk> getChunksById(int id) {
    	List<TimedChunk> result = new LinkedList<TimedChunk>();
		int partNumber = 1;
		ChunkIndex chunkIndex = new ChunkIndex(partNumber, id);
		while (chunkByIndex.containsKey(chunkIndex)) {
			// add to result
			result.add(chunkByIndex.get(chunkIndex));
			// move on
			partNumber++;
			chunkIndex = new ChunkIndex(partNumber, id);
		}
		return result;
    }

    /**
     * Remembers a source chunk
     * @param tr Translation result, of which we want to remember source chunk.
     * @param displayedIndex If it's displayed, the index of the suggestbox. If it's not, it's ignored.
     * @param isDisplayed Is this chunk displayed within suggestbox?
     */
    public void putSourceChunk(TranslationResult tr, Integer displayedIndex, boolean isDisplayed) {
        putSourceChunk(tr.getSourceChunk(), displayedIndex, isDisplayed);
    }

    /**
     * Remembers a source chunk
     * @param chunk Source chunk to remember.
     * @param displayedIndex If it's displayed, the index of the suggestbox. If it's not, it's ignored.
     * @param isDisplayed Is this chunk displayed within suggestbox?
     */
    public void putSourceChunk(TimedChunk chunk, Integer displayedIndex, boolean isDisplayed) {
        ChunkIndex chunkIndex = chunk.getChunkIndex();
        chunkByIndex.put(chunkIndex, chunk);
        if (isDisplayed) {
            indexesOfDisplayed.put(chunkIndex, displayedIndex);
        }
    }

    /**
     * Remember a translation result.
     * @param result Translation result to remember.
     */
    public void putTranslationResult(TranslationResult result) {
        TimedChunk chunk = result.getSourceChunk();
        resultsByTime.put(new ChunkTimePosition(chunk), result);
    }

    /**
     * Gets chunk by a given index.
     * @param index ChunkIndex that we want.
     * @return The chunk with a given index.
     */
    public TimedChunk getChunkByIndex(ChunkIndex index) {
        return chunkByIndex.get(index);
    }

    /**
     * Do we have a chunk with a given index?
     * @param index  ChunkIndex that we want.
     * @return True iff we have a given index.
     */
    public boolean hasChunkWithIndex(ChunkIndex index) {
        return chunkByIndex.containsKey(index);
    }

    /**
     * Gets index of suggestbox of a given chunkindex.
     * @param chunkIndex Chunkindex that we want an index for.
     * @return index of suggestbox.
     */
    public int getIndexOf(ChunkIndex chunkIndex) {
        return indexesOfDisplayed.get(chunkIndex);
    }

    /**
     * Gets index of suggestbox of a given chunk.
     * @param chunk Chunk that we want an index for.
     * @return index of suggestbox.
     */
    public int getIndexOf(TimedChunk chunk) {
        return getIndexOf(chunk.getChunkIndex());
    }

    /**
     * Is chunk with this chunkindex displayed in suggestbox?
     * @param chunkIndex Chunkindex of chunk that we are looking for.
     * @return true iff it is.
     */
    public boolean isChunkDisplayed(ChunkIndex chunkIndex) {
        return indexesOfDisplayed.containsKey(chunkIndex);
    }

    /**
     * Is this chunk displayed in suggestbox?
     * @param chunk  chunk that we are looking for.
     * @return true iff it is.
     */
    public boolean isChunkDisplayed(TimedChunk chunk) {
        return isChunkDisplayed(chunk.getChunkIndex());
    }

    /**
     * Is chunk with this translation result displayed in suggestbox?
     * @param tr translationresult that we are looking for.
     * @return true iff it is.
     */
    public boolean isChunkDisplayed(TranslationResult tr) {
        return isChunkDisplayed(tr.getSourceChunk());
    }

    /**
     * Gets index of suggestbox of a given translation result.
     * @param tr Translation result that we want an index for.
     * @return index of suggestbox.
     */
    public int getIndexOf(TranslationResult tr) {
        return getIndexOf(tr.getSourceChunk());
    }

 
}

/**
 * Class, representing position of chunk in time.
 * Time only is not sufficient, because when two chunks are displayed at the same time,
 * the order is still important.
 */
class ChunkTimePosition implements Comparable<ChunkTimePosition> {
    public double time;
    public int part;

    public ChunkTimePosition (double time, int part) {
        this.time = time;
        this.part = part;
    }

    public ChunkTimePosition(TimedChunk chunk) {
        this.time=(double)(chunk.getStartTimeLong());
        this.part = chunk.getPartNumber();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChunkTimePosition)) {
            return false;
        }
        ChunkTimePosition other = (ChunkTimePosition) o;
        return (Math.abs(other.time-time)<1 && other.part == part);
    }

    @Override
    public int compareTo(ChunkTimePosition other) {
        if (Math.abs(other.time - time)<1) {
            //the same time
            return this.part - other.part;
        } else {
            if (time > other.time) {
                return 1;
            } else if (time < other.time) {
                return -1;
            }
            return 0;
        }
    }

}
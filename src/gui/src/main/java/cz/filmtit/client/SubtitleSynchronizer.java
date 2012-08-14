package cz.filmtit.client;
import java.util.*;
import cz.filmtit.share.*;

public class SubtitleSynchronizer {
    private Map<ChunkIndex, TimedChunk> chunkByIndex = new HashMap<ChunkIndex, TimedChunk>();
    private Map<ChunkIndex, Integer> indexesOfDisplayed = new HashMap<ChunkIndex, Integer>();
    private TreeMap<ChunkTimePosition, TranslationResult> resultsByTime = 
        new TreeMap<ChunkTimePosition, TranslationResult>();
    
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
                //stejny cas
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
     * indexed by ids.
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
    
    public void putSourceChunk(TranslationResult tr, Integer index, boolean isDisplayed) {
        putSourceChunk(tr.getSourceChunk(), index, isDisplayed);
    }

    public void putSourceChunk(TimedChunk chunk, Integer index, boolean isDisplayed) {
        ChunkIndex chunkIndex = chunk.getChunkIndex();
        chunkByIndex.put(chunkIndex, chunk);
        if (isDisplayed) {
            indexesOfDisplayed.put(chunkIndex, index);
        }
    }

    public void putTranslationResult(TranslationResult result) {
        TimedChunk chunk = result.getSourceChunk();
        resultsByTime.put(new ChunkTimePosition(chunk), result);
    }

    public TimedChunk getChunkByIndex(ChunkIndex index) {
        return chunkByIndex.get(index);
    }

    public boolean hasChunkWithIndex(ChunkIndex index) {
        return chunkByIndex.containsKey(index);
    }

    public int getIndexOf(ChunkIndex chunkIndex) {
        return indexesOfDisplayed.get(chunkIndex);
    }


    public int getIndexOf(TimedChunk chunk) {
        return getIndexOf(chunk.getChunkIndex());
    }

    public boolean isChunkDisplayed(ChunkIndex chunkIndex) {
        return indexesOfDisplayed.containsKey(chunkIndex);
    }


    public boolean isChunkDisplayed(TimedChunk chunk) {
        return isChunkDisplayed(chunk.getChunkIndex());
    }

    public boolean isChunkDisplayed(TranslationResult tr) {
        return isChunkDisplayed(tr.getSourceChunk());
    }
    public int getIndexOf(TranslationResult tr) {
        return getIndexOf(tr.getSourceChunk());
    }

 
}

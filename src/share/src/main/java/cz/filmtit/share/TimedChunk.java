package cz.filmtit.share;

import cz.filmtit.share.annotations.Annotation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TimedChunk extends Chunk implements com.google.gwt.user.client.rpc.IsSerializable,
        Serializable, Comparable<TimedChunk> {

    /**
     * Start time of the chunk
     */
    private volatile String startTime;
    /**
     * End time of the chunk
     */
    private volatile String endTime;
    /**
     * Number of part of the subtitle item is the chunk part of
     */
    private volatile int partNumber;

    /**
     * Index of the chunk.
     */
    public ChunkIndex chunkIndex;


    /**
     * A unique identifier of the subtitle item within a document.
     */
    private int id = Integer.MIN_VALUE;

    /**
     * ID of the document the chunk belongs to
     */
    private long documentId = Long.MIN_VALUE;

    /**
     * Default constructor for GWT.
     */
    public TimedChunk() {
    	// nothing;    	
    }

    /**
     * Gets the chunk index.
     * @return Index of the chunk.
     */
    public ChunkIndex getChunkIndex() {
        return chunkIndex;
    }

    /**
     * Finds out of the timed chunk has the same timing as a different one.
     * @param other Other timed chunk
     * @return Flag if the timings are the same
     */
    public boolean sameTimeAs(TimedChunk other) {
        if (other==null) {
            return false;
        }        
        return this.startTime.equals(other.startTime) && this.endTime.equals(other.endTime);
    }

    /**
     * Gets flag if the timing is in sub format.
     * @return
     */
    public boolean isSubTime() {
        return !isSrtTime();
    }

    /**
     * Gets flag if the timing is in srt format.
     * @return
     */
    public boolean isSrtTime() {
        return (this.getStartTime().contains(":"));
    
    }

    /**
     * Joins the chunk with a different one if they are from one split subtitle item in order to
     * be exported to a file.
     * @param other Time chunk to be split with this one
     * @param type  Type of file being exported.
     * @return Timed chunk representing the joined timed chunk.
     */
    public TimedChunk joinWith(TimedChunk other, FileType type) {
        String joiner = getJoiner(type);
        int size = this.getSurfaceForm().length() + joiner.length();
        String joined = this.getSurfaceForm() + joiner + other.getSurfaceForm();
        

        ArrayList<Annotation> anots = new ArrayList<Annotation>(other.getAnnotations().size() + getAnnotations().size());
        anots.addAll(this.getAnnotations());
        for (Annotation a:other.getAnnotations()) {
           anots.add(new Annotation(a.getType(), a.getBegin()+size, a.getEnd()+size));
        }
        return new TimedChunk(joined, this.getStartTime(), this.getEndTime(), anots);

    }

    /**
     * Export file types
     */
    public enum FileType{
      SRT,
      SUB,
      TXT
    }

    /**
     * Gets the line separator for particular file type.
     * @param ft Type of exported file
     * @return Suitable line separator
     */
    public String getJoiner(FileType ft) {
        if (ft == FileType.SRT) {
            return "\n";
        } else if (ft == FileType.SUB) {
            return "|";
        } else {
            return " ";
        }
    }

    /**
     * Gets the text form of the timed chunk to the exported file of given type.
     * @param type Type of exported file
     * @param order Order of the chunk in the file (required by srt)
     * @param fps  Frames per second (required by sub)
     * @return Text form to the timed chunk
     */
    public StringBuilder getFileForm(FileType type, int order, double fps) {
        switch (type) {
            case SRT: return getSrtForm(order, fps);
            case SUB: return getSubForm(fps);
            case TXT: return new StringBuilder(this.getSurfaceForm()).append("\n");
        }
        return null; //<-should not happen
    }

    /**
     * Gets the text form of the timed chunk in the SUB format.
     * @param fps Frames per second
     * @return Timed chunk in SUB format
     */
    public StringBuilder getSubForm(double fps) {
        String displayForm = getFormatedForm("- ", "|");

        return getSubTime(fps).append("{").append(displayForm).append("}").append("\n");
    }

    /**
     * Gets the text form of the timed chunk in the SRT format.
     * @param order Number of the timed chunk in the file
     * @param fps Frames per second
     * @return Timed chunk in SRT format
     */
    public StringBuilder getSrtForm(int order, double fps) {
        String displayForm = getFormatedForm("- ", "\n");
       
        return new StringBuilder().append(order).append("\n").append(getSrtTime(fps)).append("\n").append(displayForm).append("\n\n");
    }

    /**
     * Gets the timing in SUB format.
     * @param fps Frames per second
     * @return Timing in SUB format
     */
    public StringBuilder getSubTime(double fps) {
        return new StringBuilder("{").append(getSubStartTime(fps)).append("}{").append(getSubEndTime(fps)).append("}");
    }

    /**
     * Gets the timing in SRT format.
     * @param fps Frames per second
     * @return Timing in SRT format
     */
    public StringBuilder getSrtTime(double fps) {
        return getSrtStartTime(fps).append(" --> ").append(getSrtEndTime(fps));
    }

    /**
     * Gets the timing of the timed chunk in either SUB ort SRT format.
     * @param begin Flag if it is a starting time
     * @param srt Flak if it is in SRT format
     * @param fps Frames per second
     * @return Timing of the chunk in text format
     */
    public StringBuilder getPartFormatedTime(boolean begin, boolean srt, double fps) {
        String time = begin?getStartTime() : getEndTime();
        boolean right = (srt == isSrtTime());
        if (right) {
            return new StringBuilder().append(time);
        } else {
            return srt?subTimeToSrt(time, fps):srtTimeToSub(time, fps);
        }
    }

    /**
     * Gets the start time (of this timed chunk) in SRT format
     * @param fps Frames per second
     * @return  Start time in SRT format
     */
    public StringBuilder getSrtStartTime(double fps) {
        return getPartFormatedTime(true, true, fps);   
    }

    /**
     * Gets the start time (of this timed chunk) in SUB format
     * @param fps Frames per second
     * @return Start time in SUB format
     */
    public StringBuilder getSubStartTime(double fps) {
        return getPartFormatedTime(true, false, fps);   
    }

    /**
     * Gets the end time (of this chunk) in SRT format
     * @param fps Frames per second
     * @return End time in SRT format
     */
    public StringBuilder getSrtEndTime(double fps) {
        return getPartFormatedTime(false, true, fps);   
    }

    /**
     * Gets the end time (of this chunk) in SUB format
     * @param fps Frames per second
     * @return  Ent time in SUB format
     */
    public StringBuilder getSubEndTime(double fps) {
        return getPartFormatedTime(false, false, fps);   
    }

    /**
     * Converts SRT time to SUB format.
     * @param time SRT time
     * @param fps Frames per second
     * @return Time in SUB format
     */
    public static StringBuilder srtTimeToSub(String time, double fps) {
        double t = timeToLong(time);
        double frame = t * fps / 1000;
        Long framelong = (long) frame;
        return new StringBuilder().append(framelong.toString());
    }

    /**
     * Converts milliseconds to time string.
     * @param allMillis time in milliseconds
     * @param includeMillis Flag if the millisecond should include or disregarded
     * @return Time as milliseconds
     */
    public static StringBuilder millisToTime(Long allMillis, boolean includeMillis) {
        long millis = allMillis % 1000;
        long seconds = ((long)(allMillis / 1000)) % 60;
        long minutes = ((long)(allMillis / (1000*60))) % 60;
        long hours = ((long)(allMillis / (1000*60*60)));
        StringBuilder res = new StringBuilder().append(hours).append(":").append(minutes).append(":").append(seconds);
        if (includeMillis) {
            res = res.append(".").append(millis);
        }
        return res;
 
    }

    /**
     * Converts SUB format time to SRT format.
     * @param frame SUB format time
     * @param fps Frames per second
     * @return SRT format time
     */
    public static StringBuilder subTimeToSrt(String frame, double fps) {
        Double f = new Long(frame).doubleValue();
        long allMillis = (long)(f*1000/fps);
        return millisToTime(allMillis, true);
    }

    /**
     * Converts SRT time to long
     * @param time SRT format time
     * @return Time as long
     */
    public static long timeToLong(String time) {
        String[] times = time.split("[,:.]");
        Long hour = Long.valueOf(times[0].replaceAll(" ",""));
        Long minute = Long.valueOf(times[1].replaceAll(" ",""));
        Long second = Long.valueOf(times[2].replaceAll(" ",""));
        Long mili = Long.valueOf(times[3].replaceAll(" ", ""));
        return hour*3600*1000+minute*60*1000+second*1000+mili;
    }
/*    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index=index;
    }*/

    /**
     * Creates a timed chunk from surface form, timing nad annotations.
     * @param surfaceForm Surface form of chunk
     * @param startTime Start time of the chunk
     * @param endTime End time of the chunk
     * @param annotations Chunk annotations
     */
    public TimedChunk(String surfaceForm, String startTime, String endTime, List<Annotation> annotations) {
        super(surfaceForm, annotations);
        this.startTime = startTime;
        this.endTime = endTime;
        
    }

    /**
     * Creates a timed chunk of given timing, index and text.
     * @param startTime Start time of the chunk
     * @param endTime End time of the chunk
     * @param partNumber Number of the part in subtitle item
     * @param text Text of the chunk
     * @param id Subtitle item ID
     * @param documentId Document ID
     */
    public TimedChunk(String startTime, String endTime, int partNumber, String text, int id, long documentId) {
        super(text);
        this.startTime = startTime;
        this.endTime = endTime;
        this.partNumber = partNumber;
		this.id = id;
		this.documentId = documentId;
        this.chunkIndex = new ChunkIndex(partNumber, id);
    }

    /**
     * Gets the start time as a non-zero long (i.e. gets one instead of zero)
     * @return  Start time as a non-zero number
     */
    public long getStartTimeLongNonZero() {
        long r = timeToLong(getStartTime());
        if (r<=0) {
            return 1;
        }
        return r;
    }

    /**
     * Gets chunks start time as long
     * @return Chunks start time as long
     */
    public long getStartTimeLong() {
        return timeToLong(getStartTime());
    }

    /**
     * Gets chunks end time as long
     * @return Chunks end time as long
     */
    public long getEndTimeLong() {
        return timeToLong(getEndTime());
    }

    /**
     * Gets the start time of the chunk
     * @return  New start of the chunk
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * Removes spaces from the string (used for timing postprocessing)
     * @param what A string
     * @return String without spaces
     */
    public static String chomp(String what) {
        String r = what;
        return r.replaceAll(" ", "");
   }

    /**
     * Sets the start time of the chunk
     * @param startTime New start of the chunk
     */
    public void setStartTime(String startTime) {
        this.startTime = chomp(startTime);
    }

    /**
     * Gets the end time of the chunk
     * @return  End time
     */
    public String getEndTime() {
        return endTime;
    }

    /**
     * Sets the end time of the chunk
     * @param endTime New end time
     */
    public void setEndTime(String endTime) {
        this.endTime = chomp(endTime);
    }

    /**
     * Gets which part of the subtitle item this timed chunk is.
     * @return  Part number in subtitle item
     */
    public int getPartNumber() {
        return partNumber;
    }

    /**
     * Sets which part of the subtitle item this timed chunk is.
     * @param partNumber Part number in subtitle item
     */
    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
        this.chunkIndex = new ChunkIndex(partNumber, id);
    }

    /**
     * Gets the subtitle item ID.
     * @return  Subtitle item ID.
     */
    public int getId() {
		return id;
	}

    /**
     * Sets the subtitle item id if it has not been set before, it throws an excpetion otherwise.
     * @param id Subtitle item ID
     */
    public void setId(int id) {
        if (this.id == id) { return; }
        if (this.id != Integer.MIN_VALUE) {
            throw new UnsupportedOperationException("Once the timed chunk ID is set, it cannot be changed.");
        }
        this.chunkIndex = new ChunkIndex(partNumber, id);
        this.id = id;
    }

    /**
     * Gets the ID of document this timed chunk belongs to
     * @return Document ID
     */
    public long getDocumentId() {
		return documentId;
	}

    /**
     * Sets the document ID if it has not been set before, otherwise it throws an excpetion.
     * @param documentId Document ID
     */
    public void setDocumentId(long documentId) {
        if (this.documentId == documentId) { return; }
        if (this.documentId != Long.MIN_VALUE) {
            throw new UnsupportedOperationException("Once the parent document ID is set, it cannot be changed.");
        }
        this.documentId = documentId;
    }

    
    /**
     * Comparing according to their order in the file/movie
     * - by startTime, endTime and partNumber, respectively.
     * (returns 0 iff all these three are the same)
     */
	@Override
	public int compareTo(TimedChunk that) {
		// - lexicographically for srt
		// - numerically for sub
		// - ???
		
		// ? this.startTime < that.startTime ?
		int result = this.startTime.compareTo(that.startTime);
		
		if (result == 0) {	// this.startTime == that.startTime
			// ? this.endTime < that.endTime ?
			result = this.endTime.compareTo(that.endTime);			
			
			if (result == 0) {	// this.endTime == that.endTime
				// ? this.partNumber < that.partNumber ?
				result = this.partNumber - that.partNumber;
			}
		}
		return result;
	}
	
	/**
	 * When comparing two TimedChunks, equal iff their proper compareTo returns 0.
	 */
	@Override
	public boolean equals(Object that) {
		if (that instanceof TimedChunk) {
			return (this.compareTo((TimedChunk)that) == 0) ? true : false;
		}
		else return super.equals(that);
	}
	
    /**
     * Displays the ChunkIndex and the surface form of the Chunk.
     */
    @Override
    public String toString() {
        return "TimedChunk[" + getChunkIndex() + ", " + getSurfaceForm() + "]";
    }
    
	/**
	 * Gets the timing interval in GUI format <br />
     * <code>hh:mm:ss,ttt - hh:mm:ss,ttt</code> (to be used to display in GUI)
	 */
    public String getDisplayTimeInterval() {
    	return SrtTime.toDisplayInterval(startTime, endTime);
    }
    
	/**
	 * Gets the timing interval in export format <br />
     * <code>hh:mm:ss,ttt --> hh:mm:ss,ttt</code> (to be used to export to SRT)
	 */
    public String getSrtTimeInterval() {
    	return SrtTime.toSrtInterval(startTime, endTime);
    }
    
}

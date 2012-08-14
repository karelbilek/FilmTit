package cz.filmtit.share;

import cz.filmtit.share.annotations.Annotation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TimedChunk extends Chunk implements com.google.gwt.user.client.rpc.IsSerializable,
        Serializable, Comparable<TimedChunk> {

	private volatile String startTime;
    private volatile String endTime;
    private volatile int partNumber;

    public ChunkIndex chunkIndex;


    /**
     * A unique identifier of the chunk within a document.
     */
    private int id = Integer.MIN_VALUE;
    
    // Document.id
    private long documentId = Long.MIN_VALUE;

    public TimedChunk() {
    	// nothing;    	
    }
    
    public ChunkIndex getChunkIndex() {
        return chunkIndex;
    }

    public boolean sameTimeAs(TimedChunk other) {
        if (other==null) {
            return false;
        }        
        return this.startTime.equals(other.startTime) && this.endTime.equals(other.endTime);
    }

    public boolean isSubTime() {
        return !isSrtTime();
    }

    public boolean isSrtTime() {
        return (this.getStartTime().contains(":"));
    
    }

    //this is immutable thing
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

    public enum FileType{
      SRT,
      SUB,
      TXT, //wtf is TXT format?
    }

    public String getJoiner(FileType ft) {
        if (ft == FileType.SRT) {
            return "\n";
        } else if (ft == FileType.SUB) {
            return "|";
        } else {
            return " ";
        }
    }

    public StringBuilder getFileForm(FileType type, int order, double fps) {
        switch (type) {
            case SRT: return getSrtForm(order, fps);
            case SUB: return getSubForm(fps);
            case TXT: return new StringBuilder(this.getSurfaceForm()).append("\n");
        }
        return null; //<-should not happen
    }

    public StringBuilder getSubForm(double fps) {
        // TODO: check if the annotations are resolved properly
        String displayForm = getFormatedForm("- ", "|");

        return getSubTime(fps).append("{").append(displayForm).append("}").append("\n");
    }

    public StringBuilder getSrtForm(int order, double fps) {
        // TODO: check if the annotations are resolved properly
        String displayForm = getFormatedForm("- ", "\n");
       
        return new StringBuilder().append(order).append("\n").append(getSrtTime(fps)).append("\n").append(displayForm).append("\n\n");
    }

    public StringBuilder getSubTime(double fps) {
        return new StringBuilder("{").append(getSubStartTime(fps)).append("}{").append(getSubEndTime(fps)).append("}");
    }
    
    public StringBuilder getSrtTime(double fps) {
        return getSrtStartTime(fps).append(" --> ").append(getSrtEndTime(fps));
    }

    public StringBuilder getPartFormatedTime(boolean begin, boolean srt, double fps) {
        String time = begin?getStartTime() : getEndTime();
        boolean right = (srt == isSrtTime());
        if (right) {
            return new StringBuilder().append(time);
        } else {
            return srt?subTimeToSrt(time, fps):srtTimeToSub(time, fps);
        }
    }

    public StringBuilder getSrtStartTime(double fps) {
        return getPartFormatedTime(true, true, fps);   
    }
   
    public StringBuilder getSubStartTime(double fps) {
        return getPartFormatedTime(true, false, fps);   
    }

    public StringBuilder getSrtEndTime(double fps) {
        return getPartFormatedTime(false, true, fps);   
    }
   
    public StringBuilder getSubEndTime(double fps) {
        return getPartFormatedTime(false, false, fps);   
    }

    public static StringBuilder srtTimeToSub(String time, double fps) {
        double t = timeToLong(time);
        double frame = t * fps / 1000;
        Long framelong = (long) frame;
        return new StringBuilder().append(framelong.toString());
    }

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

    public static StringBuilder subTimeToSrt(String frame, double fps) {
        Double f = new Long(frame).doubleValue();
        long allMillis = (long)(f*1000/fps);
        return millisToTime(allMillis, true);
    }
    
    //working only with SRT
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
   
    public TimedChunk(String surfaceForm, String startTime, String endTime, List<Annotation> annotations) {
        super(surfaceForm, annotations);
        this.startTime = startTime;
        this.endTime = endTime;
        
    }
 
    public TimedChunk(String startTime, String endTime, int partNumber, String text, int id, long documentId) {
        super(text);
        this.startTime = startTime;
        this.endTime = endTime;
        this.partNumber = partNumber;
		this.id = id;
		this.documentId = documentId;
        this.chunkIndex = new ChunkIndex(partNumber, id);
    }

    public long getStartTimeLongNonZero() {
        long r = timeToLong(getStartTime());
        if (r<=0) {
            return 1;
        }
        return r;
    }

    public long getStartTimeLong() {
        return timeToLong(getStartTime());
    }

    public long getEndTimeLong() {
        return timeToLong(getEndTime());
    }

    public String getStartTime() {
        return startTime;
    }

    public static String chomp(String what) {
        String r = what;
        return r.replaceAll(" ", "");
   }

    public void setStartTime(String startTime) {
        this.startTime = chomp(startTime);
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = chomp(endTime);
    }

    public int getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
        this.chunkIndex = new ChunkIndex(partNumber, id);
    }

	public int getId() {
		return id;
	}

    public void setId(int id) {
        if (this.id == id) { return; }
        if (this.id != Integer.MIN_VALUE) {
            throw new UnsupportedOperationException("Once the timed chunk ID is set, it cannot be changed.");
        }
        this.chunkIndex = new ChunkIndex(partNumber, id);
        this.id = id;
    }

	public long getDocumentId() {
		return documentId;
	}

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
		// TODO: compare differently for various subtitle formats, i.e.
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
	 * hh:mm:ss,ttt - hh:mm:ss,ttt
	 * (to be used to display in GUI)
	 */
    public String getDisplayTimeInterval() {
    	return SrtTime.toDisplayInterval(startTime, endTime);
    }
    
	/**
	 * hh:mm:ss,ttt --> hh:mm:ss,ttt
	 * (to be used to export to SRT)
	 */
    public String getSrtTimeInterval() {
    	return SrtTime.toSrtInterval(startTime, endTime);
    }
    
}

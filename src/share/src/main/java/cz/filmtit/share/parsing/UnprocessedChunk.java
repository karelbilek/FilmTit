package cz.filmtit.share.parsing;
/*
 * "Raw" chunk read from subtitle file
 *
 * is not a Chunk yet, it is just a time info and a string
 */


public class UnprocessedChunk {

    private String startTime;
    private String endTime;
    private String text;

    public UnprocessedChunk(String startTime, String endTime, String text) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.text=text;
    }
        
    public String getEndTime() {
        return endTime;
    }
    
    public String getStartTime() {
        return startTime;
    }

    public String getText() {
        return text;
    }

    public boolean equalsTo(UnprocessedChunk other) {
        return (startTime.equals(other.startTime) && endTime.equals(other.endTime) && text.equals(other.text));
    }

}


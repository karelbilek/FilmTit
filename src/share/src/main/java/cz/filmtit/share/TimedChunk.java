package cz.filmtit.share;

import java.io.Serializable;

public class TimedChunk extends Chunk implements Serializable {
    private String startTime;
    private String endTime;
    private int partNumber;

    public TimedChunk(String startTime, String endTime, int partNumber, String text) {
        super(text);
        this.startTime = startTime;
        this.endTime = endTime;
        this.partNumber = partNumber;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
    }
}
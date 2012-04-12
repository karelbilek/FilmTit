package cz.filmtit.share;

import java.io.Serializable;
import java.util.*;


public class Chunk implements Serializable {
    public String startTime;
    public String endTime;
    public String text;
    public String userTranslation;
    public int partNumber;
    public boolean done;
    public ArrayList<Match> matches;

    public String getKey() {
        return startTime + "#" + Integer.toString(partNumber);
    }
    
    public int hashCode() {
        return (getKey()).hashCode();
    }
    
    public boolean equals(Object obj) {
        // can be compared just with another chunk
        if (obj.getClass() != this.getClass()) { return false; }
        Chunk other = (Chunk)obj;
        return (this.startTime.equals(other.startTime) &&
                this.partNumber == other.partNumber);
    }
}

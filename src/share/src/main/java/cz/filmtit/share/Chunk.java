package cz.filmtit.share;

import java.util.ArrayList;
import java.util.List;

public class Chunk {
    public String startTime;
    public String endTime;
    public String text;
    public String userTranslation;
    public int partNumber;
    public boolean done;
    
    public List<Match> matches;
    
    public Chunk(String text) {
    	this.text = text;
    	matches = new ArrayList<Match>();
    }
    
    public Chunk() {
    	matches = new ArrayList<Match>();
    }
}

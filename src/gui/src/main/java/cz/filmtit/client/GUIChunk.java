package cz.filmtit.client;


import java.util.*;

/**
 * Represents a subtitle chunk,
 * along with the list of corresponding matches.
 * (vaguely based on class Chunk from Jindra)
 * 
 * @author Honza Václ
 */

public class GUIChunk {

	private String startTime;
    private String endTime;
    private String text;
    private String userTranslation;
    private boolean done;
    private int partNumber;
    private List<GUIMatch> matches;
    
    public static GUIChunk NoNextSubtitle = new GUIChunk("< no next subtitle >", null);
    
    public GUIChunk(String text, List<GUIMatch> matches) {
    	this.text = text;
    	this.matches = matches;
    }


    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        // TODO: check the timing format
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        // TODO: check the timing format
        this.endTime = endTime;
    }

    public String getChunkText() {
        return text;
    }

    public void setChunkText(String text) {
        this.text = text;
    }

    public String getUserTranslation() {
        return userTranslation;
    }

    public void setUserTranslation(String userTranslation) {
        this.userTranslation = userTranslation;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public int getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
    }
    
    public GUIMatch getMatchAt(int index) {
    	return matches.get(index);
    }

    public List<GUIMatch> getMatches() {
    	return matches;
    }
    
    public List<String> getMatchesAsStrings() {
    	List<String> matchstrings = new ArrayList<String>();
    	ListIterator<GUIMatch> li = matches.listIterator();
    	while (li.hasNext()) {
    		matchstrings.add(li.next().getMatchText());
    	}
    	return matchstrings;
    }

}

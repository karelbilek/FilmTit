package cz.filmtit.share;

import java.util.List;
import java.util.ArrayList;

public class Match {
	public String text;
	
    public List<Translation> translations;

    public Match() {
    	translations = new ArrayList<Translation>();
    }
    
    public Match(String text) {
    	this.text = text;
    	translations = new ArrayList<Translation>();
    }
}

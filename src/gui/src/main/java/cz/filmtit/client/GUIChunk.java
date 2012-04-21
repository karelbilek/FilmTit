package cz.filmtit.client;

import java.util.*;

import cz.filmtit.share.Chunk;
import cz.filmtit.share.Match;





/**
 * Represents a subtitle chunk,
 * along with the list of corresponding matches.
 * Serves as a "GUI-wrapper" for the shared class Chunk.
 * 
 * @author Honza Václ
 */

public class GUIChunk {

	private Chunk chunk;
	private List<GUIMatch> matches; 

	public static GUIChunk NoNextSubtitle = new GUIChunk("< no next subtitle >");


	public GUIChunk(Chunk sharedchunk) {
		this.chunk = sharedchunk;
		this.matches = new ArrayList<GUIMatch>();
		ListIterator<Match> sharedchunkiterator = sharedchunk.matches.listIterator();
		while (sharedchunkiterator.hasNext()) {
			this.matches.add( new GUIMatch(sharedchunkiterator.next()) );
		}
	}
	
	public GUIChunk(String text) {
		this.chunk = new Chunk(text);
		this.chunk.text = text;
		this.matches = new ArrayList<GUIMatch>();
	}
	
	public GUIChunk(String text, String startTime, String endTime, int partNumber) {
		this.chunk = new Chunk(text);
		this.chunk.text = text;
		this.chunk.startTime = startTime;
		this.chunk.endTime = endTime;
		this.chunk.partNumber = partNumber;
		this.matches = new ArrayList<GUIMatch>();
	}


	/**
	 * Creates a string from the chunk text and its attributes,
	 * visually delimiting them. 
	 */
	@Override
	public String toString() {
		return  ( this.chunk.startTime + " ::: "
				+ this.chunk.endTime   + " ::: "
				+ "(((" + this.chunk.partNumber + ")))" + " ::: "
				+ this.chunk.text
			    );
	}

	public String getStartTime() {
		return this.chunk.startTime;
	}

	public String getEndTime() {
		return this.chunk.endTime;
	}

	public String getChunkText() {
		return this.chunk.text;
	}

	public void setChunkText(String text) {
		this.chunk.text = text;
	}

	public String getUserTranslation() {
		return chunk.userTranslation;
	}

	public void setUserTranslation(String userTranslation) {
		chunk.userTranslation = userTranslation;
	}

	public boolean isDone() {
		return chunk.done;
	}

	public void setDone(boolean done) {
		chunk.done = done;
	}

	public int getPartNumber() {
		return chunk.partNumber;
	}
	
	public void setTimes(String startTime, String endTime) {
		chunk.startTime = startTime;
		chunk.endTime = endTime;
	}

	public void setPartNumber(int partNumber) {
		chunk.partNumber = partNumber;
	}

	public GUIMatch getMatchAt(int index) {
		return this.matches.get(index);
	}

	public List<GUIMatch> getMatches() {
		return this.matches;
	}

	/*
	public List<String> getMatchesAsStrings() {
		List<String> matchstrings = new ArrayList<String>();
		ListIterator<Match> li = chunk.matches.listIterator();
		while (li.hasNext()) {
			matchstrings.add(li.next().text);
		}
		return matchstrings;
	}
	*/
}

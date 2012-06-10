package cz.filmtit.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.regexp.shared.*;

import cz.filmtit.share.TimedChunk;

/**
 * Provides a simple parsing function for reading SRT subtitle format
 * into a "shallow" GUISubtitleList (with empty matches and translations).
 * 
 * NOTE (TODO): At the moment, it can correctly parse only strings not beginning
 * with an empty line and with no more than one consecutive empty lines.
 * 
 * TODO: add parsing of text format modifiers like "<i>" - or at least deleting them
 * 
 * @author Honza Václ
 *
 */
public class ParserSrt extends Parser {
	
	public static RegExp reNumberLine = RegExp.compile("^[0-9]+$");
	public static RegExp reTimesLine  = RegExp.compile("^[0-9][0-9]:[0-9][0-9]");
	public static String TIMES_SEPARATOR = " --> ";
	
    @Override
	protected List<TimedChunk> breakToChunks(String text) {
		List<TimedChunk> result = new ArrayList<TimedChunk>();
		
		String[] lines = text.split(LINE_SEPARATOR);
		
		//int number = 0;  // in-file numbering of the subtitle - not used at the moment
		String startTime = EMPTY_STRING;
		String endTime = EMPTY_STRING;
		String titText = EMPTY_STRING;		
		int chunkId = 0;

		for (int linenumber = 0; linenumber < lines.length; linenumber++) {
			String line = lines[linenumber];
			
			//if ( line.matches("[0-9]+") ) {
			if ( reNumberLine.test(line) ) {
				// in-file numbering of the subtitle - not used at the moment
				//number = Integer.parseInt(line);
			}
			//else if ( line.matches("[0-9][0-9]:[0-9][0-9].*")) {
			else if ( reTimesLine.test(line)) {
				// line with times
				String[] times = line.split(TIMES_SEPARATOR);
				startTime = times[0];
				endTime = times[1];
			}
			else if ( ! line.isEmpty() ) {
				if (! titText.isEmpty()) {
					titText += SUBLINE_SEPARATOR_OUT;
				}
			    titText += line;
           	}
			else {
                // empty line
				// creating the chunk(s) from what was gathered recently...

//    public TimedChunk(String startTime, String endTime, int partNumber, String text, int id, long documentId) {
                 result.add(new TimedChunk(startTime, endTime,0, titText, chunkId++, 0));
                //addToSublist(sublist, titText, startTime, endTime, chunkId++, documentId);
				
		    	// ...and resetting
				titText = EMPTY_STRING;
			}
		}  // for-loop over lines
        
        
		// adding the last tit (after it, there was no empty line from splitting):
        //addToSublist(sublist, titText, startTime, endTime, chunkId++, documentId);
        result.add(new TimedChunk(startTime, endTime,0, titText, chunkId++, 0));

        	
		return result;
	}
	
}

package cz.filmtit.client;

import com.google.gwt.regexp.shared.*;

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
public class ParserSrt implements Parser {
	
	public static RegExp reNumberLine    = RegExp.compile("^[0-9]+$");
	public static RegExp reTimesLine     = RegExp.compile("^[0-9][0-9]:[0-9][0-9]");
	public static String TIMES_SEPARATOR = " --> ";
	
	
	public GUISubtitleList parse(String text) {
		GUISubtitleList sublist = new GUISubtitleList();
		
		String[] lines = text.split(LINE_SEPARATOR);
		
		//int number = 0;  // in-file numbering of the subtitle - not used at the moment
		String startTime = EMPTY_STRING;
		String endTime = EMPTY_STRING;
		int partNumber = 1;
		String chunkText = EMPTY_STRING;		

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
				if (chunkText.isEmpty()) {
					chunkText = line;
					partNumber = 1;
				}
				else {  // chunkText is not empty
					//if (  chunkText.matches("^ ?- .*")
					//		&& line.matches("^ ?- .*") ) {
					if (  reDialogSegment.test(chunkText)
							&& reDialogSegment.test(line) ) {
						// "dialogue" lines - switching speakers
						// -> splitting into two (or more) c-hunks
						//    - 1st one is the chunkText so far, 2nd one (starts on) the current line
						sublist.addSubtitleChunk( new GUIChunk(chunkText, startTime, endTime, partNumber) );
						
						chunkText = line;
						partNumber++;
					}
					else {
						// continuation of one subtitle (one speaker) only divided into more lines
						chunkText += SUBLINE_SEPARATOR_OUT + line;
					}
				}
			}
			else {  // empty line
				// creating the chunk from what was gathered recently...
				sublist.addSubtitleChunk( new GUIChunk(chunkText, startTime, endTime, partNumber) );
				
				// ...and resetting
				chunkText = EMPTY_STRING;
				partNumber = 1;
			}
		}  // for-loop over lines

		// adding the last chunk (after it, there was no empty line from splitting):
		sublist.addSubtitleChunk( new GUIChunk(chunkText, startTime, endTime, partNumber) );

		return sublist;
	}
	
}

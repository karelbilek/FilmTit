package cz.filmtit.share.parsing;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.regexp.shared.*;

import cz.filmtit.share.SrtTime;
import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.exceptions.InvalidValueException;
import cz.filmtit.share.exceptions.ParsingException;

/**
 * Provides a simple parsing function for reading SRT subtitle format
 * into a list of UnprocessedChunks.
 *
 * @author Honza Václ and Karel Bílek
 *
 * TODO: add parsing of text format modifiers like "&lt;i&gt;" 
 */
public class UnprocessedParserSrt extends UnprocessedParser {


    /**
     * Regexp for determining if line is number of subtitle.
     */
	public static RegExp reSubNumberLine = RegExp.compile("^[0-9]+$");

    /**
     * Regexp for determining if line is time line.
     */
	public static RegExp reTimesLine     = RegExp.compile("^(.*)\\s*-- ?>\\s*(.*)$");

	/**
     * Parses SRT text to unprocessed chunks.
     * @param text text in SRT format
     * @return list of unprocessed chunks.
     */
	public List<UnprocessedChunk> parseUnprocessed(String text)
            throws ParsingException {
		List<UnprocessedChunk> sublist = new ArrayList<UnprocessedChunk>();
		
		String[] lines;
        if (text.equals("")) {
            String[] lines2= {""};
            lines=lines2;
        } else {
            lines = text.split(LINE_SEPARATOR);
		}

		String startTime = EMPTY_STRING;
		String endTime = EMPTY_STRING;
		String titText = EMPTY_STRING;

        boolean isTimeSet = false;


        for (int linenumber = 0; linenumber < lines.length; linenumber++) {
            String line = lines[linenumber];
			
			if ( reTimesLine.test(line)) {
                MatchResult mr = reTimesLine.exec(line);
                startTime = mr.getGroup(1).trim();
                endTime = mr.getGroup(2).trim();
                isTimeSet = true;
                try { // testing the time format:
                    new SrtTime(startTime);
                    new SrtTime(endTime);
                } catch (InvalidValueException e) {
                    throw new ParsingException(e.getMessage(), linenumber + 1, false);
                }
            }
			else if ( ! line.isEmpty() ) {
                if (! isTimeSet) {
                    throw new ParsingException("Subtitle timing line missing or malformed", linenumber + 1, true);
                }
				if (! titText.isEmpty()) {
					titText += LINE_SEPARATOR_OUT;
				}
			    titText += line;
           	}
			else {
                // empty line
				// creating the chunk(s) from what was gathered recently...

                sublist.add(new UnprocessedChunk(startTime, endTime, titText));
				
		    	// ...and resetting
				titText = EMPTY_STRING;
                isTimeSet = false;
			}
		}  // for-loop over lines
        
        
		// adding the last tit (after it, there was no empty line from splitting):
        sublist.add(new UnprocessedChunk(startTime, endTime, titText));

		return sublist;
	}
	
}

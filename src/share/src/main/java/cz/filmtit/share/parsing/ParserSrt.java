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
 * @author Honza VĂˇcl and KB
 *
 * TODO: add parsing of text format modifiers like "<i>" - or at least deleting them
 * 
 * @author Honza Václ
 *
 */
public class ParserSrt extends Parser {
	
	public static RegExp reSubNumberLine = RegExp.compile("^[0-9]+$");
	public static RegExp reTimesLine     = RegExp.compile("^(.*)\\s*-- ?>\\s*(.*)$");

	
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

		//int subNumber = 0;  // in-file numbering of the subtitle - not used at the moment
		String startTime = EMPTY_STRING;
		String endTime = EMPTY_STRING;
		String titText = EMPTY_STRING;

        //boolean isSubNumberSet = false;
        boolean isTimeSet = false;
        //boolean isTitTextSet = false;


        for (int linenumber = 0; linenumber < lines.length; linenumber++) {
            String line = lines[linenumber];
			
			if ( reSubNumberLine.test(line) ) {
				// in-file numbering of the subtitle - not used at the moment
				//subNumber = Integer.parseInt(line);
                //isSubNumberSet = true;
			}
			else if ( reTimesLine.test(line)) {
				// line with times
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
                //isTitTextSet = true;
				if (! titText.isEmpty()) {
					titText += SUBLINE_SEPARATOR_OUT;
				}
			    titText += line;
           	}
			else {
                // empty line
				// creating the chunk(s) from what was gathered recently...

                sublist.add(new UnprocessedChunk(startTime, endTime, titText));
                //addToSublist(sublist, titText, startTime, endTime, chunkId++, documentId);
				
		    	// ...and resetting
				titText = EMPTY_STRING;
                //isSubNumberSet = false;
                isTimeSet = false;
                //isTitTextSet = false;
			}
		}  // for-loop over lines
        
        
		// adding the last tit (after it, there was no empty line from splitting):
        sublist.add(new UnprocessedChunk(startTime, endTime, titText));
        //addToSublist(sublist, titText, startTime, endTime, chunkId++, documentId);

        //renumber(sublist);	
		return sublist;
	}
	
}

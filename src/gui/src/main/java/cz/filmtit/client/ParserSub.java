package cz.filmtit.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.regexp.shared.*;

import cz.filmtit.share.TimedChunk;

/**
 * Provides a simple parsing function for reading SUB subtitle format
 * into a "shallow" GUISubtitleList (with empty matches and translations).
 * 
 * TODO add parsing of text format modifiers like "{Y:i}"
 * 
 * @author Honza Václ
 *
 */
public class ParserSub implements Parser {
	
	public static RegExp reSubtitleLine  = RegExp.compile("^{([0-9]+)}{([0-9]+)}(.*)$");  // the "{}" are here as literals
	

	public List<TimedChunk> parse(String text, long documentId) {
		List<TimedChunk> sublist = new ArrayList<TimedChunk>();
		
		String[] lines = text.split(LINE_SEPARATOR);
		int chunkId = 0;
		
		for (int linenumber = 0; linenumber < lines.length; linenumber++) {
			String line = lines[linenumber];

			if (reSubtitleLine.test(line)) {
				MatchResult matcher = reSubtitleLine.exec(line);
				// note: the 0th group is the whole line (the whole match)
				String startTime = matcher.getGroup(1);
				String endTime   = matcher.getGroup(2);
				String lineText  = matcher.getGroup(3);
				int partNumber = 1;
				
				String[] segments = lineText.split(SUBLINE_SEPARATOR_IN);
				String chunkText = segments[0];
				for (int i = 1; i < segments.length; i++) {
					if ( reDialogSegment.test(chunkText)
							&& reDialogSegment.test(segments[i]) ) {
						sublist.add( new TimedChunk(startTime, endTime, partNumber, chunkText, chunkId++, documentId) );
						chunkText = segments[i];
						partNumber++;
					}
					else {
						chunkText += SUBLINE_SEPARATOR_OUT + segments[i];
					}
				}
				sublist.add( new TimedChunk(startTime, endTime, partNumber, chunkText, chunkId++, documentId) );
			}
			else {
				// wrong format of this line
				//throw new TODO SubFileFormatException(linenumber);
			}
		}

		return sublist;
	}
	
}

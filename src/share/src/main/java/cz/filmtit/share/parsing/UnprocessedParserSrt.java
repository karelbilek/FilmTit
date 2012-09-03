/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/

package cz.filmtit.share.parsing;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.regexp.shared.*;

import cz.filmtit.share.SrtTime;
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


    private static final int MAX_ERRORS_PER_FILE = 10;
    private int currentErrors = 0;
    
    /**
     * We can silently ignore few format exceptions, if the file is not completely wrong.
     * Sometimes, the file format is just slightly wrong, but we don't need to
     * scrap the whole thing because of one wrong time declaration.
     */
    private void maybeThrow(ParsingException e) throws ParsingException {
        if (currentErrors < MAX_ERRORS_PER_FILE) {
            currentErrors++;
        } else {
            throw e;
        }
    }

    /**
     * Regexp for determining if line is number of subtitle.
     */
	public static RegExp reSubNumberLine = RegExp.compile("^\\s*[0-9]+\\s*$");

    /**
     * Regexp for determining if line is time line.
     */
	public static RegExp reTimesLine     = RegExp.compile("^(.*)\\s*-- ?>\\s*([^X]*).*$");

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
		
            //skip the number lines
            if (!reSubNumberLine.test(line) ) {
                if ( reTimesLine.test(line)) {
                    MatchResult mr = reTimesLine.exec(line);
                    String maybeStartTime = mr.getGroup(1).trim();
                    String maybeEndTime = mr.getGroup(2).trim();
                    boolean isException=false;
                    isTimeSet = true;
                    try { // testing the time format:
                        new SrtTime(maybeStartTime);
                        new SrtTime(maybeEndTime);
                    } catch (InvalidValueException e) {
                        System.out.println("chyba pri : start = "+maybeStartTime+", end = "+maybeEndTime);
                        maybeThrow(new ParsingException(e.getMessage(), linenumber + 1, false));
                        isException = true;
                    }
                    if (!isException) {
                        startTime = maybeStartTime;
                        endTime = maybeEndTime;
                    } else {
                        //if we have been ignoring the exception:
                        startTime = SrtTime.ZERO_TIME;
                        endTime = SrtTime.ZERO_TIME;
                    }
                }
                else if ( ! line.isEmpty() ) {
                    if (! isTimeSet) {
                        maybeThrow(new ParsingException("Subtitle timing line missing or malformed", linenumber + 1, true));
                        //if we have been ignoring the exception:
                        startTime = SrtTime.ZERO_TIME;
                        endTime = SrtTime.ZERO_TIME;
                    }
                    if (! titText.isEmpty()) {
                        titText += LINE_SEPARATOR_OUT;
                    }
                    titText += line;
                }
                else {
                    // empty line
                    // creating the chunk(s) from what was gathered recently...
                    if (! titText.equals(EMPTY_STRING)) {
                        sublist.add(new UnprocessedChunk(startTime, endTime, titText));
                    }
                    
                    // ...and resetting
                    titText = EMPTY_STRING;
                    isTimeSet = false;
                }
            }
		}  // for-loop over lines
        


		// adding the last tit (after it, there was no empty line from splitting):
        if (! titText.equals(EMPTY_STRING)) {
            sublist.add(new UnprocessedChunk(startTime, endTime, titText));
        }

        // check whether at least one of the parsed subtitles is correct
        boolean ok = false;
        for (UnprocessedChunk chunk : sublist) {
            if (! chunk.getEndTime().equals(SrtTime.ZERO_TIME)) {
                ok = true;
            }
        }
        if (!ok) {
            throw new ParsingException("Wrong format of the whole subtitle file", 0, false);
        }

		return sublist;
	}
	
}

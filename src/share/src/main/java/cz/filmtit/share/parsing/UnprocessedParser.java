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

import java.util.List;
import cz.filmtit.share.exceptions.ParsingException;

/**
 * Interface for parsing a subtitle file,
 * intended primarily as a base class
 * for UnprocessedParserSrt (parsing .srt files)
 * and UnprocessedParserSub (.sub files)
 *
 * 
 * @author Honza Václ, Karel Bílek
 *
 */
public abstract class UnprocessedParser {
	//============STRING STATIC OBJECTS=========
	/**
     * How newline is preserved after parseUnprocessed
     */
    public static final String LINE_SEPARATOR_OUT = " | ";

	/**
     * Empty string.
     */
    public static final String EMPTY_STRING = "";

    /**
     * Newline regexp, that catches both win and unix newlines.
     */
    public static final String LINE_SEPARATOR  = "\r?\n";

    /**
     * Abstract method, that parses the subtitle file. It is not dealing with opening the file.
     *
     * @param text Raw text of the subtitle file
     * @return List of unprocessed subtitle items - the most basic info we can get.
     * @see UnprocessedChunk
     */
    public abstract List<UnprocessedChunk> parseUnprocessed(String text) throws ParsingException;
 

}


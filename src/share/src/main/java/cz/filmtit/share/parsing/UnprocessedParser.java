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


package cz.filmtit.share.parsing;

import java.util.List;
import cz.filmtit.share.exceptions.ParsingException;

public abstract class UnprocessedParser {
	//============STRING STATIC OBJECTS=========
	//LINE_SEPARATOR_OUT is how newline is preserved after parseUnprocessed
    public static final String LINE_SEPARATOR_OUT = " | ";

    //empty string is.... empty string.
	public static final String EMPTY_STRING = "";

    //LINE_SEPARATOR is newline that catches both win and unix newlines
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


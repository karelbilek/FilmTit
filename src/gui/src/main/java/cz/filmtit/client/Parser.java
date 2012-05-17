package cz.filmtit.client;

import java.util.List;

import com.google.gwt.regexp.shared.RegExp;

import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.TitChunkSeparator;

/**
 * Interface for parsing a subtitle file,
 * intended primarily as a base class
 * for ParserSub (parsing .sub files)
 * and ParserSrt (.srt files)
 * 
 * @author Honza Václ
 *
 */
public abstract class Parser {
	public static final String SUBLINE_SEPARATOR_IN = "\\|";
	public static final String SUBLINE_SEPARATOR_OUT = " | ";
	public static final String EMPTY_STRING = "";
	public static final String LINE_SEPARATOR  = "\r?\n";

	public abstract List<TimedChunk> parse(String text, long documentId);

    public static void addToSublist(List<TimedChunk> sublist, String titText, String startTime, String endTime, int chunkId, long documentId) {
        int partNumber = 1;

        for (String chunkText : TitChunkSeparator.separate(titText)) {
            sublist.add( new TimedChunk(startTime, endTime, partNumber, chunkText, chunkId, documentId) );
            partNumber++;
        }
    }
}

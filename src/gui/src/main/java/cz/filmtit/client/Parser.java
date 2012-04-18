package cz.filmtit.client;

import com.google.gwt.regexp.shared.RegExp;

/**
 * Interface for parsing a subtitle file,
 * intended primarily as a base class
 * for ParserSub (parsing .sub files)
 * and ParserSrt (.srt files)
 * 
 * @author Honza Václ
 *
 */
public interface Parser {
	public static final String SUBLINE_SEPARATOR_IN = "\\|";
	public static final String SUBLINE_SEPARATOR_OUT = " | ";
	public static final String EMPTY_STRING = "";
	public static final String LINE_SEPARATOR  = "\r?\n";
	
	public static final RegExp reDialogSegment = RegExp.compile(" ?- ");
	
	public abstract GUISubtitleList parse(String text);
}

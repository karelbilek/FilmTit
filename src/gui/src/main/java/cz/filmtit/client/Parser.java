package cz.filmtit.client;

import java.util.ArrayList;
import java.util.List;

import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.TitChunkSeparator;
import cz.filmtit.share.Language;
import cz.filmtit.share.annotations.*;

import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.SplitResult;


/**
 * Interface for parsing a subtitle file,
 * intended primarily as a base class
 * for ParserSrt (parsing .srt files)
 * and ParserSub (.sub files)
 * 
 * @author Honza Václ
 *
 */
public abstract class Parser {
	public static final String SUBLINE_SEPARATOR_IN = "\\|";
	public static final String SUBLINE_SEPARATOR_OUT = " | ";
	public static final String EMPTY_STRING = "";
	public static final String LINE_SEPARATOR  = "\r?\n";
	
	public static final String SUBLINE_SEPARATOR_OUT_REGEXP = "( |^)\\|( |$)";
	public static final RegExp dialogueMatch = RegExp.compile("^ ?- ");

    public static void renumber (List<TimedChunk> what) {
        int i = 0;
        for (TimedChunk chunk:what) {
            chunk.setIndex(i);
            i++;
        }
    }
	public abstract List<TimedChunk> parse(String text, long documentId);

    public static void addToSublist(List<TimedChunk> sublist, String titText, String startTime, String endTime, int chunkId, long documentId) {
        List<String> separatedText = TitChunkSeparator.separate(titText, Language.EN);
    	int partNumber = 1;
        for (String chunkText : separatedText) {
            List<Annotation> annotations = new ArrayList<Annotation>();
            
            if (dialogueMatch.test(chunkText)) {
                   chunkText = dialogueMatch.replace(chunkText, "");
                   annotations.add(new Annotation(AnnotationType.DIALOGUE, 0, 0));
            }


            RegExp sublineRegexp = RegExp.compile(SUBLINE_SEPARATOR_OUT_REGEXP, "g");
            
            MatchResult sublineResult = sublineRegexp.exec(chunkText);
            
            while (sublineResult != null) {
                int index = sublineResult.getIndex();
                
                //not sure about off-by-one errors
                chunkText = chunkText.substring(0, index) + chunkText.substring(index+3, chunkText.length());
                if (index != 0) { 
                    annotations.add(new Annotation(AnnotationType.LINEBREAK, index, index));
                }
                sublineResult = sublineRegexp.exec(chunkText);
            }

            TimedChunk newChunk = new TimedChunk(startTime, endTime, partNumber, chunkText, chunkId, documentId); 
            newChunk.addAnnotations(annotations);            
            
            sublist.add( newChunk);
            partNumber++;
        }
    }
}

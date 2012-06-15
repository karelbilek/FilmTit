package cz.filmtit.share.parsing;

import java.util.LinkedList;
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
	
    public abstract List<UnprocessedChunk> parseUnprocessed(String text);

	public List<TimedChunk> parse(String text, long documentId) {
        return processChunks(parseUnprocessed(text), documentId);
    }

    public static List<TimedChunk> processChunks(List<UnprocessedChunk> chunks, long documentId) {
        LinkedList<TimedChunk> result = new LinkedList<TimedChunk>();
        int chunkId = 0;
        for (UnprocessedChunk chunk: chunks){
            result.addAll(processChunk(chunk, chunkId, documentId));
            chunkId++;
        }
        renumber(result);
        return result;
    }
    
    public static void renumber (List<TimedChunk> what) {
        int i = 0;
        for (TimedChunk chunk:what) {
            chunk.setIndex(i);
            i++;
        }
    }
    
    public static LinkedList<TimedChunk> processChunk(UnprocessedChunk chunk, int chunkId, long documentId) {
        LinkedList<TimedChunk> result = new LinkedList<TimedChunk>();
        
        //separate into sentences
        List<String> separatedText = TitChunkSeparator.separate(chunk.getText(), Language.EN);
    	int partNumber = 1;

        for (String chunkText : separatedText) {
            List<Annotation> annotations = new ArrayList<Annotation>();
            
            //if it is a dialogue, mark it as such in annotations
            if (dialogueMatch.test(chunkText)) {
                   chunkText = dialogueMatch.replace(chunkText, "");
                   annotations.add(new Annotation(AnnotationType.DIALOGUE, 0, 0));
            }

            //add linebreaks as annotations
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

            //create a new timedchunk
            TimedChunk newChunk = new TimedChunk(chunk.getStartTime(), chunk.getEndTime(), partNumber, chunkText, chunkId, documentId); 
            newChunk.addAnnotations(annotations);            
            
            result.add( newChunk);
            partNumber++;
        }
        return result;
    }
}

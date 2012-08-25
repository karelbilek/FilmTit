package cz.filmtit.share.parsing;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;

import cz.filmtit.share.Chunk;
import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.Language;
import cz.filmtit.share.annotations.*;

import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.SplitResult;
import cz.filmtit.share.exceptions.ParsingException;


/**
 * Interface for parsing a subtitle file,
 * intended primarily as a base class
 * for ParserSrt (parsing .srt files)
 * and ParserSub (.sub files)
 * 
 * @author Honza Václ
 *
 */
public class Parser {
    //LINE_SEPARATOR_OUT_REGEXP catches LINE_SEPARATOR_OUT
    //it is not RegExp object, because RegExp object somehow keeps internal counts
    //that we use while adding newlines as Annotations 
    public static final String LINE_SEPARATOR_OUT_REGEXP = "( |^)\\|( |$)";
	
    //==========REGEXP STATIC OBJECTS=========== 
	//matching a dialogue on the beginning in a beginning
    public static final RegExp dialogueMatch = RegExp.compile("^ ?-+ ?");
    //matching a new line on the beginning
    public static final RegExp lineAtBeginMatch = RegExp.compile("^\\|");

    //ignore all HTML-like tags
	public static final RegExp formatMatch = RegExp.compile("<[^>]*>", "g");
	
    //more spaces => single space
    public static final RegExp spacesMatch = RegExp.compile("\\s+", "g");
	
    //========================================

    private UnprocessedParser unprocessedParser;
    public Parser(UnprocessedParser unprocessedParser) {
        this.unprocessedParser = unprocessedParser;
    }
    
    public static Parser PARSER_SUB = new Parser(new UnprocessedParserSub());
    public static Parser PARSER_SRT = new Parser(new UnprocessedParserSrt());

	public List<TimedChunk> parse(String text, long documentId, Language l)
            throws ParsingException {
        return processChunks(unprocessedParser.parseUnprocessed(text), documentId, l);
    }

    public static List<TimedChunk> processChunks(List<UnprocessedChunk> chunks, long documentId, Language l) {
        LinkedList<TimedChunk> result = new LinkedList<TimedChunk>();
        int chunkId = 0;
        for (UnprocessedChunk chunk: chunks){
            result.addAll(processChunk(chunk, chunkId, documentId, l));
            chunkId++;
        }
        return result;
    }

    public static Chunk getChunkFromText(String chunkText) {
            chunkText = formatMatch.replace(chunkText, "");
            chunkText = spacesMatch.replace(chunkText, " ");
        
            List<Annotation> annotations = new ArrayList<Annotation>();
            
            //if it is a dialogue, mark it as such in annotations
            if (dialogueMatch.test(chunkText)) {
                   chunkText = dialogueMatch.replace(chunkText, "");
                   annotations.add(new Annotation(AnnotationType.DIALOGUE, 0, 0));
            }

            //add linebreaks as annotations
            RegExp sublineRegexp = RegExp.compile(LINE_SEPARATOR_OUT_REGEXP, "g");
            MatchResult sublineResult = sublineRegexp.exec(chunkText);            
            while (sublineResult != null) {
                int index = sublineResult.getIndex();
                 
                //not sure about off-by-one errors
                String newChunkText = chunkText.substring(0, index);
                
                if (index+3 < chunkText.length()) {
                    int biggerIndex = lineAtBeginMatch.test(chunkText) ? 2 : (index+3);
                    newChunkText = newChunkText + " "+chunkText.substring(biggerIndex, chunkText.length());
                }

                chunkText = newChunkText;
                
                if (index != 0) { 
                    annotations.add(new Annotation(AnnotationType.LINEBREAK, index, index));
                }
                sublineResult = sublineRegexp.exec(chunkText);
            }

            Chunk ch = new Chunk(chunkText, annotations);
            return ch;
    }
   
    public static LinkedList<TimedChunk> processChunk(UnprocessedChunk chunk, int chunkId, long documentId, Language l) {
        

        LinkedList<TimedChunk> result = new LinkedList<TimedChunk>();
        
        //separate into sentences
        List<String> separatedText = TitChunkSeparator.separate(chunk.getText(), l);
    	int partNumber = 1;

        for (String chunkText : separatedText) {
             
            Chunk untimedChunk = getChunkFromText(chunkText);

            //create a new timedchunk
            TimedChunk newChunk = new TimedChunk(chunk.getStartTime(), chunk.getEndTime(), partNumber, untimedChunk.getSurfaceForm(), chunkId, documentId); 
            newChunk.addAnnotations(untimedChunk.getAnnotations());            
            
            result.add( newChunk);
            partNumber++;
        }
        return result;
    }
}

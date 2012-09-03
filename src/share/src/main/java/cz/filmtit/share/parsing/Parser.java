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
 *
 * Parser of file into list of TimedChunks.
 * The type of parsing is dependent only on UnprocessedParser,
 * because there are only two of UnprocessedParsers and both of them are 
 * singletons, there is need only for two instances of Parser
 *
 * @author Honza Václ, Karel Bílek
 *
 */
public class Parser {


    /**
     * Catches LINE_SEPARATOR_OUT from UnprocessedParser.
     * It is not RegExp object, because RegExp object somehow keeps internal counts
     * that we use while adding newlines as Annotations, so we will have to create
     * RegExp object later.
     */
     static final String LINE_SEPARATOR_OUT_REGEXP = "( |^)\\|( |$)";
	
    //==========REGEXP STATIC OBJECTS=========== 
    /**
     * Matches a dialogue on the beginning of line
     */
    static final RegExp dialogueMatch = RegExp.compile("^ ?-+ ?");

    /**
     * Matches a new line at the very beginning of string.
     */
    static final RegExp lineAtBeginMatch = RegExp.compile("^\\|");

    /**
     * RegExp for recognizing (and then, ignoring) all HTML-like tags
     */
	static final RegExp formatMatch = RegExp.compile("<[^>]*>", "g");
	
    //more spaces => single space
    static final RegExp spacesMatch = RegExp.compile("\\s+", "g");
	
    //========================================

    /**
     * UnprocessedParser to get unprocessed chunks from text.
     */
    private UnprocessedParser unprocessedParser;

    /**
     * Constructor. It is private, because only 2 instances of Parser exist - PARSER_SUB and PARSER_SRT.
     * The only difference between them is the underlying UnprocessedParser.
     * @param unprocessedParser "raw" chunk parser
     */
    private Parser(UnprocessedParser unprocessedParser) {
        this.unprocessedParser = unprocessedParser;
    }
    
    /**
     * Parser for SUB files.
     */
    public static Parser PARSER_SUB = new Parser(new UnprocessedParserSub());
    
    /**
     * Parser for SRT files.
     */
    public static Parser PARSER_SRT = new Parser(new UnprocessedParserSrt());

    /**
     * Parses given text.
     * @param text Given text, as string.
     * @param documentId ID of the document, which is saved in the TimedChunk object
     * @param l language of the subtitle
     * @return list of TimedChunks
     */
	public List<TimedChunk> parse(String text, long documentId, Language l)
            throws ParsingException {
        return processChunks(unprocessedParser.parseUnprocessed(text), documentId, l);
    }

    /**
     * Makes TimedChunk out of unprocessedChunks.
     * @param chunks unprocessed chunks to process
     * @param documentId ID of the document, which is saved in the resulting TimedChunk objects
     * @param l language of the subtitle
     * @return list of TimedChunks
     */
    public static List<TimedChunk> processChunks(List<UnprocessedChunk> chunks, long documentId, Language l) {
        LinkedList<TimedChunk> result = new LinkedList<TimedChunk>();
        int chunkId = 0;
        for (UnprocessedChunk chunk: chunks){
            result.addAll(processChunk(chunk, chunkId, documentId, l));
            chunkId++;
        }
        return result;
    }


    /**
     * Takes chunk, separates it to sentences and converts those to TimedChunks.
     * @param chunk unprocessed chunk, might contain zero to N sentences
     * @param chunkId ID of resulting chunk (it depends on the order in the file which we don't know at this point)
     * @param documentId ID of document, that is saved in the resulting TimedChunks
     * @param l language of the subtitle
     * @return list of chunks. It is list, because it might contain more sentences.
     */
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

    /**
     * Get chunk with proper annotations from source text.
     * @param chunkText text of the chunk with "-" and newlines (marked as | ).
     * @return Chunk with surfaceForm without "-" and newlines, and with proper annotations. 
     */
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
            RegExp lineRegexp = RegExp.compile(LINE_SEPARATOR_OUT_REGEXP, "g");
            MatchResult lineResult = lineRegexp.exec(chunkText);
            while (lineResult != null) {
                int index = lineResult.getIndex();
                 
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
                lineResult = lineRegexp.exec(chunkText);
            }

            Chunk ch = new Chunk(chunkText, annotations);
            return ch;
    }
   

}

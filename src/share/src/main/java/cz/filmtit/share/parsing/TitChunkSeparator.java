package cz.filmtit.share.parsing;

import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.regexp.shared.SplitResult;

import java.util.ArrayList;
import java.util.List;
import cz.filmtit.share.tokenizers.*;
import cz.filmtit.share.*;


/**
 * TitChunkSeparator is a singleton for splitting chunks into sentences.
 * It uses both SentenceTokenizers, but also some pre-defined rules.
 *
 * @author Honza Václ, Karel Bílek
 *
 */
public class TitChunkSeparator {
    
    /**
     * Czech sentence tokenizer.
     */
    public static SentenceTokenizer czechTokenizer = new CzechSentenceTokenizer();
    /**
     * English sentence tokenizer.
     */
    public static SentenceTokenizer englishTokenizer = new EnglishSentenceTokenizer();

    /**
     * String, separating lines.
     */
	public static final String LINE_SEPARATOR_OUT = " | ";

    /**
     * Regexp, splitting lines.
     */
	public static final RegExp lineSplitter = RegExp.compile(" *\\| *");
	
    /**
     * Regexp, splitting on "-" on the beginning of the line.
     */
    public static final RegExp dialogSegmenter = RegExp.compile("^ ?-+ ?.");

    /**
     * Regexp, splitting on ".?!" at the end of line.
     */
    public static final RegExp endOfLinePunct = RegExp.compile("[.?…!]\\s*$");

    /**
     * Regexp, removing formatting tags.
     * In srt, they are for example &lt;i&gt;, in sub they are for example "{Y:i}"
     */
	public static final RegExp formatTag = RegExp.compile("(<[^>]*>)|(\\{[^}]*\\})", "g");  // the "{}" are here as literals

    /**
     * Tokenizes string to sentences just using the tokenizer. It does NOT use our
     * additional rules.
     * @param tit Subtitle that will be tokenized. It can have "|" in the middle somewhere.
     * @param l language of the subtitle (currently only Czech and English are supported)
     * @return sentences. If the input has "|" (newline) somewhere, it WILL be preserved.
     */
    private static List<String> tokenizeByTokenizers(String tit, Language l) {
       
       if (l == Language.EN) {
            return englishTokenizer.tokenize(tit);
       } else {
            return czechTokenizer.tokenize(tit);
       }
    }

    /**
     * Separates longer string to sentences, using BOTH our own rules AND the rule-based tokenizers.
     * @param tit Subtitle that will be separated to sentences.
     * @param l language of the subtitle (currently only Czech and English are supported)
     * @return sentences.
     */
	public static List<String> separate(String tit, Language l) {
        
        // remove formatting tags
		tit = formatTag.replace(tit, "");

		SplitResult lines = lineSplitter.split(tit);

		List<String> resultChunks = new ArrayList<String>();
		String intermediateChunk = "";
        for (int i = 0; i < lines.length(); i++) {
			String line = lines.get(i);

			if (dialogSegmenter.test(line) && !intermediateChunk.equals("")) {
				// is a dialog line -> splitting
				resultChunks.addAll(tokenizeByTokenizers(intermediateChunk, l));
				intermediateChunk = line;
			}
			else {
                if (intermediateChunk.equals("")) {
                    intermediateChunk = line;
				} else {
                    intermediateChunk += LINE_SEPARATOR_OUT + line;
		        }

                //is punct at EOL -> splitting
                if (endOfLinePunct.test(intermediateChunk)) {
                    resultChunks.addAll(tokenizeByTokenizers(intermediateChunk,l));
                    
                    intermediateChunk="";
                }
            }
		}
		if (!intermediateChunk.equals("")) {
            resultChunks.addAll(tokenizeByTokenizers(intermediateChunk, l));
		}
		return resultChunks;
	}

}

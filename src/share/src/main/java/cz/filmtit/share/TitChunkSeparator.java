package cz.filmtit.share;

import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.regexp.shared.SplitResult;

import java.util.ArrayList;
import java.util.List;
import cz.filmtit.share.tokenizers.*;

public class TitChunkSeparator {

    public static CzechSentenceTokenizer czechTokenizer = new CzechSentenceTokenizer();
    public static EnglishSentenceTokenizer englishTokenizer = new EnglishSentenceTokenizer();

	public static final String SUBLINE_SEPARATOR_OUT = " | ";
	
	public static final RegExp indirectSplitter = RegExp.compile(" *\\| *");
	public static final RegExp dialogSegmenter = RegExp.compile("^ ?- ?.");
    public static final RegExp endOfLinePunct = RegExp.compile("[.?…!]$");

	// formatting tags - in srt e.g. "<i>", in sub e.g. "{Y:i}"
	public static final RegExp formatTag = RegExp.compile("(<[^>]*>)|(\\{[^}]*\\})", "g");  // the "{}" are here as literals

    private static List<String> tokenizeByTokenizers(String tit, Language l) {
       
       if (l == Language.EN) {
            return englishTokenizer.tokenize(tit);
       } else {
            return czechTokenizer.tokenize(tit);
       }
    }


	public static List<String> separate(String tit, Language l) {
        
        // remove formatting tags
		tit = formatTag.replace(tit, "");

		SplitResult lines = indirectSplitter.split(tit);

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
                    intermediateChunk += SUBLINE_SEPARATOR_OUT + line;
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

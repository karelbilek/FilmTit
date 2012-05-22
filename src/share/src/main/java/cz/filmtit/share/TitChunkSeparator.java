package cz.filmtit.share;

import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.regexp.shared.SplitResult;
import com.google.gwt.regexp.shared.MatchResult;

import java.util.ArrayList;
import java.util.List;

public class TitChunkSeparator {

	public static final String SUBLINE_SEPARATOR_OUT = " | ";
	
   //splitting on dialogues
    static final RegExp dialogSegmenter = RegExp.compile("(^|\\|) ?- ?");
	
	// formatting tags - in srt e.g. "<i>", in sub e.g. "{Y:i}"
	static final RegExp formatTag = RegExp.compile("(<[^>]*>)|(\\{[^}]*\\})", "g");  // the "{}" are here as literals

    public static List<String> separate(String tit) {
        return separate(tit, false);
    }

	public static List<String> separate(String tit, boolean splitBySentences) {
		// remove formatting tags
		tit = formatTag.replace(tit, "");

        
		SplitResult sentencesSplitByDialogue = dialogSegmenter.split(tit);

		ArrayList<String> resultChunks = new ArrayList<String>();
		for (int i = 0; i < sentencesSplitByDialogue.length(); i++) {
			String sentenceSplitByDialogue = sentencesSplitByDialogue.get(i);

            if (splitBySentences) {
                //more advanced splitting by sentences
                resultChunks.addAll(splitBySentences(sentenceSplitByDialogue));
                

            } else {
                resultChunks.add(sentenceSplitByDialogue);
            }
		}

		return clean(resultChunks);
	}

    //these are for splitting on diacritics
    //only if splitBySentences==true
	static final String diacriticsExceptDot = "\"!\\?";
    static final String diacriticsExceptDotSegmenter = "([^"+diacriticsExceptDot+"]+)(["+diacriticsExceptDot+"]+|$)";
    static final RegExp dotOnEndSegmenter = RegExp.compile("\\.\\|");

    public static ArrayList<String> splitBySentences(String tit) {
        RegExp diacriticsExceptDotR = RegExp.compile(diacriticsExceptDotSegmenter, "g");
		
        ArrayList<String> resultChunks = new ArrayList<String>();
        
        //separating by non-dot punctuation
        for (
            MatchResult sentenceMatchResult = diacriticsExceptDotR.exec(tit);
            sentenceMatchResult != null;
            sentenceMatchResult = diacriticsExceptDotR.exec(tit)) {
            

            String sentence = sentenceMatchResult.getGroup(1)
                                    +sentenceMatchResult.getGroup(2);
        
            //separating by dot at the end
            SplitResult sentenceDotSplitResult = 
                dotOnEndSegmenter.split(sentence);
            
            for (int i = 0; i < sentenceDotSplitResult.length(); i++) {
                
                String sentenceFinal = sentenceDotSplitResult.get(i);
                
                if (i != sentenceDotSplitResult.length()-1) {
                    sentenceFinal = sentenceFinal + ".";
                }
                resultChunks.add(sentenceFinal);
            }
        }

        return resultChunks;
    
    }
    
    static final RegExp endClean = RegExp.compile("\\s*$");
    static final RegExp startClean = RegExp.compile("^\\s*");
    public static ArrayList<String> clean(ArrayList<String> chunks) {
        ArrayList<String> resultChunks = new ArrayList<String>();
        for (String chunk: chunks) {
            chunk = endClean.replace(startClean.replace(chunk,""), "");
            if (chunk.length() > 0) {
                resultChunks.add(chunk);
            }
        }

        return resultChunks;
    }
}

package cz.filmtit.share;

import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.SplitResult;
import java.util.List;
import java.util.LinkedList;

public class TitChunkSeparator {
    
	public static final String sentenceSplitter = "([^!\"\\(\\)\\.:;\\?]+)([!\"\\(\\),\\.:;\\?]+|$)";
	
        //this will also get ordinary " - " , but I cannot use beginnings of lines
        //since the data are not available since I don't know which files Jindra used originally
    public static final RegExp indirectSplitter = RegExp.compile("( +-|- +)");
    public static final RegExp beginPad = RegExp.compile("^ *-?");
    public static final RegExp endingPad = RegExp.compile("\"? *$");


    public static final RegExp italic = RegExp.compile(" *<[^>]*> *", "g");
    
	//public static final RegExp sentenceSplitter = RegExp.compile("([!\"\\(\\),\\.:;\\?]| ?- )+");
    
    public static List<String> separate(String tit) {
        String tit_nonitalic = italic.replace(tit,"");

        SplitResult lines = indirectSplitter.split(tit_nonitalic);
        
        LinkedList<String> res = new LinkedList<String>();
        
        for (int i=0; i<lines.length(); i++) {
            String line = lines.get(i);

            RegExp sentenceSplitterR = RegExp.compile(sentenceSplitter, "g");
            
            MatchResult sentenceR = sentenceSplitterR.exec(line);

            while (sentenceR!=null) {
                String sentenceS = sentenceR.getGroup(1)+sentenceR.getGroup(2);
                sentenceS = beginPad.replace(sentenceS, "");
                sentenceS = endingPad.replace(sentenceS, "");
                if (sentenceS.length() > 0 ) {
                    res.add(sentenceS);
                }
                sentenceR = sentenceSplitterR.exec(line);
            }
        }
        return res;
    }

}

package cz.filmtit.share.tokenizers;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.regexp.shared.RegExp;

/**
 * Tokenizes text into sentences by looking for typical end-of-sentence markers,
 * but considering exceptions (eg abbreviations).
 * 
 * It is heavily based on LanguageTools by Daniel Naber, but was
 * also heavily refractored.
 *
 * @author Daniel Naber, Karel Bílek
 */
abstract public class SentenceTokenizer{

    /**
     * end of sentence marker
     */
    protected static final String EOS = "\0";
    
    /**
     * punctuation
     */
    protected static final String P = "[\\.!?…]"; // PUNCTUATION
    
    protected static final String AP = "(?:'|«|\"|”|\\)|\\]|\\})?"; // AFTER PUNCTUATION
    protected static final String PAP = P + AP;
    protected static final String PARENS = "[\\(\\)\\[\\]]"; // parentheses


    // add unbreakable field, for example footnote, if it's at the end of the sentence
    private static final RegExp punctWhitespace = RegExp.compile("(" + PAP + "(\u0002)?\\s)", "g");
    // \p{Lu} = uppercase, with obeying Unicode (\p{Upper} is just US-ASCII!):
    private static final RegExp punctUpperLower = RegExp.compile("(" + PAP
            + ")([\\p{Lu}][^\\p{Lu}.])", "g");
    
    abstract public String nonStandardLetters();

    private final String NSR = nonStandardLetters();
    
    private final RegExp letterPunct = RegExp.compile("(\\s[\\w"+NSR+"]" + P + ")", "g");

    private  final RegExp abbrev1 = RegExp.compile("([^-\\w"+NSR+"][\\w"+NSR+"]" + PAP + "\\s)" + EOS, "g");

    private  final RegExp abbrev2 = RegExp.compile("([^-\\w"+NSR+"][\\w"+NSR+"]" + P + ")" + EOS, "g");
    private  final RegExp abbrev3 = RegExp.compile("(\\s[\\w"+NSR+"]\\.\\s+)" + EOS, "g");
    private static final RegExp abbrev4 = RegExp.compile("(\\.\\.\\. )" + EOS + "([\\p{Ll}])", "g");
    private static final RegExp abbrev5 = RegExp.compile("(['\"]" + P + "['\"]\\s+)" + EOS, "g");
    private static final RegExp abbrev6 = RegExp.compile("([\"']\\s*)" + EOS + "(\\s*[\\p{Ll}])", "g");
    private static final RegExp abbrev7 = RegExp.compile("(\\s" + PAP + "\\s)" + EOS, "g");
    // z.b. 3.10. (im Datum):
    private static final RegExp abbrev8 = RegExp.compile("(\\d{1,2}\\.\\d{1,2}\\.\\s+)" + EOS, "g");
    private final RegExp repair1 = RegExp.compile("('[\\w"+NSR+"]" + P + ")(\\s)", "g");
    private static final RegExp repair2 = RegExp.compile("(\\sno\\.)(\\s+)(?!\\d)", "g");
    private static final RegExp repair3 = RegExp.compile("([ap]\\.m\\.\\s+)([\\p{Lu}])", "g");

    private static final RegExp repair10 = RegExp.compile("([\\(\\[])([!?]+)([\\]\\)]) " + EOS, "g");
    private static final RegExp repair11 = RegExp.compile("([!?]+)([\\)\\]]) " + EOS, "g");
    private static final RegExp repair12 = RegExp.compile("(" + PARENS + ") " + EOS,  "g");

    // some abbreviations:
    /*private static final String[] ABBREV_LIST = {
            // English -- but these work globally for all languages:
            "Mr", "Mrs", "No", "pp", "St", "no",
            "Sr", "Jr", "Bros", "etc", "vs", "esp", "Fig", "fig", "Jan", "Feb", "Mar", "Apr", "Jun", "Jul",
            "Aug", "Sep", "Sept", "Oct", "Okt", "Nov", "Dec", "Ph.D", "PhD",
            "al",  // in "et al."
            "cf", "Inc", "Ms", "Gen", "Sen", "Prof", "Corp", "Co"
    };*/
   private String abbrevsPreRegexp;

   abstract String[] getAbbrevList(); 

    private final Set<RegExp> abbreviationRegExps = new HashSet<RegExp>();

    /**
     * Month names like "Dezember" that should not be considered a sentence
     * boundary in string like "13. Dezember". May also contain other
     * words that indicate there's no sentence boundary when preceded
     * by a number and a dot.
     */
    protected abstract String[] getMonthNames();

    /**
     * Create a sentence tokenizer with the given list of abbreviations,
     * additionally to the built-in ones.
     */
    public SentenceTokenizer() {
    
            final List<String> allAbbreviations = new ArrayList<String>();
         

          String regexpBuilder = null;
          int howmuch = 0;
          //for (String element : abbrev) {
          for (String element : getAbbrevList()) {
                if (regexpBuilder == null) {
                    regexpBuilder = element;
                } else {
                    regexpBuilder += "|"+element;
                }
                //20 was ideal when I experimented
                //not many regexes, not too long ones
                if (howmuch!=20) {
                    howmuch++;
                } else {
                   
                    RegExp newRegExp = RegExp.compile("(\\b(" + regexpBuilder + ")" + PAP + "\\s)" + EOS); 
                    abbreviationRegExps.add(newRegExp);

                    howmuch=0;
                    regexpBuilder=null;
                }
                
            }
            if (regexpBuilder != null) {
                RegExp newRegExp = RegExp.compile("(\\b(" + regexpBuilder + ")" + PAP + "\\s)" + EOS); 
                abbreviationRegExps.add(newRegExp);
 
            }
          
    }

    /**
     * Tokenize the given string to sentences.
     */
    public List<String> tokenize(String s) {
        s = firstSentenceSplitting(s);
        s = removeFalseEndOfSentence(s);
        s = splitUnsplitStuff(s);
        final String[] strings = s.split(EOS);

        List<String> l = new ArrayList<String>();
        for(int i = 0; i < strings.length; i++) {
            
            String sentence = strings[i];
            l.add(sentence);
        }
        return l;
    }

    /**
     * Add a special break character at all places with typical sentence delimiters.
     */
    private String firstSentenceSplitting(String s) {
        // Punctuation followed by whitespace means a new sentence:
        s = punctWhitespace.replace(s, "$1" + EOS);
        // New (compared to the perl module): Punctuation followed by uppercase followed
        // by non-uppercase character (except dot) means a new sentence:
        s = punctUpperLower.replace(s, "$1" + EOS + "$2");
        // Break also when single letter comes before punctuation:
        s = letterPunct.replace(s, "$1" + EOS);
        return s;
    }

    /**
     * Repair some positions that don't require a split, i.e. remove the special break character at
     * those positions.
     */
    protected String removeFalseEndOfSentence(String s)  {
        // Don't split at e.g. "U. S. A.":
        s = abbrev1.replace(s, "$1");
        // Don't split at e.g. "U.S.A.":
        s = abbrev2.replace(s, "$1");
        // Don't split after a white-space followed by a single letter followed
        // by a dot followed by another whitespace.
        // e.g. " p. "
        s = abbrev3.replace(s, "$1");
        // Don't split at "bla bla... yada yada" (TODO: use \.\.\.\s+ instead?)
        s = abbrev4.replace(s, "$1$2");
        // Don't split [.?!] when the're quoted:
        s = abbrev5.replace(s, "$1");


        // Don't split at abbreviations:
        for (final RegExp abbrevRegExp : abbreviationRegExps) {
            //final Matcher matcher = abbrevRegExp.matcher(s);
            s = abbrevRegExp.replace(s, "$1");
        }
        
        /*RegExp pattern = RegExp.compile("(?u)(\\b(" + abbrevsPreRegexp + ")" + PAP + "\\s)" + EOS, "g");
        s = pattern.replace(s, "$1");
*/

        // Don't break after quote unless there's a capital letter:
        // e.g.: "That's right!" he said.
        s = abbrev6.replace(s, "$1$2");

        // fixme? not sure where this should occur, leaving it commented out:
        // don't break: text . . some more text.
        // text=~s/(\s\.\s)$EOS(\s*)/$1$2/sg;

        // e.g. "Das ist . so." -> assume one sentence
        s = abbrev7.replace(s, "$1");

        // e.g. "Das ist . so." -> assume one sentence
        s = abbrev8.replace(s, "$1");

        // extension by dnaber --commented out, doesn't help:
        // text = re.compile("(:\s+)%s(\s*[%s])" % (self.EOS, string.lowercase),
        // re.DOTALL).sub("\\1\\2", text)

        // "13. Dezember" etc. -> keine Satzgrenze:
        if (getMonthNames() != null) {
            for (String element : getMonthNames()) {
                s = s.replaceAll("(\\d+\\.) " + EOS + "(" + element + ")", "$1 $2");
            }
        }
        // z.B. "Das hier ist ein(!) Satz."
        s = repair10.replace(s, "$1$2$3 ");

        // z.B. "Das hier ist (genau!) ein Satz."
        s = repair11.replace(s, "$1$2 ");

        // z.B. "bla (...) blubb" -> kein Satzende
        s = repair12.replace(s, "$1 ");


        s = s.replaceAll("(\\d+\\.) " + EOS + "([\\p{L}&&[^\\p{Lu}]]+)", "$1 $2");

        // z.B. "Das hier ist ein(!) Satz."
        s = s.replaceAll("\\(([!?]+)\\) " + EOS, "($1) "); 
        
        

        return s;
    }

    /**
     * Treat some more special cases that make up a sentence boundary. Insert the special break
     * character at these positions.
     */
    private String splitUnsplitStuff(String s) {
        // e.g. "x5. bla..." -- not sure, leaving commented out:
        // text = re.compile("(\D\d+)(%s)(\s+)" % self.P, re.DOTALL).sub("\\1\\2%s\\3" % self.EOS, text)
        // Not sure about this one, leaving out four now:
        // text = re.compile("(%s\s)(\s*\()" % self.PAP, re.DOTALL).sub("\\1%s\\2" % self.EOS, text)
        // Split e.g.: He won't. #Really.
        s = repair1.replace(s, "$1" + EOS + "$2");
        // Split e.g.: He won't say no. Not really.
        s = repair2.replace(s, "$1" + EOS + "$2");
        // Split at "a.m." or "p.m." followed by a capital letter.
        
        //TODO english true
        //s = repair3.replace(s, "$1" + EOS + "$2");
        
        return s;
    }

    

    /*public static void main(final String[] args) {
      final SentenceTokenizer st = new GermanSentenceTokenizer();
      st.tokenize("Er sagte (...) und");
    }*/

}

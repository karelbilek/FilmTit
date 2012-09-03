/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

Copyright (C) 2005 Daniel Naber

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

package cz.filmtit.share.tokenizers;


import java.util.ArrayList;
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
     * End of sentence marker. Used only internally to split sentences.
     */
    protected static final String END_OF_SENTENCE = "\0";
    
    /**
     * Punctuation marks.
     */
    protected static final String PUNCTUATION = "[\\.!?…]";

    /**
     * Quotes like ' and ".
     */
    private static final String QUOTES = "['\"]";

    /**
     * Everything non-lettery that can happen after punctuation.
     */
    protected static final String AFTER_PUNCTUATION = "(?:'|«|\"|”|\\)|\\]|\\})?"; // AFTER PUNCTUATION

    /**
     * Punctuation together with anything after it.
     */
    protected static final String PUNCTUATION_AND_AFTER = PUNCTUATION + AFTER_PUNCTUATION;

    /**
     * Parenthesis, put out to simplify.
     */
    protected static final String PARENTHESES = "[\\(\\)\\[\\]]";


    /**
     * Regexp for adding \0 after punct and space after it. Used for the first splitting.
     */
    private static final RegExp punctWhitespace = RegExp.compile("(" + PUNCTUATION_AND_AFTER + "(\u0002)?\\s)", "g");

    /**
     * Regextp for adding \0 after punct and upper letter after it.
     *
     */
    private static final RegExp punctUpperLower = RegExp.compile("(" + PUNCTUATION_AND_AFTER
            + ")([\\p{Lu}][^\\p{Lu}.])", "g");
    // \p{Lu} = uppercase, with obeying Unicode (\p{Upper} is just US-ASCII!):

    /**
     * All possible letters in the given language.
     * This is potentially unsafe, if overriding class has "]" in nonStandardLetters.
     */
    private final String POSSIBLE_LETTERS = "[\\w\""+ nonStandardLetters() +"\"]\"";

    /**
     * Anything but possible letters in given language.
     * This is potentially unsafe, if overriding class has "]" in nonStandardLetters.
     */
    private final String ANYTHING_BUT_LETTERS = "[\\w\""+ nonStandardLetters() +"\"]\"";

    //==========REGEXPs==========
    //I will not document them, since I would be just rewriting the names
    private final RegExp singleLetterBeforePunctuation =
            RegExp.compile("(\\s"+POSSIBLE_LETTERS + PUNCTUATION + ")", "g");

    private final RegExp singleLetterAbbreviationWithSpace =
             RegExp.compile("("+
                ANYTHING_BUT_LETTERS+ POSSIBLE_LETTERS + PUNCTUATION_AND_AFTER + "\\s" +
            ")" + END_OF_SENTENCE, "g");

    private  final RegExp singleLetterAbbreviationWithoutSpace =
            RegExp.compile("("+
                    ANYTHING_BUT_LETTERS+POSSIBLE_LETTERS + PUNCTUATION +
             ")" + END_OF_SENTENCE, "g");

    private final RegExp sentenceEndedWithSingleLetter =
            RegExp.compile("(" +
                    "\\s" + POSSIBLE_LETTERS + "\\.\\s+" +
            ")" + END_OF_SENTENCE, "g");

    private static final RegExp ellipsisAndSmallLetter =
            RegExp.compile("(" +
                    "\\.\\.\\. " +
            ")" + END_OF_SENTENCE + "([\\p{Ll}])", "g");


    private static final RegExp punctuationInQuotes =
            RegExp.compile("(" +
                    QUOTES + PUNCTUATION + QUOTES + "\\s+" +
            ")" + END_OF_SENTENCE, "g");

    private static final RegExp smallLetterAfterQuotes =
            RegExp.compile("("+
                    QUOTES+"\\s*" +
            ")" + END_OF_SENTENCE + "(" +
                    "\\s*[\\p{Ll}]" +
            ")", "g");

    private static final RegExp moreDots =
            RegExp.compile("(" +
                    "\\s" + PUNCTUATION_AND_AFTER + "\\s" +
            ")" + END_OF_SENTENCE, "g");

    private static final RegExp datumDots =
            RegExp.compile("(" +
                    "\\d{1,2}\\.\\d{1,2}\\.\\s+" +
            ")" + END_OF_SENTENCE, "g");


    private final RegExp oneWordSentence =
            RegExp.compile("('"+POSSIBLE_LETTERS + PUNCTUATION + ")(\\s)", "g");

    private static final RegExp exclamationInParenthesis =
            RegExp.compile("([\\(\\[])([!?]+)([\\]\\)]) " + END_OF_SENTENCE, "g");

    private static final RegExp wordWithExclamationInParenthesis =
            RegExp.compile("([!?]+)([\\)\\]]) " + END_OF_SENTENCE, "g");

    private static final RegExp dotsAfterParentheses =
            RegExp.compile("(" + PARENTHESES + ") " + END_OF_SENTENCE,  "g");


    /**
     * Returns all letters that might not have been in \w in regexps.
     * @return Letters, just in a string, letter after letter. Shouldn't contain "]" since that
     * would break the regexes.
     */
    abstract public String nonStandardLetters();

    /**
     * List of all the abbreviations in a given language.
     * @return All the abbreviations in a given language, without the dots at the end.
     */
    abstract String[] getAbbrevList();



    /**
     * Month names like "Dezember" that should not be considered a sentence
     * boundary in string like "13. Dezember". May also contain other
     * words that indicate there's no sentence boundary when preceded
     * by a number and a dot.
     *
     * It is language dependent.
     * @return Names of months, as array of strings.
     */
    protected abstract String[] getMonthNames();


    /**
     * Set of all the regexps, built from strings in AbbrevList.
     * It is not 1:1, I found out that it is faster to have about 20 abbreviations
     * in one regexp, connected by "|", rather than having too many regexps.
     */
    private final Set<RegExp> abbreviationRegExps = new HashSet<RegExp>();

    /**
     * Create a sentence tokenizer.
     * Uses list of abbreviations from abstract getAbbrevList().
     */
    public SentenceTokenizer() {

          String regexpBuilder = null;

          int maxAbbrevInRegex = 0;
          for (String element : getAbbrevList()) {
                if (regexpBuilder == null) {
                    regexpBuilder = element;
                } else {
                    regexpBuilder += "|"+element;
                }
                //20 was ideal when I experimented
                //not many regexes, not too long ones
                if (maxAbbrevInRegex!=20) {
                    maxAbbrevInRegex++;
                } else {
                   
                    RegExp newRegExp = RegExp.compile("(\\b(" + regexpBuilder + ")" + PUNCTUATION_AND_AFTER + "\\s)" + END_OF_SENTENCE);
                    abbreviationRegExps.add(newRegExp);

                    maxAbbrevInRegex=0;
                    regexpBuilder=null;
                }
                
            }
            if (regexpBuilder != null) {
                RegExp newRegExp = RegExp.compile("(\\b(" + regexpBuilder + ")" + PUNCTUATION_AND_AFTER + "\\s)" + END_OF_SENTENCE);
                abbreviationRegExps.add(newRegExp);
            }
          
    }



    /**
     * Tokenize the given string to sentences.
     * @param s String to tokenize.
     * @return Sentences as list.
     */
    public List<String> tokenize(String s) {
        s = firstSentenceSplitting(s);
        s = removeFalseEndOfSentence(s);
        s = splitUnsplit(s);
        final String[] strings = s.split(END_OF_SENTENCE);

        List<String> l = new ArrayList<String>();
        for(int i = 0; i < strings.length; i++) {
            
            String sentence = strings[i];
            l.add(sentence);
        }
        return l;
    }



    /**
     * Add a special break character at all places with typical sentence delimiters.
     * @param s String which we want to split.
     * @return the same string with added \0 marks after the sentences; it requires a clean up.
     */
    private String firstSentenceSplitting(String s) {
        // Punctuation followed by whitespace means a new sentence:
        s = punctWhitespace.replace(s, "$1" + END_OF_SENTENCE);
        // New (compared to the perl module): Punctuation followed by uppercase followed
        // by non-uppercase character (except dot) means a new sentence:
        s = punctUpperLower.replace(s, "$1" + END_OF_SENTENCE + "$2");
        // Break also when single letter comes before punctuation:
        s = singleLetterBeforePunctuation.replace(s, "$1" + END_OF_SENTENCE);
        return s;
    }

    /**
     * Repair some positions that don't require a split, i.e. remove the special break character at
     * those positions.
     * @param s String with false end of sentences (\0)
     * @return String without the false end of sentences.
     */
    protected String removeFalseEndOfSentence(String s)  {
        // Don't split at e.g. "U. S. A.":
        s = singleLetterAbbreviationWithSpace.replace(s, "$1");
        // Don't split at e.g. "U.S.A.":
        s = singleLetterAbbreviationWithoutSpace.replace(s, "$1");
        // Don't split after a white-space followed by a single letter followed
        // by a dot followed by another whitespace.
        // e.g. " p. "
        s = sentenceEndedWithSingleLetter.replace(s, "$1");
        // Don't split at "bla bla... yada yada" (TODO: use \.\.\.\s+ instead?)
        s = ellipsisAndSmallLetter.replace(s, "$1$2");
        // Don't split [.?!] when the're quoted:
        s = punctuationInQuotes.replace(s, "$1");


        // Don't split at abbreviations:
        for (final RegExp abbrevRegExp : abbreviationRegExps) {
            //final Matcher matcher = abbrevRegExp.matcher(s);
            s = abbrevRegExp.replace(s, "$1");
        }
        
        /*RegExp pattern = RegExp.compile("(?u)(\\b(" + abbrevsPreRegexp + ")" + PUNCTUATION_AND_AFTER + "\\s)" + END_OF_SENTENCE, "g");
        s = pattern.replace(s, "$1");
*/

        // Don't break after quote unless there's a capital letter:
        // e.g.: "That's right!" he said.
        s = smallLetterAfterQuotes.replace(s, "$1$2");

        // fixme? not sure where this should occur, leaving it commented out:
        // don't break: text . . some more text.
        // text=~s/(\s\.\s)$END_OF_SENTENCE(\s*)/$1$2/sg;

        // e.g. "Das ist . so." -> assume one sentence
        s = moreDots.replace(s, "$1");

        // e.g. "Das ist . so." -> assume one sentence
        s = datumDots.replace(s, "$1");

        // extension by dnaber --commented out, doesn't help:
        // text = re.compile("(:\s+)%s(\s*[%s])" % (self.END_OF_SENTENCE, string.lowercase),
        // re.DOTALL).sub("\\1\\2", text)

        // "13. Dezember" etc. -> keine Satzgrenze:
        if (getMonthNames() != null) {
            for (String element : getMonthNames()) {
                s = s.replaceAll("(\\d+\\.) " + END_OF_SENTENCE + "(" + element + ")", "$1 $2");
            }
        }
        // z.B. "Das hier ist ein(!) Satz."
        s = exclamationInParenthesis.replace(s, "$1$2$3 ");

        // z.B. "Das hier ist (genau!) ein Satz."
        s = wordWithExclamationInParenthesis.replace(s, "$1$2 ");

        // z.B. "bla (...) blubb" -> kein Satzende
        s = dotsAfterParentheses.replace(s, "$1 ");


        s = s.replaceAll("(\\d+\\.) " + END_OF_SENTENCE + "([\\p{L}&&[^\\p{Lu}]]+)", "$1 $2");

        // z.B. "Das hier ist ein(!) Satz."
        s = s.replaceAll("\\(([!?]+)\\) " + END_OF_SENTENCE, "($1) ");
        
        

        return s;
    }

    /**
     * Treat some more special cases that make up a sentence boundary. Insert the special break
     * character at these positions.
     * @param s string with unsplit sentences
     * @return corrected string
     */
    private String splitUnsplit(String s) {
        //There was more stuff in here but it was actually wrong.

        // Split e.g.: He won't. #Really.
        s = oneWordSentence.replace(s, "$1" + END_OF_SENTENCE + "$2");

        return s;
    }



}

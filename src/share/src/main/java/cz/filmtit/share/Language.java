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

package cz.filmtit.share;

import java.io.Serializable;
/**
 * Represents a language.
 *
 * @author Joachim Daiber
 */
public enum Language implements com.google.gwt.user.client.rpc.IsSerializable, Serializable {

    EN      ("en", "English"),
    CS      ("cs", "Czech"),
    DA      ("da", "Danish"),
    NL      ("nl", "Dutch"),
    FI      ("fi", "Finnish"),
    FR      ("fr", "French"),
    DE      ("de", "German"),
    HU      ("hu", "Hungarian"),
    IT      ("it", "Italian"),
    NO      ("no", "Norwegian"),
    PT      ("pt", "Portuguese"),
    RO      ("ro", "Romanian"),
    RU      ("ru", "Russian"),
    ES      ("es", "Spanish"),
    SV      ("sv", "Swedish"),
    TR      ("tr", "Turkish"),
    UNKNOWN ("?", "Unknown language");

    private String code;
    private String name;

    Language(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static Language fromCode(String code) {
        for (Language language : values()) {
            if (language.getCode().equalsIgnoreCase(code))
                return language;
        }
        return Language.UNKNOWN;
    }
    
    /**
     * Returns CS for EN and EN for CS.
     * @return
     */
    public Language theOther() {
    	switch (this) {
		case CS:
			return EN;
		case EN:
			return CS;
		default:
			return UNKNOWN;
		}
    }    
    
    /**
     * Gets a string representing the translation direction,
     * using language codes.
     * @param from Source Language
     * @param to Target Language
     * @return e.g. "en>cs"
     */
    public static String getTranslationDirectionCodes(Language from, Language to) {
    	return from.code + ">" + to.code;
    }
    
    /**
     * Gets a string representing the translation direction,
     * using language codes.
     * @return e.g. "en>cs"
     */
    public String getTranslationDirectionCodes() {
    	return getTranslationDirectionCodes(this, theOther());
    }    
    
    /**
     * Gets a string representing the translation direction,
     * using language names.
     * @param from Source language
     * @param to Target language
     * @return e.g. "English > Czech"
     */
    public static String getTranslationDirectionNames(Language from, Language to) {
    	return from.name + " > " + to.name;
    }
    
    /**
     * Gets a string representing the translation direction,
     * using language names.
     * @return e.g. "English > Czech"
     */
    public String getTranslationDirectionNames() {
    	return getTranslationDirectionNames(this, theOther());
    }    
    
    
}

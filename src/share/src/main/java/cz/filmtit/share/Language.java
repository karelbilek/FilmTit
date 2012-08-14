package cz.filmtit.share;

import java.io.Serializable;
/**
 * @author Joachim Daiber
 */
public enum Language implements com.google.gwt.user.client.rpc.IsSerializable, Serializable {

    EN      ("en", "English"),
    CS      ("cs", "Czech"),
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
     * @param from
     * @param to
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
     * @param from
     * @param to
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

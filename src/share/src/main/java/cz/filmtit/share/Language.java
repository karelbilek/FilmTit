package cz.filmtit.share;

/**
 * @author Joachim Daiber
 */
public enum Language {

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
            if (language.getCode().equals(code))
                return language;
        }
        return Language.UNKNOWN;
    }
}

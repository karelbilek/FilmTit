package filmtit0.client;


import java.util.*;

/**
 * Represents a matching chunk,
 * along with the list of corresponding translations.
 * (vaguely based on class Match from Jindra)
 * 
 * @author Honza Václ
*/

public class GUIMatch {

	private String text;
    private List<GUITranslation> translations;

    public GUIMatch(String text, List<GUITranslation> translations) {
        this.text = text;
        this.translations = translations;
    }

    public String getMatchText() {
        return text;
    }

    public void setMatchText(String match) {
        if (match == null) { this.text = match; }
        else { throw new UnsupportedOperationException("The match text can be set just once."); }
    }

    public List<GUITranslation> getTranslations() {
        return translations;
    }
    
    public List<String> getTranslationsAsStrings() {
    	List<String> translstrings = new ArrayList<String>();
    	ListIterator<GUITranslation> li = translations.listIterator();
    	while (li.hasNext()) {
    		translstrings.add(li.next().getTranslationText());
    	}
    	return translstrings;
    }

}

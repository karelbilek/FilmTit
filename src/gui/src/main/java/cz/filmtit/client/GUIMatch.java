package cz.filmtit.client;


import java.util.*;


/**
 * Represents a matching chunk,
 * along with the list of corresponding translations.
 * (vaguely based on class Match from Jindra)
 * 
 * @author Honza Václ
*/

public class GUIMatch {

	private Match match;
	private List<GUITranslation> translations;


	public GUIMatch(Match sharedmatch) {
		this.match = sharedmatch;
		this.translations = new ArrayList<GUITranslation>();
		ListIterator<Translation> sharedtranslationiterator = sharedmatch.translations.listIterator();
		while (sharedtranslationiterator.hasNext()) {
			this.translations.add( new GUITranslation(sharedtranslationiterator.next()) );
		}
	}
	

	public String getMatchText() {
        return match.text;
    }
    
    public void setMatchText(String match) {
        if (match == null) { this.match.text = match; }
        else { throw new UnsupportedOperationException("The match text can be set just once."); }
    }
    
    
    public List<GUITranslation> getTranslations() {
        return this.translations;
    }
    
    public List<String> getTranslationsAsStrings() {
    	List<String> translstrings = new ArrayList<String>();
    	ListIterator<GUITranslation> li = this.translations.listIterator();
    	while (li.hasNext()) {
    		translstrings.add(li.next().getTranslationText());
    	}
    	return translstrings;
    }
    
}

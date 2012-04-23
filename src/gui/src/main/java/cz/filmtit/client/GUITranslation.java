package cz.filmtit.client;

/**
 * Represents a single translation chunk.
 * (vaguely based on class Translation from Jindra)
 * 
 * @author Honza Václ
*/

public class GUITranslation {
    
	private Translation translation;
	
    public GUITranslation(Translation sharedtranslation) {
    	this.translation = sharedtranslation;
    }
    
	/**
     * Creates the translation object of given score and text.
     * @param text Text of the translation.
     * @param score Rank of the translation from TM.
     */
    public GUITranslation(String text, double score) {
        this.translation.text = text;
        this.translation.score = score;
    }
    

    public String getTranslationText() {
        return translation.text;
    }

    public void setTranslationText(String text) {
        if (text == null) { this.translation.text = text; }
        else { throw new UnsupportedOperationException("Translation text can be set just once."); }
    }

}

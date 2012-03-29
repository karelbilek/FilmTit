package cz.filmtit.client;


/**
 * Represents a single translation chunk.
 * (vaguely based on class Translation from Jindra)
 * 
 * @author Honza Václ
*/

public class GUITranslation {
    /**
     * Creates the translation object of given score and text.
     * @param text Text of the translation.
     * @param score Rank of the translation from TM.
     */
    public GUITranslation(String text) {
        this.text = text;
    }
    
    private String text;
    

    public String getTranslationText() {
        return text;
    }

    public void setTranslationText(String text) {
        if (text == null) { this.text = text; }
        else { throw new UnsupportedOperationException("Translation text can be set just once."); }
    }

}

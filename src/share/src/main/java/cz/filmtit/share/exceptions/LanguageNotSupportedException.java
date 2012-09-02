package cz.filmtit.share.exceptions;

/**
 * Exception is thrown in core if is requested unknown language
 * @author Joachim Daiber
 */
public class LanguageNotSupportedException extends Exception {

    public LanguageNotSupportedException(String message) {
        super(message);
    }

}

package cz.filmtit.share.exceptions;

/**
 * @author Joachim Daiber
 */
public class SearcherNotAvailableException extends Exception {

    public SearcherNotAvailableException(String message) {
        super(message);
    }

    public SearcherNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }


}

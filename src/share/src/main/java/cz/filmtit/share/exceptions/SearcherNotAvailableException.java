package cz.filmtit.share.exceptions;

/**
 * Exception is thrown if source for translating  (eg. TM, Mosses...) is missing
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

package cz.filmtit.share.exceptions;

import java.io.Serializable;

/**
 * An exception the user space throws if a non-existing or expired session ID is used.
 * @author Jindřich Libovický
 */
public class InvalidSessionIdException extends Exception implements Serializable {
    public InvalidSessionIdException(String message) {
        super(message);
    }
}

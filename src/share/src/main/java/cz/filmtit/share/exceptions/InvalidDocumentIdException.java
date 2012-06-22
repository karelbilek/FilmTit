package cz.filmtit.share.exceptions;

import java.io.Serializable;

/**
 * An exception the user space throws if an operation with an non-existing document is atempted.
 * @author Jindřich Libovický
 */
public class InvalidDocumentIdException extends Exception implements Serializable {
    InvalidDocumentIdException() {}

    public InvalidDocumentIdException(String message) {
        super(message);
    }
}

package cz.filmtit.share.exceptions;

import java.io.Serializable;

/**
 * An exception the user space throws if an operation with an non-existing document is atempted.
 * @author Jindřich Libovický
 */
public class InvalidDocumentId extends Exception implements Serializable {
    public InvalidDocumentId(String message) {
        super(message);
    }
}

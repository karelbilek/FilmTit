package cz.filmtit.share.exceptions;

import java.io.Serializable;

/**
 * An exception the parser throws if the document is somehow wrong
 */
public class InvalidDocumentFormatException extends Exception implements Serializable {
    public InvalidDocumentFormatException(String message) {
        super(message);
    }
}

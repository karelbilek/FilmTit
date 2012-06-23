package cz.filmtit.share.exceptions;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * An exception the user space throws if an operation with an non-existing document is atempted.
 * @author Jindřich Libovický
 */
public class InvalidDocumentIdException extends Exception implements Serializable, IsSerializable {
    InvalidDocumentIdException() {}

    public InvalidDocumentIdException(String message) {
        super(message);
    }
}

package cz.filmtit.share.exceptions;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * An exception the user space throws if a non-existing or expired session ID is used.
 * @author Jindřich Libovický
 */
public class InvalidSessionIdException extends Exception implements Serializable, IsSerializable {
    InvalidSessionIdException() {}

    public InvalidSessionIdException(String message) {
        super(message);
    }
}

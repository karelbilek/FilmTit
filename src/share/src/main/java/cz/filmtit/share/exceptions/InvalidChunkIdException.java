package cz.filmtit.share.exceptions;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

/**
 * The exception is thrown by the User Space if saved chunk does not belong to opened document
 * @author Jindřich Libovický
 */
public class InvalidChunkIdException extends Exception implements Serializable, IsSerializable {
    InvalidChunkIdException() {}

    public InvalidChunkIdException(String message) {
        super(message);
    }
}

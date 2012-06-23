package cz.filmtit.share.exceptions;

import java.io.Serializable;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Jindřich Libovický
 */
public class InvalidChunkIdException extends Exception implements Serializable, IsSerializable {
    InvalidChunkIdException() {}

    public InvalidChunkIdException(String message) {
        super(message);
    }
}

package cz.filmtit.share.exceptions;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

/**
 * Exceptions is thrown in User space if some object have bad setting of properties (eg. invalid email , or invalid time in chunk )
 */
public class InvalidValueException extends Exception implements Serializable, IsSerializable {
    InvalidValueException() {}

    public InvalidValueException(String message) {
        super(message);
    }
}

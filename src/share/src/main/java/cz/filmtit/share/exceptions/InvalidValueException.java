package cz.filmtit.share.exceptions;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;


public class InvalidValueException extends Exception implements Serializable, IsSerializable {
    InvalidValueException() {}

    public InvalidValueException(String message) {
        super(message);
    }
}

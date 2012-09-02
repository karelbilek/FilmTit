package cz.filmtit.share.exceptions;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

/**
 * Exception is thrown in core when failed seeking for candidate translation pair in TM
 *
 * @author Joachim Daiber
 */
public class DatabaseException extends Exception implements Serializable, IsSerializable {

    public DatabaseException(String message) {
    	super(message);
    }


}

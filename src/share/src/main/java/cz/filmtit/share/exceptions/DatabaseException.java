package cz.filmtit.share.exceptions;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Joachim Daiber
 */
public class DatabaseException extends Exception implements Serializable, IsSerializable {

    public DatabaseException(String message) {
    	super(message);
    }


}

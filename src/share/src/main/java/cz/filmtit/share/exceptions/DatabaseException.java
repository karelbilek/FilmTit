package cz.filmtit.share.exceptions;

import java.io.Serializable;

/**
 * @author Joachim Daiber
 */
public class DatabaseException extends Exception implements Serializable {

    public DatabaseException(String message) {
    	super(message);
    }


}

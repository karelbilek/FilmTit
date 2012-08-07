package cz.filmtit.share.exceptions;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.io.Serializable;

/**
 * The exception is thrown by the User Space if the client requires the sessionID for an authentication session
 * that has failed.
 *
 * @author Jindřich Libovický
 */
public class AuthenticationFailedException extends Exception implements Serializable, IsSerializable {

    public AuthenticationFailedException(String message) {
        super(message);
    }
}

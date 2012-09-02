package cz.filmtit.share;

import java.io.Serializable;

/**
 * Represents services which are available for the OpenID login.
 *
 * @author Jindřich Libovický
 */
public enum AuthenticationServiceType implements com.google.gwt.user.client.rpc.IsSerializable, Serializable {
    GOOGLE,
    YAHOO,
    SEZNAM
}

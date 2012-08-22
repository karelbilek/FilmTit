package cz.filmtit.userspace.login;

import org.expressme.openid.Authentication;

import java.util.Date;

/**
 * An extension of the JOpenID Authentication class which contains the time of its creation to provide the server
 * information when it should be timed out.
 *
 * @author Jindřich Libovický
 */
public class TimedAuthenticationWrapper {
    /**
     * JOpenID authentication object.
     */
    private Authentication authentication;
    /**
     * Time when the object was created.
     */
    private long creationTime = new Date().getTime();

    /**
     * Creates the authentication wrapper for authentication object provided by OpenID manager
     * and remembers its creation time.
     * @param authentication
     */
    public TimedAuthenticationWrapper(Authentication authentication) {
        this.authentication = authentication;
    }

    /**
     * Gets the time the object was created.
     * @return The time the object was created.
     */
    public long getCreationTime() { return getCreationTime(); }

    /**
     * Gets the JOpenID authentication object.
     * @return JOpenID authentication object
     */
    public Authentication getAuthentication() { return  authentication; }
}

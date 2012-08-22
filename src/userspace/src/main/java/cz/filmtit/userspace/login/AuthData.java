package cz.filmtit.userspace.login;

import org.expressme.openid.Endpoint;

import java.util.Date;

/**
 * Class representing authentication is process while logging in using OpenID
 */
public class AuthData {
    /**
     * Mac key for Open ID authentication.
     */
    public byte[] Mac_key;
    /**
     * OpenID endpoint.
     */
    public Endpoint endpoint;

    /**
     * Time when the authentication session was started as long.
     */
    private long creationTime = new Date().getTime();

    /**
     * Gets the time when the authentication started as long.
     * @return Time when the authentication session was started.
     */
    public long getCreationTime() { return creationTime; }
}
package cz.filmtit.userspace.login;

import org.expressme.openid.Endpoint;

/**
 * Used by JOpenID.
 */
public class AuthData {
    public byte[] Mac_key;
    public Endpoint endpoint;
}
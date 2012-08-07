package cz.filmtit.share;

/**
 * An object of class LoginSessionResponse is sent by the User Space to the client when the client calls the
 * getAuthenticationURL. It contains generate unique identifier of the login session and URL of the openID
 * page, where the user can acutally log in.
 *
 * @author Jindřich Libovický
 */
public class LoginSessionResponse {
    public LoginSessionResponse(long authID, String openIDURL) {
        this.authID = authID;
        this.openIDURL = openIDURL;
    }

    /**
     * Server-side unique identifier of the authentication session.
     */
    private long authID;
    /**
     * URL of the page of the OpenID provider.
     */
    private String openIDURL;
}

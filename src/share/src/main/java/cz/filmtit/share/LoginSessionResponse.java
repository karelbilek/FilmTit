package cz.filmtit.share;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

/**
 * An object of class LoginSessionResponse is sent by the User Space to the client when the client calls the
 * getAuthenticationURL. It contains generate unique identifier of the login session and URL of the openID
 * page, where the user can actually log in.
 *
 * @author Jindřich Libovický
 */
public class LoginSessionResponse implements Serializable, IsSerializable {

    /**
     * Creates LoginSessionResponse with given properties
     * @param authID Authentication token the GUI uses for asking for session ID
     * @param openIDURL URL where the user is suppose to authenticate
     */
    public LoginSessionResponse(int authID, String openIDURL) {
        this.authID = authID;
        this.openIDURL = openIDURL;
    }

    /**
     * Server-side unique identifier of the authentication session.
     */
    private int authID;

    /**
     * Gets the server-side identifier of the authentication session.
     * @return
     */
    public int getAuthID() {
		return authID;
	}

	/**
     * URL of the page of the OpenID provider.
     */
    private String openIDURL;

    /**
     * Gets the OpenID provider URL.
     * @return URL of the page of the OpenID provider.
     */
    public String getOpenIDURL() {
		return openIDURL;
	}

    /**
     * Default constructor required be GWT.
     */
    private LoginSessionResponse() {
		// nothing
	}
}

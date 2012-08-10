package cz.filmtit.client;

import cz.filmtit.client.callables.SetUserTranslation;

/**
 * An object that can be stored in the local storage by LocalStorageHandler.
 * @author rur
 *
 * @param <T>
 */
public interface Storable {

	/**
	 * Serialize the object into a KeyValuePair object (without class id and username).
	 * @return
	 */
	KeyValuePair toKeyValuePair();

	/**
	 * Invoked after the object is loaded from the local storage.
	 * The method must ensure that <br>
	 * LocalStorageHandler.SuccessOnLoadFromLocalStorage(this); <br>
	 * or <br>
	 * LocalStorageHandler.FailureOnLoadFromLocalStorage(this); <br>
	 * gets called on success or error.
	 */
	void onLoadFromLocalStorage();
	
	/**
	 * Get a string uniquely identifying the class.
	 * @return
	 */
	String getClassID();
	
	/**
	 * Create a string representing the object to be shown to the user.
	 * @return
	 */
	String toUserFriendlyString();
	
	// Java cannot define static methods in interfaces
	// static T fromKeyValuePair(KeyValuePair keyValuePair)
}

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
	 */
	void onLoadFromLocalStorage();
	
	/**
	 * Get a string uniquely identifying the class.
	 * The method must ensure that <br>
	 * LocalStorageHandler.removeFromLocalStorage(this); <br>
	 * LocalStorageHandler.decrementYetToUpload(); <br>
	 * gets called on success or unrecoverble error.
	 * @return
	 */
	String getClassID();
	
	// Java cannot define static methods in interfaces
	// static T fromKeyValuePair(KeyValuePair keyValuePair)
}

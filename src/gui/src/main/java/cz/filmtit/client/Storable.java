/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/

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

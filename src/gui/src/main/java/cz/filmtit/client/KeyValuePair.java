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

/**
 * Represents a pair of Strings called "key" and "value".
 */
public class KeyValuePair {
	
	/**
	 * The "key" part of the pair.
	 */
	private String key;

	/**
	 * The "key" part of the pair.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * The "value" part of the pair.
	 */
	private String value;
	
	/**
	 * The "value" part of the pair.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Creates a new pair of two string, a "key" and a "value".
	 * @param key The "key" part of the pair.
	 * @param value The "value" part of the pair.
	 */
	public KeyValuePair(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	/**
	 * A one-string representation in the form:
	 * &lt;key,value&gt;
	 */
	@Override
	public String toString() {
		return "<" + key + "," + value + ">";
	}
	
}
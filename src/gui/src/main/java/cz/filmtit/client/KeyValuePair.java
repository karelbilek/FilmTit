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
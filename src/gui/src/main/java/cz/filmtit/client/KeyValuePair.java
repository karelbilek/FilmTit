package cz.filmtit.client;

/**
 * Represents a pair of Strings called key and value.
 */
public class KeyValuePair {
	
	private String key;

	public String getKey() {
		return key;
	}

	private String value;
	
	public String getValue() {
		return value;
	}

	public KeyValuePair(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	@Override
	public String toString() {
		return "<" + key + "," + value + ">";
	}
	
}
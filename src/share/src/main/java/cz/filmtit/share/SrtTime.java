package cz.filmtit.share;

import cz.filmtit.share.exceptions.InvalidValueException;

/**
 * Represents one time in an SRT file, which has the format of
 * <code>hh:mm:ss,ttt</code>.
 * Provides int getters, int and String setters, convertors to and from a String.
 * TODO: convertor from miliseconds if needed
 * TODO: comparator
 * TODO: subtractor
 * @author rur
 *
 */
public class SrtTime {
	
	// constants
	public static final char HM_DELIMITER = ':';
	public static final char MS_DELIMITER = ':';
	public static final char ST_DELIMITER = ',';
	
	// values (h m s could be byte)
	private int h;
	private int m;
	private int s;
	private int t;
	
	// getters
	public int getH() {
		return h;
	}
	public int getM() {
		return m;
	}
	public int getS() {
		return s;
	}
	public int getT() {
		return t;
	}
	
	// setters from int
	public void setH(int h) throws InvalidValueException {
		if (0 <= h && h <= 99) {
			this.h = h;
		}
		else {
			throw new InvalidValueException("Value of h must be 0 <= h <= 99, ' + h + ' is invalid!");
		}
	}
	public void setM(int m) throws InvalidValueException {
		if (0 <= m && m <= 59) {
			this.m = m;
		}
		else {
			throw new InvalidValueException("Value of m must be 0 <= m <= 59, ' + m + ' is invalid!");
		}
	}
	public void setS(int s) throws InvalidValueException {
		if (0 <= s && s <= 59) {
			this.s = s;
		}
		else {
			throw new InvalidValueException("Value of s must be 0 <= s <= 59, ' + s + ' is invalid!");
		}
	}
	public void setT(int t) throws InvalidValueException {
		if (0 <= t && t <= 999) {
			this.t = t;
		}
		else {
			throw new InvalidValueException("Value of t must be 0 <= t <= 999, ' + t + ' is invalid!");
		}
	}

	// setters from String
	public void setH(String h) throws InvalidValueException {
		try {
			setH(Integer.parseInt(h));
		}
		catch (NumberFormatException e) {
			throw new InvalidValueException("Value of h must be an ineteger, ' + h + ' is invalid!");
		}
	}
	public void setM(String m) throws InvalidValueException {
		try {
			setM(Integer.parseInt(m));
		}
		catch (NumberFormatException e) {
			throw new InvalidValueException("Value of m must be an ineteger, ' + m + ' is invalid!");
		}
	}
	public void setS(String s) throws InvalidValueException {
		try {
			setS(Integer.parseInt(s));
		}
		catch (NumberFormatException e) {
			throw new InvalidValueException("Value of s must be an ineteger, ' + s + ' is invalid!");
		}
	}
	public void setT(String t) throws InvalidValueException {
		try {
			setT(Integer.parseInt(t));
		}
		catch (NumberFormatException e) {
			throw new InvalidValueException("Value of t must be an ineteger, ' + t + ' is invalid!");
		}
	}

	// constructors
	public SrtTime(int h, int m, int s, int t) throws InvalidValueException {
		setH(h);
		setM(m);
		setS(s);
		setT(t);
	}
	public SrtTime(String time) throws InvalidValueException {
		if (time.length() == 12) {
			// check the delimiters
			if (
					time.charAt(2) != HM_DELIMITER ||
					time.charAt(5) != MS_DELIMITER ||
					time.charAt(8) != ST_DELIMITER
			) {
				// wrong delimiters
				throw new InvalidValueException("Format of SRT time must be hh:mm:ss,ttt, '" + time + "' is invalid!");
			}
			else {
				// try set the values
				setH(time.substring(0, 1));
				setM(time.substring(3, 4));
				setS(time.substring(6, 7));
				setT(time.substring(9, 11));
			}
		}
		else {
			// wrong length
			throw new InvalidValueException("Format of SRT time must be hh:mm:ss,ttt, '" + time + "' is invalid!");
		}
	}
	
	// toString
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(h);
		sb.append(HM_DELIMITER);
		sb.append(m);
		sb.append(MS_DELIMITER);
		sb.append(s);
		sb.append(ST_DELIMITER);
		sb.append(t);
		return sb.toString();
	}
	
	public int toMs() {
		int value = 0;
		value += h;
		value *= 60;
		value += m;
		value *= 60;
		value += s;
		value *= 1000;
		value += t;
		return value;
	}
	
}
